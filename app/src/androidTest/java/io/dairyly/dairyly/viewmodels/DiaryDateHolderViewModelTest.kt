package io.dairyly.dairyly.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import androidx.test.platform.app.InstrumentationRegistry
import io.dairyly.dairyly.data.DairyRepository
import io.dairyly.dairyly.data.models.DiaryDateHolder
import io.dairyly.dairyly.utils.getOrAwaitValue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import java.util.*
import java.util.concurrent.TimeUnit


class DiaryDateHolderViewModelTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var testDiaryDateVm: DiaryDateViewModel

    @Mock
    lateinit var observer: Observer<List<DiaryDateHolder>>

    @Before
    fun setUp() {
        val repo = DairyRepository.getInstance(InstrumentationRegistry.getInstrumentation().targetContext)
        testDiaryDateVm = DiaryDateViewModel(repo, 1)
    }

    @Test
    fun getDateListLiveData() {
        val a = testDiaryDateVm.dateHolderListLiveData.getOrAwaitValue()
        assert(a.isNotEmpty())
    }

    @Test
    fun testDiaryDateUseCase(){
        val a = testDiaryDateVm.userDiaryUseCase
                .getGoodBadScoreInDay(1, Calendar.getInstance().time)
                .test()
                .awaitDone(1, TimeUnit.SECONDS)
                .assertValue {
                    return@assertValue it.data != null
                }
    }
}