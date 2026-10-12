package com.zdmgold.orlumen.designs.aod

import com.zdmgold.orlumen.core.model.DesignDescriptor

object AodRegistry {
    val designs: List<AodDesign> = listOf(
        OrbitClock,
        SlabClock,
        LedgerFlipClock,
        MeridianClock,
        LexiconTextClock,
        LunarClock,
        DuetClock,
        BitmapClock
    )

    val descriptors: List<DesignDescriptor>
        get() = designs.map { it.descriptor }

    fun byId(id: String): AodDesign? = designs.firstOrNull { it.descriptor.id == id }

    fun resolve(id: String): AodDesign = byId(id) ?: designs.first()
}
