package com.zdmgold.orlumen.core.repository

import android.app.Activity
import com.zdmgold.orlumen.core.datastore.PreferencesRepository
import com.zdmgold.orlumen.platform.monetization.BillingGateway
import com.zdmgold.orlumen.platform.monetization.Ownership
import com.zdmgold.orlumen.platform.monetization.ProProductInfo
import com.zdmgold.orlumen.platform.monetization.PurchaseEvent
import com.zdmgold.orlumen.platform.monetization.PurchaseLaunchResult
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

internal fun resolveEntitlement(cachedIsPro: Boolean, ownership: Ownership): Boolean = when (ownership) {
    Ownership.OWNED -> true
    Ownership.NOT_OWNED -> false
    Ownership.UNKNOWN -> cachedIsPro
}

@Singleton
class EntitlementRepository @Inject constructor(
    private val billing: BillingGateway,
    private val preferencesRepository: PreferencesRepository
) {
    /** Confirmed ownership wins; while Play is unreachable the last confirmed answer is used. */
    val isPro: Flow<Boolean> = combine(
        preferencesRepository.preferences.map { it.isProUser }.distinctUntilChanged(),
        billing.ownership
    ) { cached, ownership -> resolveEntitlement(cached, ownership) }.distinctUntilChanged()

    val product: StateFlow<ProProductInfo?> = billing.product
    val events: SharedFlow<PurchaseEvent> = billing.events

    fun connect() = billing.connect()

    fun refresh() = billing.refresh()

    fun purchase(activity: Activity): PurchaseLaunchResult = billing.launchPurchase(activity)

    /** Mirrors confirmed ownership into preferences so it survives offline starts. Run in a long-lived scope. */
    suspend fun persistConfirmedOwnership() {
        billing.ownership.collect { ownership ->
            when (ownership) {
                Ownership.OWNED -> preferencesRepository.setProUser(true)
                Ownership.NOT_OWNED -> preferencesRepository.setProUser(false)
                Ownership.UNKNOWN -> Unit
            }
        }
    }
}
