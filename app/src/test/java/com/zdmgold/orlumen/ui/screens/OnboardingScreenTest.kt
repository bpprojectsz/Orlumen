package com.zdmgold.orlumen.ui.screens

import com.zdmgold.orlumen.ui.screens.onboarding.onboardingPages
import org.junit.Assert.assertEquals
import org.junit.Test

class OnboardingScreenTest {
    @Test
    fun introducesTheAppInThreeSteps() {
        assertEquals(3, onboardingPages.size)
    }

    @Test
    fun everyStepHasItsOwnTitleAndBody() {
        assertEquals(onboardingPages.size, onboardingPages.map { it.titleRes }.toSet().size)
        assertEquals(onboardingPages.size, onboardingPages.map { it.bodyRes }.toSet().size)
    }
}
