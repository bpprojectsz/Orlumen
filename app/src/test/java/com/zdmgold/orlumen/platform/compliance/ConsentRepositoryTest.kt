package com.zdmgold.orlumen.platform.compliance

import com.google.android.ump.ConsentInformation
import com.zdmgold.orlumen.core.repository.adsAllowed
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ConsentRepositoryTest {
    private fun consent(canRequestAds: Boolean) = ConsentSnapshot(ConsentStatus.OBTAINED, canRequestAds, false)

    @Test
    fun adsRequireConsentAndNoProEntitlement() {
        assertTrue(adsAllowed(isPro = false, consent = consent(true)))
        assertFalse(adsAllowed(isPro = false, consent = consent(false)))
        assertFalse(adsAllowed(isPro = true, consent = consent(true)))
        assertFalse(adsAllowed(isPro = true, consent = consent(false)))
    }

    @Test
    fun startsWithNoPermissionToRequestAds() {
        assertFalse(ConsentSnapshot.initial.canRequestAds)
        assertEquals(ConsentStatus.UNKNOWN, ConsentSnapshot.initial.status)
    }

    @Test
    fun mapsEveryUmpStatus() {
        assertEquals(ConsentStatus.REQUIRED, consentStatusFrom(ConsentInformation.ConsentStatus.REQUIRED))
        assertEquals(ConsentStatus.NOT_REQUIRED, consentStatusFrom(ConsentInformation.ConsentStatus.NOT_REQUIRED))
        assertEquals(ConsentStatus.OBTAINED, consentStatusFrom(ConsentInformation.ConsentStatus.OBTAINED))
        assertEquals(ConsentStatus.UNKNOWN, consentStatusFrom(ConsentInformation.ConsentStatus.UNKNOWN))
        assertEquals(ConsentStatus.UNKNOWN, consentStatusFrom(99))
    }
}
