package com.zdmgold.orlumen.core.repository

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.zdmgold.orlumen.core.datastore.PreferencesRepository
import com.zdmgold.orlumen.platform.monetization.Ownership
import com.zdmgold.orlumen.testing.FakeGateway
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

@OptIn(ExperimentalCoroutinesApi::class)
class EntitlementRepositoryTest {
    @get:Rule
    val folder = TemporaryFolder()

    private fun preferences(scope: TestScope): PreferencesRepository {
        val store = PreferenceDataStoreFactory.create(scope = scope.backgroundScope) {
            folder.newFile("entitlement.preferences_pb")
        }
        return PreferencesRepository(store)
    }

    @Test
    fun confirmedOwnershipOverridesTheCachedAnswer() {
        assertTrue(resolveEntitlement(cachedIsPro = false, ownership = Ownership.OWNED))
        assertFalse(resolveEntitlement(cachedIsPro = true, ownership = Ownership.NOT_OWNED))
    }

    @Test
    fun unknownOwnershipFallsBackToTheCache() {
        assertTrue(resolveEntitlement(cachedIsPro = true, ownership = Ownership.UNKNOWN))
        assertFalse(resolveEntitlement(cachedIsPro = false, ownership = Ownership.UNKNOWN))
    }

    @Test
    fun isProFollowsBillingAndTheCache() = runTest(UnconfinedTestDispatcher()) {
        val gateway = FakeGateway()
        val prefs = preferences(this)
        val repository = EntitlementRepository(gateway, prefs)
        assertFalse(repository.isPro.first())

        prefs.setProUser(true)
        assertTrue(repository.isPro.first())

        gateway.ownership.value = Ownership.NOT_OWNED
        assertFalse(repository.isPro.first())

        gateway.ownership.value = Ownership.OWNED
        prefs.setProUser(false)
        assertTrue(repository.isPro.first())
    }

    @Test
    fun confirmedOwnershipIsPersistedAndUnknownIsIgnored() = runTest(UnconfinedTestDispatcher()) {
        val gateway = FakeGateway()
        val prefs = preferences(this)
        val repository = EntitlementRepository(gateway, prefs)
        backgroundScope.launch { repository.persistConfirmedOwnership() }

        assertFalse(prefs.preferences.first().isProUser)
        gateway.ownership.value = Ownership.OWNED
        assertTrue(prefs.preferences.first().isProUser)
        gateway.ownership.value = Ownership.UNKNOWN
        assertTrue(prefs.preferences.first().isProUser)
        gateway.ownership.value = Ownership.NOT_OWNED
        assertFalse(prefs.preferences.first().isProUser)
    }

    @Test
    fun connectAndRefreshAreForwarded() = runTest(UnconfinedTestDispatcher()) {
        val gateway = FakeGateway()
        val repository = EntitlementRepository(gateway, preferences(this))
        repository.connect()
        repository.refresh()
        assertEquals(1, gateway.connectCalls)
        assertEquals(1, gateway.refreshCalls)
    }
}
