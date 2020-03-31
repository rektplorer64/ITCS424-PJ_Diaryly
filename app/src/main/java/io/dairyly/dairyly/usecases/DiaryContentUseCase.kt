package io.dairyly.dairyly.usecases

import io.dairyly.dairyly.data.DairyRepository
import io.dairyly.dairyly.data.Resource
import io.dairyly.dairyly.data.models.DiaryDateHolder
import io.dairyly.dairyly.data.models.DiaryEntry
import io.dairyly.dairyly.data.models.DiaryEntryBlockInfo
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

    fun getOneDiaryEntry(entryId: Int): Flowable<Resource<DiaryEntry>>{
        return RxUseCaseProcedure(repo.getDairyEntryById(entryId), null).proceed()
    }

    fun getAllDiaryEntriesByDateHolder(userId: Int, dateHolder: DiaryDateHolder): Flowable<Resource<List<DiaryEntry>>> {
        return RxUseCaseProcedure(repo.getDiaryEntriesByDate(userId, dateHolder.date), null).proceed()
    }
}