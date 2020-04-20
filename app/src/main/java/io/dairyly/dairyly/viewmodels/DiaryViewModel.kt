package io.dairyly.dairyly.viewmodels

import androidx.lifecycle.*
import io.dairyly.dairyly.data.Resource
import io.dairyly.dairyly.data.models.DiaryDateHolder
import io.dairyly.dairyly.usecases.UserDiaryUseCase.getDiaryEntriesInDay
import io.dairyly.dairyly.usecases.UserDiaryUseCase.getDiaryEntry

class DiaryViewModel : ViewModel() {

    private val dateHolderLiveData: MutableLiveData<DiaryDateHolder> = MutableLiveData()

    val entryListByDateHolder: LiveData<Resource<List<io.dairyly.dairyly.models.data.DiaryEntry>>> by lazy {
        Transformations.switchMap(dateHolderLiveData) { holder ->
            LiveDataReactiveStreams.fromPublisher(getDiaryEntriesInDay(holder.date))
        }
    }

    fun setDateHolder(dateHolder: DiaryDateHolder) {
        dateHolderLiveData.postValue(dateHolder)
    }

    val listAllEntries: LiveData<Resource<List<io.dairyly.dairyly.models.data.DiaryEntry>>> by lazy {
        LiveDataReactiveStreams.fromPublisher(getDiaryEntry())
    }
}