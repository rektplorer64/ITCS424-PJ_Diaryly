package io.dairyly.dairyly.data

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import net.andreinc.mockneat.MockNeat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import java.util.*


@RunWith(AndroidJUnit4::class)
class DairyRepositoryTest {

    @get:Rule
    val executor = InstantTaskExecutorRule()

    private lateinit var context: Context
    private lateinit var database: TestDairyRepository

    private val testUserId = 1

    private val m = MockNeat.threadLocal()

    @Before
    fun initializeDatabase() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        database = TestDairyRepository(context){
            it.populateDatabase(DairylyGenerator(MockNeat.threadLocal(), 10, 100, 100, 100, 100))
        }
    }

    @Test
    fun listAllDairyEntriesByUserId() {
        val list = database.listAllDairyEntriesByUserId(testUserId).blockingFirst()

        list.forEach {
            assert(it.info.userId == testUserId)
        }
    }

    @Test
    fun listAllDairyEntriesInRange() {
        val startDate = m.localDates()
                .between(m.localDates().`val`(), LocalDate.of(2020, 1, 2))
                .toUtilDate().`val`()
        val list = database.listAllDairyEntriesInRange(testUserId, startDate,
                                                       Calendar.getInstance().time).blockingFirst()
        list.forEach {
            assert(it.info.userId == testUserId)
        }
    }
}