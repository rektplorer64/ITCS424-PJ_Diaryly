package io.dairyly.dairyly.usecases

import android.util.Log
import io.dairyly.dairyly.data.DairyRepository
import io.dairyly.dairyly.data.Resource
import io.dairyly.dairyly.data.models.DiaryDateHolder
import io.dairyly.dairyly.data.models.DiaryEntryBlockInfo
import io.dairyly.dairyly.models.DiaryRepo
import io.reactivex.Flowable

class DiaryContentUseCase(private val repo: DairyRepository) {
    suspend fun addDairyBlockToEntry(entryId: Int,
                                     blockInfo: DiaryEntryBlockInfo): Flowable<Resource<DiaryEntryBlockInfo>> {
        val returnValue = SuspendingUseCaseProcedure(
                {
                    repo.database.value.enDairyEntryBlockInfoDao()
                            .insert(blockInfo)
                }, {
                    it.message
                }).proceed()

        val flowable = returnValue.data?.get(0)?.toInt()?.let {
            repo.database.value.enDairyEntryBlockInfoDao().getRowById(it)
        }

        return if(flowable == null) {
            Flowable.fromArray(
                    Resource.error(null,
                                   "The Data request cannot be retrieved."))
        } else {
            RxUseCaseProcedure(flowable, null).proceed()
        }
    }

    fun getOneDiaryEntry(entryId: String): Flowable<Resource<io.dairyly.dairyly.models.data.DiaryEntry>>{
        return RxUseCaseProcedure(DiaryRepo.reactivelyRetrieveAnEntryById(entryId), null).proceed()
    }

    fun getAllDiaryEntriesByDateHolder(dateHolder: DiaryDateHolder): Flowable<Resource<List<io.dairyly.dairyly.models.data.DiaryEntry>>> {
        return RxUseCaseProcedure(DiaryRepo.retrieveEntryInDay(dateHolder.date), null).proceed()
                .doAfterNext { Log.d("getAllDiaryByDH", "Getting $it") }
    }
}