@file:Suppress("UNCHECKED_CAST")

package io.dairyly.dairyly.utils

import android.app.Application
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.dairyly.dairyly.data.DairyRepository
import io.dairyly.dairyly.data.models.DiaryDateHolder
import io.dairyly.dairyly.viewmodels.*

class AppViewModelFactory(private val repo: DairyRepository, val userId: Int = 1,
                          private val entryId: String? = null,
                          private val dateHolder: DiaryDateHolder?= null,
                          private val application: Application?= null
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return when(modelClass) {
            DiaryViewModel::class.java            -> DiaryViewModel(repo, userId) as T
            DiaryDateViewModel::class.java        -> DiaryDateViewModel(repo, userId) as T
            EntryDisplayViewModel::class.java     -> EntryDisplayViewModel(repo, entryId!!) as T
            EntryEditorViewModel::class.java      -> EntryEditorViewModel(application!!, entryId) as T
            else                                  -> EntryActivityViewModel(repo, entryId!!, dateHolder!!) as T
        }
    }
}

inline fun <reified T : ViewModel?> viewModelInjectionHelper(activity: FragmentActivity,
                                                             userId: Int = 1,
                                                             diaryEntryId: String? = null, dateHolder: DiaryDateHolder?= null): T {
    val repo = activity.let { DairyRepository.getInstance(it) }
    return ViewModelProvider(activity, AppViewModelFactory(repo, userId, diaryEntryId, dateHolder, activity.application)).get(
            T::class.java)
}

inline fun <reified T : ViewModel?> viewModelInjectionHelper(fragment: Fragment,
                                                             userId: Int = 1,
                                                             diaryEntryId: String? = null, dateHolder: DiaryDateHolder?= null): T {
    val repo = fragment.let { DairyRepository.getInstance(it.context!!) }
    return ViewModelProvider(fragment, AppViewModelFactory(repo, userId, diaryEntryId, dateHolder)).get(
            T::class.java)
}

inline fun <reified T : ViewModel?> viewModelInjectionHelper(activity: FragmentActivity,
                                                             key: String, userId: Int = 1,
                                                             diaryEntryId: String = "", dateHolder: DiaryDateHolder?= null): T {
    val repo = activity.let { DairyRepository.getInstance(it) }
    return ViewModelProvider(activity, AppViewModelFactory(repo, userId, diaryEntryId, dateHolder)).get(key,
                                                                                            T::class.java)
}