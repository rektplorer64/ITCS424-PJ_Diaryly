package io.dairyly.dairyly.usecases

import android.content.Context
import android.util.Log
import io.dairyly.dairyly.models.data.Resource
import io.dairyly.dairyly.data.models.DiaryDateHolder
import io.dairyly.dairyly.models.DiaryRepo
import io.dairyly.dairyly.models.data.DiaryEntry
import io.dairyly.dairyly.models.data.Profile
import io.dairyly.dairyly.utils.getDayRange
import io.reactivex.Flowable
import io.reactivex.Single
import java.util.*

object UserDiaryUseCase {

    private val LOG_TAG = this::class.java.simpleName

    fun getDiaryEntry(): Flowable<Resource<List<DiaryEntry>>>{
        return RxUseCaseProcedure(DiaryRepo.retrieveAllEntry(), null).proceed()
    }

    fun getDiaryEntriesInDay(day: Date): Flowable<Resource<List<DiaryEntry>>> {
        val range = day.getDayRange()
        Log.d(this::class.java.simpleName, "Date Range: $range")
        return RxUseCaseProcedure(DiaryRepo.reactivelyRetrieveEntriesInTimeRange(range.first, range.second), null).proceed()
    }

    fun getUserProfile(): Flowable<Resource<Profile>>{
        return RxUseCaseProcedure(DiaryRepo.reactivelyRetrieveProfileInfo(), null).proceed()
    }

    fun searchDiaryByTitle(title: String): Flowable<Resource<List<DiaryEntry>>> {
        return RxUseCaseProcedure(DiaryRepo.reactivelyRetrieveEntryByTitle(title), null).proceed()
    }

    fun deleteDiaryEntry(diaryEntry: DiaryEntry,
                         context: Context): Single<List<Resource<Boolean>>> {
        return RxSingleUseCaseProcedure(DiaryRepo.deleteAnEntry(diaryEntry, context), null).proceed()
    }

    fun getGoodBadScoreInTimeRange(dayStart: Date, dayEnd: Date): Flowable<Resource<List<DiaryDateHolder>>> {
        return RxUseCaseProcedure(DiaryRepo.identifyGoodBadScoreListInRange(dayStart, dayEnd), null).proceed()
    }
}
