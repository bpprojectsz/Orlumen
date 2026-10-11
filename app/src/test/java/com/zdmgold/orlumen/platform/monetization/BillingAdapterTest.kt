package com.zdmgold.orlumen.platform.monetization

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BillingAdapterTest {
    private val proId = "com.zdmgold.orlumen.pro"

    private fun record(
        products: List<String> = listOf(proId),
        state: RecordState = RecordState.PURCHASED,
        acknowledged: Boolean = true,
        token: String = "token"
    ) = PurchaseRecord(products, state, acknowledged, token)

    @Test
    fun noPurchasesMeansNotOwned() {
        val evaluation = evaluatePurchases(emptyList(), proId)
        assertFalse(evaluation.owned)
        assertFalse(evaluation.pending)
        assertTrue(evaluation.tokensToAcknowledge.isEmpty())
    }

    @Test
    fun completedProPurchaseIsOwned() {
        assertTrue(evaluatePurchases(listOf(record()), proId).owned)
    }

    @Test
    fun otherProductsDoNotGrantPro() {
        assertFalse(evaluatePurchases(listOf(record(products = listOf("something.else"))), proId).owned)
    }

    @Test
    fun pendingPurchaseIsNotOwnedYet() {
        val evaluation = evaluatePurchases(listOf(record(state = RecordState.PENDING)), proId)
        assertFalse(evaluation.owned)
        assertTrue(evaluation.pending)
    }

    @Test
    fun onlyUnacknowledgedCompletedPurchasesNeedAcknowledging() {
        val records = listOf(
            record(acknowledged = false, token = "a"),
            record(acknowledged = true, token = "b"),
            record(state = RecordState.PENDING, acknowledged = false, token = "c")
        )
        assertEquals(listOf("a"), evaluatePurchases(records, proId).tokensToAcknowledge)
    }
}
