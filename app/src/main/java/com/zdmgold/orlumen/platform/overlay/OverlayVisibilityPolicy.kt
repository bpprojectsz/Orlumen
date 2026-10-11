package com.zdmgold.orlumen.platform.overlay

enum class AppCategoryHint {
    GAME,
    VIDEO,
    OTHER
}

data class VisibilityInput(
    val foregroundPackage: String?,
    val category: AppCategoryHint,
    val isLandscape: Boolean,
    val ownPackage: String
)

data class VisibilityRules(val hideOverFullscreen: Boolean, val allowedPackages: Set<String>)

object OverlayVisibilityPolicy {
    fun isVisible(input: VisibilityInput, rules: VisibilityRules): Boolean {
        val fullscreen = input.category != AppCategoryHint.OTHER || input.isLandscape
        val hiddenByFullscreen = rules.hideOverFullscreen && fullscreen
        val foreground = input.foregroundPackage
        val outsideAllowList = rules.allowedPackages.isNotEmpty() &&
            foreground != null &&
            foreground != input.ownPackage &&
            foreground !in rules.allowedPackages
        return !hiddenByFullscreen && !outsideAllowList
    }
}
