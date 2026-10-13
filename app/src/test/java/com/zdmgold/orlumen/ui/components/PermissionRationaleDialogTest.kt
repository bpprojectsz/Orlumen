package com.zdmgold.orlumen.ui.components

import com.zdmgold.orlumen.core.model.PermissionKind
import org.junit.Assert.assertEquals
import org.junit.Test

class PermissionRationaleDialogTest {
    @Test
    fun everyPermissionKindHasItsOwnTitleAndMessage() {
        val rationales = PermissionKind.entries.map { rationaleFor(it) }
        assertEquals(PermissionKind.entries.size, rationales.map { it.first }.toSet().size)
        assertEquals(PermissionKind.entries.size, rationales.map { it.second }.toSet().size)
    }

    @Test
    fun coversTheSevenPermissionsTheAppAsksFor() {
        assertEquals(7, PermissionKind.entries.size)
    }
}
