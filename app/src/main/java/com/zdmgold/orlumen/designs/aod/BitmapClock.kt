package com.zdmgold.orlumen.designs.aod

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zdmgold.orlumen.R
import com.zdmgold.orlumen.core.model.DesignDescriptor
import com.zdmgold.orlumen.core.model.DesignKind
import com.zdmgold.orlumen.core.model.ParamSpec

private const val KEY_PIXEL = "pixel"
private const val MIN_PIXEL = 4f
private const val MAX_PIXEL = 12f
private const val DEFAULT_PIXEL = 7f
private const val GLYPH_ROWS = 7
private const val DIGIT_COLUMNS = 5
private const val COLON_COLUMNS = 1
private const val GAP_COLUMNS = 1
private const val TOTAL_COLUMNS = 4 * DIGIT_COLUMNS + COLON_COLUMNS + 5 * GAP_COLUMNS
private const val CELL_PADDING_SHARE = 0.08f
private const val MARKER_SIZE_SP = 16

private val digitGlyphs = mapOf(
    '0' to listOf(".###.", "#...#", "#..##", "#.#.#", "##..#", "#...#", ".###."),
    '1' to listOf("..#..", ".##..", "..#..", "..#..", "..#..", "..#..", ".###."),
    '2' to listOf(".###.", "#...#", "....#", "...#.", "..#..", ".#...", "#####"),
    '3' to listOf(".###.", "#...#", "....#", "..##.", "....#", "#...#", ".###."),
    '4' to listOf("...#.", "..##.", ".#.#.", "#..#.", "#####", "...#.", "...#."),
    '5' to listOf("#####", "#....", "####.", "....#", "....#", "#...#", ".###."),
    '6' to listOf("..##.", ".#...", "#....", "####.", "#...#", "#...#", ".###."),
    '7' to listOf("#####", "....#", "...#.", "..#..", ".#...", ".#...", ".#..."),
    '8' to listOf(".###.", "#...#", "#...#", ".###.", "#...#", "#...#", ".###."),
    '9' to listOf(".###.", "#...#", "#...#", ".####", "....#", "...#.", ".##..")
)

private val colonGlyph = listOf(".", ".", "#", ".", "#", ".", ".")

/** Rows of a 5x7 pixel glyph for a digit or ':'; each row is '#' for lit and '.' for dark. */
internal fun bitmapGlyph(character: Char): List<String> =
    if (character == ':') colonGlyph else digitGlyphs[character] ?: digitGlyphs.getValue('0')

/** Retro pixel digits drawn cell by cell, so the pixel size is adjustable. */
object BitmapClock : AodDesign {
    override val descriptor = DesignDescriptor(
        id = "bitmap",
        kind = DesignKind.AOD,
        nameRes = R.string.aod_bitmap,
        isPro = false,
        params = listOf(ParamSpec.Slider(KEY_PIXEL, R.string.param_pixel, MIN_PIXEL, MAX_PIXEL, DEFAULT_PIXEL))
    )

    @Composable
    override fun Clock(input: AodClockInput, modifier: Modifier) {
        val pixel = input.extra(KEY_PIXEL, DEFAULT_PIXEL).coerceIn(MIN_PIXEL, MAX_PIXEL)
        val text = input.clock.hours + ":" + input.clock.minutes
        Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
            Canvas(modifier = Modifier.size(width = (TOTAL_COLUMNS * pixel).dp, height = (GLYPH_ROWS * pixel).dp)) {
                val cell = pixel.dp.toPx()
                val padding = cell * CELL_PADDING_SHARE
                var column = 0
                text.forEach { character ->
                    val rows = bitmapGlyph(character)
                    rows.forEachIndexed { rowIndex, row ->
                        row.forEachIndexed { columnIndex, mark ->
                            if (mark == '#') {
                                drawRect(
                                    color = input.accent,
                                    topLeft = Offset(
                                        (column + columnIndex) * cell + padding,
                                        rowIndex * cell + padding
                                    ),
                                    size = Size(cell - 2f * padding, cell - 2f * padding)
                                )
                            }
                        }
                    }
                    column += rows.first().length + GAP_COLUMNS
                }
            }
            input.clock.marker?.let { Text(text = it, color = input.secondary, fontSize = MARKER_SIZE_SP.sp) }
        }
    }
}
