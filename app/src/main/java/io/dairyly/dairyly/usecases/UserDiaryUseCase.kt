package io.dairyly.dairyly.usecases

import android.util.Log
import io.dairyly.dairyly.data.DairyRepository
import io.dairyly.dairyly.data.Resource
import io.dairyly.dairyly.data.models.DiaryDateHolder
import io.dairyly.dairyly.data.models.DiaryEntry
import io.dairyly.dairyly.data.models.DiaryEntryInfo
import io.dairyly.dairyly.data.models.User
import io.dairyly.dairyly.models.DiaryRepo
import io.dairyly.dairyly.utils.getDayRange
import io.reactivex.Flowable
import io.reactivex.Single
import java.util.*

class UserDiaryUseCase(private val repo: DairyRepository) {

    private val LOG_TAG = this::class.java.simpleName

    fun getUser(userId: Int): Flowable<Resource<User>> {
        return RxUseCaseProcedure(repo.getUser(userId), null).proceed()
    }

    fun getUserDairyEntries(userId: Int): Flowable<Resource<List<DiaryEntry>>> {
        return RxUseCaseProcedure(repo.listAllDairyEntriesByUserId(userId), null).proceed()
    }

    fun getLatestDiaryEntryId(): Single<Int> {
        return repo.identifyLatestDiaryEntryId()
    }

    suspend fun addOneDairyEntry(entry: DiaryEntryInfo): Resource<List<Long>> {
        return SuspendingUseCaseProcedure({
                                              repo.database.value.dairyEntryDao()
                                                      .insert(entry)
                                          }, {
                                              it.message
                                          })
                .proceed()
    }

    suspend fun updateOneDairyEntry(entry: DiaryEntryInfo): Resource<Int> {
        return SuspendingUseCaseProcedure({
                                              repo.database.value.dairyEntryDao()
                                                      .update(entry)
                                          }, {
                                              it.message
                                          })
                .proceed()
        // return repo.database.value.dairyEntryDao().update(entry)
    }

    fun getDiaryEntry(): Flowable<Resource<List<io.dairyly.dairyly.models.data.DiaryEntry>>>{
        return RxUseCaseProcedure(DiaryRepo.retrieveAllEntry(), null).proceed()
    }

    fun getGoodBadScoreInDay(userId: Int, day: Date): Flowable<Resource<Int>> {
        val range = day.getDayRange()
        return RxUseCaseProcedure(repo.identifyTotalGoodBadScoreInRange(userId, range[0], range[1]),
                                  null).proceed()
    }

    fun getDiaryEntriesInDay(userId: Int, day: Date): Flowable<Resource<List<DiaryEntry>>> {
        val range = day.getDayRange()
        Log.d(this::class.java.simpleName, "Date Range: $range")
        return RxUseCaseProcedure(repo.listAllDairyEntriesInRange(userId, range[0], range[1]),
                                  null).proceed()
    }

    fun getGoodBadScoreInDayRange(userId: Int, dayStart: Date,
                                  dayEnd: Date): Flowable<Resource<List<DiaryDateHolder>>> {
        val rangeDayStart = dayStart.getDayRange()
        val rangeDayEnd = dayEnd.getDayRange()
        Log.d("GoodBad Score", "query date range [${rangeDayStart[0]} (${rangeDayStart[0].time}), ${rangeDayEnd[1]} (${rangeDayEnd[1].time})]")
        return RxUseCaseProcedure(repo
                                          .identifyGoodBadScoreListInRange(userId,
                                                                           rangeDayStart[0],
                                                                           rangeDayEnd[1])
        ) {
            Log.d(LOG_TAG, "GoodBad Before Transforming: $it")
            val a = it.map {elem ->
                val timeOriginal = elem.date.time
                val time = elem.date.time * 86400000
                val calendar = Calendar.getInstance().apply {
                    timeInMillis = time
                }

                elem.date = calendar.time
                elem
            }
            Log.d(LOG_TAG, "GoodBad After Transforming: $a")
            a
        }.proceed()
    }
}