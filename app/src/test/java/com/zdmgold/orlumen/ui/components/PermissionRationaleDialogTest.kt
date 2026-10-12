package com.zdmgold.orlumen.ui.components

import org.junit.Assert.assertEquals
import org.junit.Test

class PermissionRationaleDialogTest {
    @Test
    fun everyPermissionKindHasItsOwnTitleAndMessage() {
        val rationales = PermissionKind.entries.map { rationaleFor(it) }
        assertEquals(PermissionKind.entries.size, rationales.map { it.titleRes }.toSet().size)
        assertEquals(PermissionKind.entries.size, rationales.map { it.messageRes }.toSet().size)
    }

    @Test
    fun coversTheSevenPermissionsTheAppAsksFor() {
        assertEquals(7, PermissionKind.entries.size)
    }
}
