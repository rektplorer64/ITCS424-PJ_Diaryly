package io.dairyly.dairyly.utils

import junit.framework.Assert.assertEquals
import org.junit.Test
import java.util.*

class DateUtilKtTest {

    @Test
    fun getDayRange() {
        val cal = Calendar.getInstance()
        val time = cal.time.getDayRange()
        val time2 = cal.time.addDays(7).getDayRange()

        val t = time[0]
        val t2 = time2[0]

        val delta = (t2.time - t.time) / 86400000
        assertEquals(7, delta)
    }
}