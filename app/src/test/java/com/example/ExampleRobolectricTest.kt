package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.AlarmItem
import com.example.data.model.LapItem
import com.example.data.model.ThemePreset
import com.example.data.model.WorldClockCity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `verify app name resource matches FAKEEXX`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("FAKEEXX", appName)
    }

    @Test
    fun `verify alarm calculations and next ring logic`() {
        val alarm = AlarmItem(
            hour = 8,
            minute = 30,
            label = "Morning Rise",
            isEnabled = true,
            repeatDays = "2,3,4,5,6"
        )
        assertEquals("08:30", alarm.formattedTime(is24Hour = true))
        assertEquals("08:30", alarm.formattedTime(is24Hour = false))
        assertEquals("AM", alarm.amPm())
        assertTrue(alarm.getNextRingTimeMillis() > 0)
    }

    @Test
    fun `verify theme presets coverage`() {
        assertEquals(6, ThemePreset.values().size)
        assertNotNull(ThemePreset.ABYSS)
        assertNotNull(ThemePreset.OBSIDIAN)
        assertNotNull(ThemePreset.IVORY)
        assertNotNull(ThemePreset.MIDNIGHT_BLUE)
        assertNotNull(ThemePreset.FOREST)
        assertNotNull(ThemePreset.MONOCHROME)
    }

    @Test
    fun `verify stopwatch formatting`() {
        val formatted = LapItem.formatMillis(65430L)
        assertEquals("01:05.43", formatted)
    }

    @Test
    fun `verify world clock IANA offset calculation`() {
        val tokyo = WorldClockCity("Asia/Tokyo", "Tokyo", "Japan", "Asia/Tokyo")
        assertTrue(tokyo.getOffsetString().contains("UTC"))
        assertNotNull(tokyo.getDayDifference())
    }
}
