package io.dairyly.dairyly

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.dairyly.dairyly.data.DairyRepository
import io.dairyly.dairyly.data.DairylyGenerator
import io.dairyly.dairyly.data.models.DiaryEntryInfo
import io.dairyly.dairyly.data.populateDatabase
import io.dairyly.dairyly.usecases.UserDiaryUseCase
import kotlinx.coroutines.runBlocking
import net.andreinc.mockneat.MockNeat
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*
import java.util.concurrent.TimeUnit


@RunWith(AndroidJUnit4::class)
class UseCaseTest {

    @Rule
    @JvmField
    var executor = InstantTaskExecutorRule()

    private lateinit var context: Context
    private lateinit var repo: DairyRepository

    private lateinit var userDiaryUseCase: UserDiaryUseCase


    @Before
    fun initializeDatabase() {
        context = InstrumentationRegistry.getInstrumentation().targetContext

        // val db = Room.inMemoryDatabaseBuilder(
        //         InstrumentationRegistry.getInstrumentation().targetContext, AppDatabase::class.java)
        //         .build()

        repo = DairyRepository.getInstance(context)
        repo.populateDatabase(DairylyGenerator(
                MockNeat.threadLocal(), 10, 100, 100, 100, 100))

        userDiaryUseCase = UserDiaryUseCase(repo)
    }

    @Test
    fun addOneDairyEntry() {
        runBlocking {
            val now = Calendar.getInstance().time
            val dairyEntry = DiaryEntryInfo(0, 4, now, now, DiaryEntryInfo.GoodBad.GOOD)
            val res = userDiaryUseCase.addOneDairyEntry(dairyEntry)

            println(res.message)

            val list = userDiaryUseCase.getUserDairyEntries(1).blockingFirst().data?.get(0)
            println(list)

            val latestEntryId = userDiaryUseCase.getLatestDiaryEntryId().blockingGet()

            assertEquals(latestEntryId.toLong(), res.data?.get(0))
        }
    }

    @Test
    fun readDairyByUserId(){
        userDiaryUseCase.getUserDairyEntries(4).test().awaitDone(1, TimeUnit.SECONDS)
                .assertValue {res ->
                    var count = 0
                    res.data?.forEach {
                        if(it.info.userId == 4) {
                            count++
                        }
                    }
                    count == res.data?.size
                }
    }
}