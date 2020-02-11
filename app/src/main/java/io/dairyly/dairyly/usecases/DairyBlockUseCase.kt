package io.dairyly.dairyly.usecases

import io.dairyly.dairyly.data.DairyRepository
import io.dairyly.dairyly.data.Resource
import io.dairyly.dairyly.data.models.DairyEntryBlockInfo
import io.reactivex.Flowable

sealed class DairyBlockUseCase(private val repo: DairyRepository) {
    suspend fun addDairyBlockToEntry(entryId: Int,
                                     blockInfo: DairyEntryBlockInfo): Flowable<Resource<DairyEntryBlockInfo>> {
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
}