package io.dairyly.dairyly.data

import android.content.Context
import io.dairyly.dairyly.data.models.DiaryEntry
import io.dairyly.dairyly.data.models.User
import io.reactivex.Flowable
import io.reactivex.Single
import net.andreinc.mockneat.MockNeat
import java.util.*

open class DairyRepository(context: Context) {

    internal open val database = lazy {
        val a = AppDatabase.getInstance(context)
        a.populateDatabase(DairylyGenerator(
                MockNeat.threadLocal(), 10, 100, 1000, 100, 100))
        a
    }

    fun getUser(userId: Int): Flowable<User> {
        return database.value.userInfoDao().getRowById(userId)
    }

    fun identifyLatestDiaryEntryId(): Single<Int> {
        return database.value.dairyEntryDao().getLatestDiaryEntryId()
    }

    fun listAllDairyEntriesById(entry: Int): Flowable<DiaryEntry>{
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
}

class TestDairyRepository(context: Context, callback: (AppDatabase) -> Unit) : DairyRepository(context){
    override val database = lazy{
        val a = AppDatabase.getInstance(context)
        callback(a)
        a
    }
}