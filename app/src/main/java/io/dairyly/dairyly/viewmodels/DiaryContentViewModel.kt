package io.dairyly.dairyly.viewmodels

import androidx.lifecycle.*
import io.dairyly.dairyly.data.DairyRepository
import io.dairyly.dairyly.data.Resource
import io.dairyly.dairyly.data.models.DiaryDateHolder
import io.dairyly.dairyly.data.models.DiaryEntry
import io.dairyly.dairyly.data.models.User
import io.dairyly.dairyly.usecases.DiaryContentUseCase
import io.dairyly.dairyly.usecases.UserDiaryUseCase

class DiaryContentViewModel(repository: DairyRepository, rawUserId: Int, rawEntryId: Int, rawDateHolder: DiaryDateHolder) :
        ViewModel() {

    private val diaryUseCase = UserDiaryUseCase(repository)
    private val diaryContentUseCase = DiaryContentUseCase(repository)

    private val userId = MutableLiveData(rawUserId)

    private val user: LiveData<Resource<User>> by lazy {
        Transformations.switchMap(userId) {
            LiveDataReactiveStreams.fromPublisher(diaryUseCase.getUser(it))
        }
    }

    private val entryId = MutableLiveData(rawEntryId)
    val entryContent: LiveData<Resource<DiaryEntry>> by lazy {
        Transformations.switchMap(entryId) {
            LiveDataReactiveStreams.fromPublisher(diaryContentUseCase.getOneDiaryEntry(it))
        }
    }

    private val dateHolder = MutableLiveData<DiaryDateHolder>(rawDateHolder)
    val dailyDiary: LiveData<Resource<List<DiaryEntry>>> by lazy {
        Transformations.switchMap(userId){userId ->
            Transformations.switchMap(dateHolder){
                LiveDataReactiveStreams.fromPublisher(diaryContentUseCase.getAllDiaryEntriesByDateHolder(userId, it))
            }
        }
    }
}