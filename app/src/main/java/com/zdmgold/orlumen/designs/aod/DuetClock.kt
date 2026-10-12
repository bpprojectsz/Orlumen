package com.zdmgold.orlumen.designs.aod

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zdmgold.orlumen.R
import com.zdmgold.orlumen.core.model.DesignDescriptor
import com.zdmgold.orlumen.core.model.DesignKind
import com.zdmgold.orlumen.core.model.ParamSpec
import com.zdmgold.orlumen.core.utils.ClockParts
import com.zdmgold.orlumen.core.utils.TimeFormatter
import java.time.ZoneId
import java.time.ZoneOffset

private const val KEY_ZONE = "zone"
private const val DEFAULT_ZONE = "UTC"
private const val TIME_SIZE_SP = 60
private const val LABEL_SIZE_SP = 14
private const val MARKER_SIZE_SP = 16
private const val GROUP_GAP_DP = 28
private const val SECOND_ALPHA = 0.92f

private val zoneLabels = listOf(
    "UTC" to R.string.tz_utc,
    "Europe/London" to R.string.tz_london,
    "Europe/Paris" to R.string.tz_paris,
    "America/New_York" to R.string.tz_new_york,
    "America/Los_Angeles" to R.string.tz_los_angeles,
    "Asia/Dubai" to R.string.tz_dubai,
    "Asia/Kolkata" to R.string.tz_kolkata,
    "Asia/Tokyo" to R.string.tz_tokyo,
    "Africa/Lagos" to R.string.tz_lagos,
    "Australia/Sydney" to R.string.tz_sydney
)

/** Local time over a second time zone chosen in the editor. */
object DuetClock : AodDesign {
    override val descriptor = DesignDescriptor(
        id = "duet",
        kind = DesignKind.AOD,
        nameRes = R.string.aod_duet,
        isPro = false,
        params = listOf(
            ParamSpec.Choice(
                key = KEY_ZONE,
                labelRes = R.string.param_zone,
                options = zoneLabels.map { (id, label) -> ParamSpec.Option(id, label) },
                defaultOptionId = DEFAULT_ZONE
            )
        )
    )

    @Composable
    override fun Clock(input: AodClockInput, modifier: Modifier) {
        val zoneId = input.choice(KEY_ZONE, DEFAULT_ZONE)
        val zone = remember(zoneId) { runCatching { ZoneId.of(zoneId) }.getOrDefault(ZoneOffset.UTC) }
        val other = input.instant.atZone(zone)
        val otherClock = TimeFormatter.parts(other.hour, other.minute, input.use24Hour)
        val otherLabel = zoneLabels.firstOrNull { it.first == zoneId }?.second ?: R.string.tz_utc
        Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
            ClockGroup(stringResource(R.string.duet_local), input.clock, input, input.accent)
            Spacer(modifier = Modifier.height(GROUP_GAP_DP.dp))
            ClockGroup(stringResource(otherLabel), otherClock, input, Color.White.copy(alpha = SECOND_ALPHA))
        }
    }

    @Composable
    private fun ClockGroup(label: String, clock: ClockParts, input: AodClockInput, color: Color) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = label, color = input.secondary, fontSize = LABEL_SIZE_SP.sp)
            Text(
                text = "${clock.hours}${input.separator}${clock.minutes}",
                color = color,
                fontSize = TIME_SIZE_SP.sp,
                fontWeight = FontWeight(input.config.clockWeight)
            )
            clock.marker?.let { Text(text = it, color = input.secondary, fontSize = MARKER_SIZE_SP.sp) }
        }
    }
}
