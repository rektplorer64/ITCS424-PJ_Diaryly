package io.dairyly.dairyly.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.LiveDataReactiveStreams
import androidx.lifecycle.ViewModel
import io.dairyly.dairyly.data.DairyRepository
import io.dairyly.dairyly.data.Resource
import io.dairyly.dairyly.data.models.DiaryDateHolder
import io.dairyly.dairyly.models.data.DiaryEntry
import io.dairyly.dairyly.usecases.DiaryContentUseCase

class EntryActivityViewModel(repository: DairyRepository,
                             val entryId: String, private val diaryDateHolder: DiaryDateHolder) : ViewModel() {

    private val diaryContentUseCase = DiaryContentUseCase(repository)

    // private val dateHolder = MutableLiveData(rawDateHolder)
    val dailyDiary: LiveData<Resource<List<DiaryEntry>>> by lazy {
        LiveDataReactiveStreams.fromPublisher(diaryContentUseCase.getAllDiaryEntriesByDateHolder(diaryDateHolder))
    }
}

class EntryDisplayViewModel(repository: DairyRepository, val  entryId: String) : ViewModel(){

    private val diaryContentUseCase = DiaryContentUseCase(repository)

    val entryContent: LiveData<Resource<DiaryEntry>> by lazy {
        LiveDataReactiveStreams.fromPublisher(diaryContentUseCase.getOneDiaryEntry(entryId))
    }
}