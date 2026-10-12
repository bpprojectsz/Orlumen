package com.zdmgold.orlumen.designs.visualizer

import com.zdmgold.orlumen.core.model.DesignDescriptor

object VisualizerRegistry {
    val designs: List<VisualizerDesign> = listOf(
        FilamentDesign,
        TideDesign,
        StrandDesign,
        RailDesign,
        EmberDesign,
        AuroraDesign
    )

    val descriptors: List<DesignDescriptor>
        get() = designs.map { it.descriptor }

    fun byId(id: String): VisualizerDesign? = designs.firstOrNull { it.descriptor.id == id }

    fun resolve(id: String): VisualizerDesign = byId(id) ?: designs.first()
}
