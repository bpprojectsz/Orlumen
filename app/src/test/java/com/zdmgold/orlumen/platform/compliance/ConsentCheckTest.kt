package com.zdmgold.orlumen.platform.compliance

import org.junit.Assert.assertEquals
import org.junit.Test

class ConsentCheckTest {
    @Test
    fun aCheckWithoutAnErrorSucceeded() {
        assertEquals(ConsentCheck.Succeeded, consentCheckOf(failed = false, message = null))
    }

    @Test
    fun aFailureKeepsTheErrorText() {
        assertEquals(ConsentCheck.Failed("No network"), consentCheckOf(failed = true, message = "No network"))
    }

    @Test
    fun aFailureWithoutTextStillSaysSomething() {
        assertEquals(ConsentCheck.Failed("unknown error"), consentCheckOf(failed = true, message = null))
        assertEquals(ConsentCheck.Failed("unknown error"), consentCheckOf(failed = true, message = "  "))
    }
}
