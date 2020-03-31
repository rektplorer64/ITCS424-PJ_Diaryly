package io.dairyly.dairyly.viewmodels

import android.util.Log
import androidx.lifecycle.*
import io.dairyly.dairyly.data.DairyRepository
import io.dairyly.dairyly.data.Resource
import io.dairyly.dairyly.data.models.DiaryDateHolder
import io.dairyly.dairyly.ui.components.DAY_TIME_WINDOW
import io.dairyly.dairyly.usecases.UserDiaryUseCase
import io.dairyly.dairyly.utils.addDays
import org.apache.commons.lang3.time.DateUtils
import java.io.Serializable
import java.util.*
import kotlin.collections.HashMap
import kotlin.math.ceil

class DiaryDateViewModel(repository: DairyRepository, private val rawUserId: Int) : ViewModel() {

    private val LOG_TAG = DiaryDateViewModel::class.java.simpleName
    val userDiaryUseCase = UserDiaryUseCase(repository)

    private val today = MutableLiveData(Calendar.getInstance().time)

    val dateHolderListLiveData: LiveData<List<DiaryDateHolder>> = Transformations.switchMap(today) {
        Log.d(LOG_TAG, today.value.toString())

        var totalStartDate: Date = it.clone() as Date
        var totalEndDate: Date = it.clone() as Date
        val dateMap = arrayListOf<DiaryDateHolder>()

        val centerDayOfMonth = ceil(((DAY_TIME_WINDOW + 1) / 2).toDouble()).toInt()

        var countNegative = 0
        var countPositive = 0
        for(i in 0 until DAY_TIME_WINDOW + 1) {
            val dayProportion = (i / centerDayOfMonth).toFloat()
            val dayAmount = if(dayProportion < 1) {
                countNegative++
                -1 * countNegative
            } else {
                countPositive++
            }

            val time = DateUtils.truncate(it.addDays(dayAmount), Calendar.DATE)

            if(totalStartDate.time > time.time) {
                totalStartDate = time
            }
            if(totalEndDate.time < time.time) {
                totalEndDate = time
            }

            dateMap += DiaryDateHolder(time, 0)
        }

        Log.d(LOG_TAG, "Date Range calculation result: $dateMap")
        Log.d(LOG_TAG, "Today is $it, [$totalStartDate, $totalEndDate]")

        // MutableLiveData(dateMap)
        val allDiaryDateMapLiveData = MutableLiveData(dateMap)
        val allGoodBadScoreInTotalRange = getGoodBadScoreInDayRange(totalStartDate, totalEndDate)

        zipLiveData<ArrayList<DiaryDateHolder>, Resource<List<DiaryDateHolder>>, List<DiaryDateHolder>>(
                allDiaryDateMapLiveData, allGoodBadScoreInTotalRange) { array, dbResource ->
            // Sort date holders in ascending order

            Log.d(this::class.java.simpleName, "GoodBad Score Retrieved from DB: $dbResource")
            array.sort()
            for(dateHolderRes in dbResource.data!!){
                val targetIndex = array.indexOf(dateHolderRes)
                if(targetIndex >= 0){
                    array[targetIndex].goodBadScore = dateHolderRes.goodBadScore
                }
            }

            array

        }
    }

    private fun getGoodBadScoreInDay(day: Date): LiveData<Resource<Int>> {
        return LiveDataReactiveStreams.fromPublisher(
                userDiaryUseCase.getGoodBadScoreInDay(rawUserId, day))
    }

    private fun getGoodBadScoreInDayRange(dayStart: Date, dayEnd: Date): LiveData<Resource<List<DiaryDateHolder>>> {
        return LiveDataReactiveStreams
                .fromPublisher(
                        userDiaryUseCase.getGoodBadScoreInDayRange(rawUserId, dayStart, dayEnd))
    }
}

fun <A, B, C> zipLiveData(a: LiveData<A>, b: LiveData<B>, predicate: (A, B) -> C): LiveData<C> {
    return MediatorLiveData<C>().apply {
        var lastA: A? = null
        var lastB: B? = null

        fun update() {
            val localLastA = lastA
            val localLastB = lastB
            if(localLastA != null && localLastB != null)
                this.value = predicate(localLastA, localLastB)
        }

        addSource(a) {
            lastA = it
            update()
        }
        addSource(b) {
            lastB = it
            update()
        }
    }
}