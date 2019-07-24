package id.apwdevs.app.catalogue.plugin

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class PluginsKtTest {

    @Test
    fun getCurrency() {
        val prefix = "$"
        val value = "12000000000000"
        val currency = getCurrency(prefix, value)
        assertEquals(currency, "$ 12.000.000.000.000")
    }

    @Test
    fun getReadableTime() {
        val expected = "2h 2m"
        val inMinute = 122
        val reality = getReadableTime(inMinute)
        assertEquals(expected, reality)
    }

}