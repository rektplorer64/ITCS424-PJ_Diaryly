package io.dairyly.dairyly.usecases

import io.dairyly.dairyly.data.DairyRepository
import io.dairyly.dairyly.data.Resource
import io.dairyly.dairyly.data.models.DiaryEntry
import io.dairyly.dairyly.data.models.DairyEntryInfo
import io.dairyly.dairyly.data.models.User
import io.reactivex.Flowable
import io.reactivex.Single

class UserDiaryUseCase(private val repo: DairyRepository) {

    fun getUser(userId: Int): Flowable<Resource<User>> {
        return RxUseCaseProcedure(repo.getUser(userId), null).proceed()
    }

    fun getUserDairyEntries(userId: Int): Flowable<Resource<List<DiaryEntry>>> {
        return RxUseCaseProcedure(repo.listAllDairyEntriesByUserId(userId), null).proceed()
    }

    fun getLatestDiaryEntryId(): Single<Int> {
        return repo.identifyLatestDiaryEntryId()
    }

    suspend fun addOneDairyEntry(entry: DairyEntryInfo): Resource<List<Long>> {
        return SuspendingUseCaseProcedure({
                                              repo.database.value.dairyEntryDao()
                                                      .insert(entry)
                                          }, {
                                              it.message
                                          })
                .proceed()
    }

    suspend fun updateOneDairyEntry(entry: DairyEntryInfo): Resource<Int> {
        return SuspendingUseCaseProcedure({
                                              repo.database.value.dairyEntryDao()
                                                      .update(entry)
                                          }, {
                                              it.message
                                          })
                .proceed()
        // return repo.database.value.dairyEntryDao().update(entry)
    }

}