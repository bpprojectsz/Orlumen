package com.zdmgold.orlumen.core.viewmodel

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.zdmgold.orlumen.core.datastore.PreferencesRepository
import com.zdmgold.orlumen.core.repository.EntitlementRepository
import com.zdmgold.orlumen.platform.monetization.Ownership
import com.zdmgold.orlumen.platform.monetization.ProProductInfo
import com.zdmgold.orlumen.platform.monetization.PurchaseEvent
import com.zdmgold.orlumen.testing.FakeGateway
import com.zdmgold.orlumen.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

@OptIn(ExperimentalCoroutinesApi::class)
class EntitlementViewModelTest {
    @get:Rule
    val mainRule = MainDispatcherRule()

    @get:Rule
    val folder = TemporaryFolder()

    private class Setup(val model: EntitlementViewModel, val gateway: FakeGateway, val prefs: PreferencesRepository)

    private fun TestScope.setup(): Setup {
        val store = PreferenceDataStoreFactory.create(scope = backgroundScope) {
            folder.newFile("entitlement-vm.preferences_pb")
        }
        val prefs = PreferencesRepository(store)
        val gateway = FakeGateway()
        val model = EntitlementViewModel(EntitlementRepository(gateway, prefs))
        backgroundScope.launch { model.uiState.collect {} }
        return Setup(model, gateway, prefs)
    }

    @Test
    fun connectsToBillingWhenCreated() = runTest(UnconfinedTestDispatcher()) {
        val setup = setup()
        assertEquals(1, setup.gateway.connectCalls)
        assertFalse(setup.model.uiState.value.isPro)
        assertNull(setup.model.uiState.value.product)
    }

    @Test
    fun reflectsOwnershipAndPriceAndPersistsConfirmedOwnership() = runTest(UnconfinedTestDispatcher()) {
        val setup = setup()
        setup.gateway.product.value = ProProductInfo("com.zdmgold.orlumen.pro", "USD 31.99")
        setup.gateway.ownership.value = Ownership.OWNED
        assertTrue(setup.model.uiState.value.isPro)
        assertEquals("USD 31.99", setup.model.uiState.value.product?.formattedPrice)
        assertTrue(setup.prefs.preferences.first().isProUser)
    }

    @Test
    fun purchaseEventsAreExposedUntilConsumed() = runTest(UnconfinedTestDispatcher()) {
        val setup = setup()
        setup.gateway.events.tryEmit(PurchaseEvent.PENDING)
        assertEquals(PurchaseEvent.PENDING, setup.model.uiState.value.lastEvent)
        setup.model.consumeEvent()
        assertNull(setup.model.uiState.value.lastEvent)
    }

    @Test
    fun restoreAsksBillingToRefresh() = runTest(UnconfinedTestDispatcher()) {
        val setup = setup()
        setup.model.restore()
        assertEquals(1, setup.gateway.refreshCalls)
    }
}
