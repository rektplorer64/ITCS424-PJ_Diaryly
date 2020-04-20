package io.dairyly.dairyly.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.LiveDataReactiveStreams
import androidx.lifecycle.ViewModel
import io.dairyly.dairyly.data.Resource
import io.dairyly.dairyly.data.models.DiaryDateHolder
import io.dairyly.dairyly.models.DiaryRepo
import io.dairyly.dairyly.models.data.DiaryEntry
import io.dairyly.dairyly.usecases.DiaryContentUseCase.getAllDiaryEntriesByDateHolder
import io.dairyly.dairyly.usecases.DiaryContentUseCase.getOneDiaryEntry
import io.dairyly.dairyly.usecases.UserDiaryUseCase.deleteDiaryEntry
import io.reactivex.Single

class EntryActivityViewModel(application: Application, entryId: String,
                             diaryDateHolder: DiaryDateHolder) : AndroidViewModel(application) {

    private val LOG_TAG = this::class.java.simpleName

    // private val dateHolder = MutableLiveData(rawDateHolder)
    val dailyDiary: LiveData<Resource<List<DiaryEntry>>> =
            LiveDataReactiveStreams.fromPublisher(getAllDiaryEntriesByDateHolder(diaryDateHolder))

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

    fun removeDiaryEntry(diaryEntry: DiaryEntry): Single<List<Resource<Boolean>>> {
        return deleteDiaryEntry(diaryEntry, getApplication<Application>().applicationContext)
    }

    val userProfile by lazy { LiveDataReactiveStreams.fromPublisher(DiaryRepo.reactivelyRetrieveProfileInfo()) }

}

class EntryDisplayViewModel(val entryId: String) : ViewModel(){

    val entryContent: LiveData<Resource<DiaryEntry>> by lazy {
        LiveDataReactiveStreams.fromPublisher(getOneDiaryEntry(entryId))
    }
}