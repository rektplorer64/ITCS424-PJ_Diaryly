package io.dairyly.dairyly

import io.dairyly.dairyly.utils.toSHA256
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun hashSHA256Test(){
        assertEquals("sadasd;sadasdsadksmdskadsa".toSHA256(), "61a0b521d657554abd19b8e3fb352c2a74d74b314b8fd4954223cbee9d9644c3")
    }
}
