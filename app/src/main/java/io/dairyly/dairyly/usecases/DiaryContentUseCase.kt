package io.dairyly.dairyly.usecases

import android.util.Log
import io.dairyly.dairyly.data.models.DiaryDateHolder
import io.dairyly.dairyly.models.DiaryRepo
import io.dairyly.dairyly.models.data.DiaryEntry
import io.dairyly.dairyly.models.data.Resource
import io.reactivex.Flowable

object DiaryContentUseCase {

    fun getOneDiaryEntry(entryId: String): Flowable<Resource<DiaryEntry>>{
        return RxUseCaseProcedure(DiaryRepo.reactivelyRetrieveAnEntryById(entryId), null).proceed()
    }

    fun getAllDiaryEntriesByDateHolder(dateHolder: DiaryDateHolder): Flowable<Resource<List<DiaryEntry>>> {
        return RxUseCaseProcedure(DiaryRepo.retrieveEntryInDay(dateHolder.date), null).proceed()
                .doAfterNext { Log.d("getAllDiaryByDH", "Getting $it") }
    }
}