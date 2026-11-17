package com.zdmgold.orlumen.ui.screens.onboarding

import com.zdmgold.orlumen.R
import org.junit.Assert.assertEquals
import org.junit.Test

class OnboardingPagesTest {
    @Test
    fun hasFourPagesInOrder() {
        assertEquals(
            listOf(
                R.string.onboarding_title_1,
                R.string.onboarding_title_clock,
                R.string.onboarding_title_2,
                R.string.onboarding_title_3
            ),
            onboardingPages.map { it.titleRes }
        )
    }

    @Test
    fun privacyPageStaysLast() {
        assertEquals(R.string.onboarding_body_3, onboardingPages.last().bodyRes)
    }
}
