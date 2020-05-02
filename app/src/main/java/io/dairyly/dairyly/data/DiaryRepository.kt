package io.dairyly.dairyly.data

import android.content.Context
import io.dairyly.dairyly.data.models.DiaryDateHolder
import io.dairyly.dairyly.data.models.DiaryEntry
import io.dairyly.dairyly.data.models.User
import io.reactivex.Flowable
import io.reactivex.Single
import java.util.*

open class DairyRepository private constructor(context: Context) {

    companion object{
        @Volatile
        private var INSTANCE: DairyRepository? = null

        fun getInstance(context: Context): DairyRepository = INSTANCE ?: synchronized(this){
            INSTANCE?: DairyRepository(context).also { INSTANCE = it }
        }
    }

    internal open val database = lazy {
        val a = AppDatabase.getInstance(context)
        // a.populateDatabase(DairylyGenerator(
        //         MockNeat.threadLocal(), 1, 1, 1000, 1000, 10))
        a
    }

    fun getUser(userId: Int): Flowable<User> {
        return database.value.userInfoDao().getRowById(userId)
    }

    fun identifyLatestDiaryEntryId(): Single<Int> {
        return database.value.dairyEntryDao().getLatestDiaryEntryId()
    }

    fun getDairyEntryById(entry: Int): Flowable<DiaryEntry>{
        return database.value.dairyEntryDao().getRowById(entry)
    }

    fun listAllDairyEntriesByUserId(userId: Int): Flowable<List<DiaryEntry>> {
        return database.value.dairyEntryDao().getRowByUserId(userId)
    }

    fun listAllDairyEntriesInRange(userId: Int, dateStart: Date,
                                   dateEnd: Date): Flowable<List<DiaryEntry>> {
        if(dateStart.time >= dateEnd.time) {
            throw IllegalArgumentException(
                    "The first argument should be lesser than the second one!")
        }
        return database.value.dairyEntryDao()
                .getRowsByTimeRange(userId, dateStart.time, dateEnd.time)
    }

    fun identifyTotalGoodBadScoreInRange(userId: Int, dateStart: Date,
                                         dateEnd: Date): Flowable<Int>{
        if(dateStart.time >= dateEnd.time) {
            throw IllegalArgumentException(
                    "The first argument should be lesser than the second one!")
        }
        return database.value.dairyEntryDao().getTotalGoodBadScoreInRange(userId, dateStart, dateEnd)
    }

    fun identifyGoodBadScoreListInRange(userId: Int, dateStart: Date,
                                         dateEnd: Date): Flowable<List<DiaryDateHolder>>{
        if(dateStart.time >= dateEnd.time) {
            throw IllegalArgumentException(
                    "The first argument should be lesser than the second one!")
        }
        return database.value.dairyEntryDao().getGoodBadScoreListInRange(userId, dateStart, dateEnd)
    }

    fun getDiaryEntriesByDate(userId: Int, date: Date): Flowable<List<DiaryEntry>> {
        return database.value.dairyEntryDao().getRowsByDate(userId, date)
    }
}
