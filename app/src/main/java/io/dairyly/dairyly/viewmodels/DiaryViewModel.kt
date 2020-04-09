package io.dairyly.dairyly.viewmodels

import androidx.lifecycle.*
import io.dairyly.dairyly.data.DairyRepository
import io.dairyly.dairyly.data.Resource
import io.dairyly.dairyly.data.models.DiaryDateHolder
import io.dairyly.dairyly.data.models.DiaryEntry
import io.dairyly.dairyly.data.models.User
import io.dairyly.dairyly.models.DiaryRepo
import io.dairyly.dairyly.usecases.UserDiaryUseCase

class DiaryViewModel(repository: DairyRepository, rawUserId: Int) : ViewModel(){

    private val dairyUseCase = UserDiaryUseCase(repository)

    private val dateHolderLiveData: MutableLiveData<DiaryDateHolder> = MutableLiveData()

    private val userId = MutableLiveData(rawUserId)
    private val user: LiveData<Resource<User>> by lazy {
        Transformations.switchMap(userId) {
            LiveDataReactiveStreams.fromPublisher(dairyUseCase.getUser(it))
        }
    }

    val listEntriesByDateHolder: LiveData<Resource<List<DiaryEntry>>> by lazy {
        Transformations.switchMap(user){user ->
            Transformations.switchMap(dateHolderLiveData){holder ->
                LiveDataReactiveStreams.fromPublisher(dairyUseCase.getDiaryEntriesInDay(user.data?.detail!!.userId, holder.date))
                // LiveDataReactiveStreams.fromPublisher(dairyUseCase.getDiaryEntriesInDay(user.data?.detail!!.userId, holder.date))
            }
        }
    }

    fun postDateHolder(dateHolder: DiaryDateHolder){
        dateHolderLiveData.postValue(dateHolder)
    }

    val listAllEntries: LiveData<Resource<List<io.dairyly.dairyly.models.data.DiaryEntry>>> by lazy {
        LiveDataReactiveStreams.fromPublisher(dairyUseCase.getDiaryEntry())
    }
}