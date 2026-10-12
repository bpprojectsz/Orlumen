package com.zdmgold.orlumen.designs.aod

import androidx.annotation.StringRes
import com.zdmgold.orlumen.R

data class AodSymbol(val id: String, val glyph: String, @StringRes val labelRes: Int)

object AodSymbolSet {
    val symbols: List<AodSymbol> = listOf(
        AodSymbol("diamond", "\u2726", R.string.symbol_diamond),
        AodSymbol("heart", "\u2665", R.string.symbol_heart),
        AodSymbol("star", "\u2605", R.string.symbol_star),
        AodSymbol("moon", "\u263E", R.string.symbol_moon),
        AodSymbol("bolt", "\u26A1", R.string.symbol_bolt),
        AodSymbol("note", "\u266A", R.string.symbol_note),
        AodSymbol("sun", "\u2600", R.string.symbol_sun),
        AodSymbol("flower", "\u273F", R.string.symbol_flower)
    )

    fun byId(id: String?): AodSymbol? = symbols.firstOrNull { it.id == id }
}
