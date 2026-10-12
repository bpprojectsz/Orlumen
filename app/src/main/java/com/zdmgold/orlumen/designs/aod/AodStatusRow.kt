package com.zdmgold.orlumen.designs.aod

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import com.zdmgold.orlumen.R
import com.zdmgold.orlumen.platform.aod.BatteryInfo

@Composable
fun AodStatusRow(battery: BatteryInfo, secondary: Color, modifier: Modifier = Modifier) {
    val charging = if (battery.isCharging) " \u00B7 ${stringResource(R.string.aod_charging)}" else ""
    Text(text = "${battery.percent}%$charging", color = secondary, fontSize = 14.sp, modifier = modifier)
}
