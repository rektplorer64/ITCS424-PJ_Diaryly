package io.dairyly.dairyly.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.LiveDataReactiveStreams
import androidx.lifecycle.ViewModel
import io.dairyly.dairyly.data.DairyRepository
import io.dairyly.dairyly.data.Resource
import io.dairyly.dairyly.data.models.DiaryDateHolder
import io.dairyly.dairyly.models.data.DiaryEntry
import io.dairyly.dairyly.usecases.DiaryContentUseCase

class EntryActivityViewModel(repository: DairyRepository, entryId: String, diaryDateHolder: DiaryDateHolder) : ViewModel() {

    private val LOG_TAG = this::class.java.simpleName
    private val diaryContentUseCase = DiaryContentUseCase(repository)

    // private val dateHolder = MutableLiveData(rawDateHolder)
    val dailyDiary: LiveData<Resource<List<DiaryEntry>>> =
            LiveDataReactiveStreams.fromPublisher(diaryContentUseCase.getAllDiaryEntriesByDateHolder(diaryDateHolder)).apply {
                observeForever {
                    Log.d("Converting to LiveData", "Getting $it")
                }
            }

    var selectedDiaryEntry = entryId
    var isFirstTime = true

    fun getSelectedDiaryEntryIndex(): Int{
        val array = dailyDiary.value!!.data!!
        Log.d(LOG_TAG, "Total Array Elements => ${array.size} => ${array.map(DiaryEntry::id)}")
        var target = 0

        for(i in array.indices){
            Log.d(LOG_TAG, "Getting index => ${array[i].id}")
            Log.d(LOG_TAG, "Comparing => ${array[i].id} vs. $selectedDiaryEntry")
            if(array[i].id.contentEquals(selectedDiaryEntry)){
                target = i
                break
            }
        }
        return target
    }

}

class EntryDisplayViewModel(repository: DairyRepository, val  entryId: String) : ViewModel(){

    private val diaryContentUseCase = DiaryContentUseCase(repository)

    val entryContent: LiveData<Resource<DiaryEntry>> by lazy {
        LiveDataReactiveStreams.fromPublisher(diaryContentUseCase.getOneDiaryEntry(entryId))
    }
}