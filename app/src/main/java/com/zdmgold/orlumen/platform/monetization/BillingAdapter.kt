package com.zdmgold.orlumen.platform.monetization

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.zdmgold.orlumen.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

private const val TAG = "BillingAdapter"

enum class Ownership {
    UNKNOWN,
    OWNED,
    NOT_OWNED
}

enum class PurchaseEvent {
    PURCHASED,
    PENDING,
    CANCELED,
    FAILED
}

enum class PurchaseLaunchResult {
    LAUNCHED,
    UNAVAILABLE,
    ALREADY_OWNED,
    FAILED
}

data class ProProductInfo(val productId: String, val formattedPrice: String)

enum class RecordState {
    PURCHASED,
    PENDING,
    OTHER
}

data class PurchaseRecord(
    val products: List<String>,
    val state: RecordState,
    val acknowledged: Boolean,
    val token: String
)

data class PurchaseEvaluation(val owned: Boolean, val pending: Boolean, val tokensToAcknowledge: List<String>)

internal fun evaluatePurchases(records: List<PurchaseRecord>, proProductId: String): PurchaseEvaluation {
    val pro = records.filter { proProductId in it.products }
    return PurchaseEvaluation(
        owned = pro.any { it.state == RecordState.PURCHASED },
        pending = pro.any { it.state == RecordState.PENDING },
        tokensToAcknowledge = pro.filter { it.state == RecordState.PURCHASED && !it.acknowledged }.map { it.token }
    )
}

interface BillingGateway {
    val ownership: StateFlow<Ownership>
    val product: StateFlow<ProProductInfo?>
    val events: SharedFlow<PurchaseEvent>

    fun connect()

    fun refresh()

    fun launchPurchase(activity: Activity): PurchaseLaunchResult
}

@Singleton
class BillingAdapter @Inject constructor(@ApplicationContext context: Context) : BillingGateway {
    private val _ownership = MutableStateFlow(Ownership.UNKNOWN)
    override val ownership: StateFlow<Ownership> = _ownership.asStateFlow()

    private val _product = MutableStateFlow<ProProductInfo?>(null)
    override val product: StateFlow<ProProductInfo?> = _product.asStateFlow()

    private val _events = MutableSharedFlow<PurchaseEvent>(extraBufferCapacity = 1)
    override val events: SharedFlow<PurchaseEvent> = _events.asSharedFlow()

    @Volatile
    private var productDetails: ProductDetails? = null

    @Volatile
    private var connecting = false

    private val client: BillingClient = BillingClient.newBuilder(context)
        .setListener { result, purchases -> onPurchasesUpdated(result, purchases) }
        .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
        .enableAutoServiceReconnection()
        .build()

    override fun connect() {
        if (client.isReady || connecting) return
        connecting = true
        client.startConnection(
            object : BillingClientStateListener {
                override fun onBillingSetupFinished(result: BillingResult) {
                    connecting = false
                    if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                        queryOwnership()
                        queryProduct()
                    } else {
                        Log.w(TAG, "Billing setup failed: ${result.responseCode} ${result.debugMessage}")
                    }
                }

                override fun onBillingServiceDisconnected() {
                    connecting = false
                }
            }
        )
    }

    override fun refresh() {
        if (client.isReady) {
            queryOwnership()
            queryProduct()
        } else {
            connect()
        }
    }

    override fun launchPurchase(activity: Activity): PurchaseLaunchResult {
        val details = productDetails
        return when {
            details == null -> PurchaseLaunchResult.UNAVAILABLE
            _ownership.value == Ownership.OWNED -> PurchaseLaunchResult.ALREADY_OWNED
            client.launchBillingFlow(activity, flowParamsFor(details)).responseCode ==
                BillingClient.BillingResponseCode.OK -> PurchaseLaunchResult.LAUNCHED
            else -> PurchaseLaunchResult.FAILED
        }
    }

    private fun flowParamsFor(details: ProductDetails): BillingFlowParams {
        val productParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(details)
            .build()
        return BillingFlowParams.newBuilder().setProductDetailsParamsList(listOf(productParams)).build()
    }

    private fun queryOwnership() {
        val params = QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP).build()
        client.queryPurchasesAsync(params) { result, purchases ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) applyPurchases(purchases)
        }
    }

    private fun queryProduct() {
        val product = QueryProductDetailsParams.Product.newBuilder()
            .setProductId(BuildConfig.PRO_PRODUCT_ID)
            .setProductType(BillingClient.ProductType.INAPP)
            .build()
        val params = QueryProductDetailsParams.newBuilder().setProductList(listOf(product)).build()
        client.queryProductDetailsAsync(params) { result, detailsResult ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                val details = detailsResult.productDetailsList.firstOrNull()
                productDetails = details
                _product.value = details?.oneTimePurchaseOfferDetails?.formattedPrice
                    ?.let { ProProductInfo(BuildConfig.PRO_PRODUCT_ID, it) }
            }
        }
    }

    private fun applyPurchases(purchases: List<Purchase>): PurchaseEvaluation {
        val evaluation = evaluatePurchases(purchases.map { it.toRecord() }, BuildConfig.PRO_PRODUCT_ID)
        evaluation.tokensToAcknowledge.forEach { acknowledge(it) }
        _ownership.value = if (evaluation.owned) Ownership.OWNED else Ownership.NOT_OWNED
        return evaluation
    }

    private fun acknowledge(token: String) {
        val params = AcknowledgePurchaseParams.newBuilder().setPurchaseToken(token).build()
        client.acknowledgePurchase(params) { result ->
            if (result.responseCode != BillingClient.BillingResponseCode.OK) {
                Log.w(TAG, "Acknowledge failed: ${result.responseCode} ${result.debugMessage}")
            }
        }
    }

    private fun onPurchasesUpdated(result: BillingResult, purchases: List<Purchase>?) {
        when (result.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                val evaluation = applyPurchases(purchases.orEmpty())
                _events.tryEmit(
                    if (evaluation.pending &&
                        !evaluation.owned
                    ) {
                        PurchaseEvent.PENDING
                    } else {
                        PurchaseEvent.PURCHASED
                    }
                )
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> _events.tryEmit(PurchaseEvent.CANCELED)
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                queryOwnership()
                _events.tryEmit(PurchaseEvent.PURCHASED)
            }
            else -> {
                Log.w(TAG, "Purchase failed: ${result.responseCode} ${result.debugMessage}")
                _events.tryEmit(PurchaseEvent.FAILED)
            }
        }
    }

    private fun Purchase.toRecord(): PurchaseRecord {
        val recordState = when (purchaseState) {
            Purchase.PurchaseState.PURCHASED -> RecordState.PURCHASED
            Purchase.PurchaseState.PENDING -> RecordState.PENDING
            else -> RecordState.OTHER
        }
        return PurchaseRecord(products, recordState, isAcknowledged, purchaseToken)
    }
}
