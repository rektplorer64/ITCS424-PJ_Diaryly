package io.dairyly.dairyly.viewmodels

import androidx.lifecycle.*
import io.dairyly.dairyly.data.DairyRepository
import io.dairyly.dairyly.data.Resource
import io.dairyly.dairyly.data.models.DiaryEntry
import io.dairyly.dairyly.data.models.User
import io.dairyly.dairyly.usecases.UserDiaryUseCase

public class DiaryViewModel(repository: DairyRepository, rawUserId: Int) : ViewModel(){


    private val dairyUseCase = UserDiaryUseCase(repository)

    private val userId = MutableLiveData(rawUserId)
    private val user: LiveData<Resource<User>> by lazy {
        Transformations.switchMap(userId) {
            LiveDataReactiveStreams.fromPublisher(dairyUseCase.getUser(it))
        }
    }

    val listAllDiaryEntries: LiveData<Resource<List<DiaryEntry>>> by lazy {
        Transformations.switchMap(user){
            LiveDataReactiveStreams.fromPublisher(dairyUseCase.getUserDairyEntries(it.data?.detail!!.userId))
        }
    }
}