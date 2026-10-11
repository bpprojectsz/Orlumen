package com.zdmgold.orlumen.platform.overlay

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OverlayVisibilityPolicyTest {
    private val own = "com.zdmgold.orlumen"

    private fun input(
        foreground: String? = "music.app",
        category: AppCategoryHint = AppCategoryHint.OTHER,
        landscape: Boolean = false
    ) = VisibilityInput(foreground, category, landscape, own)

    private fun rules(hide: Boolean = true, allowed: Set<String> = emptySet()) = VisibilityRules(hide, allowed)

    @Test
    fun visibleByDefault() {
        assertTrue(OverlayVisibilityPolicy.isVisible(input(), rules()))
    }

    @Test
    fun hidesOverGamesAndVideosWhenEnabled() {
        assertFalse(OverlayVisibilityPolicy.isVisible(input(category = AppCategoryHint.GAME), rules()))
        assertFalse(OverlayVisibilityPolicy.isVisible(input(category = AppCategoryHint.VIDEO), rules()))
    }

    @Test
    fun showsOverGamesWhenHidingIsDisabled() {
        assertTrue(OverlayVisibilityPolicy.isVisible(input(category = AppCategoryHint.GAME), rules(hide = false)))
    }

    @Test
    fun landscapeCountsAsFullscreen() {
        assertFalse(OverlayVisibilityPolicy.isVisible(input(landscape = true), rules()))
        assertTrue(OverlayVisibilityPolicy.isVisible(input(landscape = true), rules(hide = false)))
    }

    @Test
    fun allowListLimitsWhichAppsShowTheOverlay() {
        val allowed = setOf("music.app")
        assertTrue(OverlayVisibilityPolicy.isVisible(input("music.app"), rules(allowed = allowed)))
        assertFalse(OverlayVisibilityPolicy.isVisible(input("other.app"), rules(allowed = allowed)))
    }

    @Test
    fun ownAppAndUnknownForegroundAreAlwaysAllowed() {
        val allowed = setOf("music.app")
        assertTrue(OverlayVisibilityPolicy.isVisible(input(own), rules(allowed = allowed)))
        assertTrue(OverlayVisibilityPolicy.isVisible(input(null), rules(allowed = allowed)))
    }

    @Test
    fun emptyAllowListMeansEveryApp() {
        assertTrue(OverlayVisibilityPolicy.isVisible(input("anything"), rules(allowed = emptySet())))
    }
}
