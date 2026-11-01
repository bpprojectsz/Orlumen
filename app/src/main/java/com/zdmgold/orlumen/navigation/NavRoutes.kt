package com.zdmgold.orlumen.navigation

object NavRoutes {
    const val ARG_DESIGN_ID = "designId"
    const val ONBOARDING = "onboarding"
    const val PERMISSIONS = "permissions"
    const val HOME = "home"
    const val DIAGNOSTICS = "diagnostics"
    const val ABOUT = "about"
    const val VISUALIZER_GALLERY = "visualizer_gallery"
    const val VISUALIZER_EDITOR = "visualizer_editor/{$ARG_DESIGN_ID}"
    const val AOD_GALLERY = "aod_gallery"
    const val AOD_EDITOR = "aod_editor/{$ARG_DESIGN_ID}"
    const val PALETTES = "palettes"

    fun visualizerEditor(designId: String) = "visualizer_editor/$designId"

    fun aodEditor(designId: String) = "aod_editor/$designId"
}
