package com.zdmgold.orlumen.platform.aod

import android.os.BatteryManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BatteryInfoProviderTest {
    @Test
    fun percentIsLevelOverScale() {
        assertEquals(50, batteryInfoFrom(50, 100, BatteryManager.BATTERY_STATUS_DISCHARGING).percent)
        assertEquals(25, batteryInfoFrom(50, 200, BatteryManager.BATTERY_STATUS_DISCHARGING).percent)
    }

    @Test
    fun invalidReadingsGiveZeroAndClampsHold() {
        assertEquals(0, batteryInfoFrom(-1, 100, 0).percent)
        assertEquals(0, batteryInfoFrom(50, 0, 0).percent)
        assertEquals(100, batteryInfoFrom(150, 100, 0).percent)
    }

    @Test
    fun chargingAndFullCountAsCharging() {
        assertTrue(batteryInfoFrom(10, 100, BatteryManager.BATTERY_STATUS_CHARGING).isCharging)
        assertTrue(batteryInfoFrom(100, 100, BatteryManager.BATTERY_STATUS_FULL).isCharging)
        assertFalse(batteryInfoFrom(10, 100, BatteryManager.BATTERY_STATUS_DISCHARGING).isCharging)
        assertFalse(batteryInfoFrom(10, 100, BatteryManager.BATTERY_STATUS_NOT_CHARGING).isCharging)
    }
}
