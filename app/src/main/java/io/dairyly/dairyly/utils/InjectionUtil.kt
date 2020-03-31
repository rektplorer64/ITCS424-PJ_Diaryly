@file:Suppress("UNCHECKED_CAST")

package io.dairyly.dairyly.utils

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.dairyly.dairyly.data.DairyRepository
import io.dairyly.dairyly.data.models.DiaryDateHolder
import io.dairyly.dairyly.viewmodels.DiaryContentViewModel
import io.dairyly.dairyly.viewmodels.DiaryDateViewModel
import io.dairyly.dairyly.viewmodels.DiaryViewModel

class AppViewModelFactory(private val repo: DairyRepository, val userId: Int,
                          private val diaryEntryId: Int = -1, private val dateHolder: DiaryDateHolder?= null) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return when(modelClass) {
            DiaryViewModel::class.java     -> DiaryViewModel(repo, userId) as T
            DiaryDateViewModel::class.java -> DiaryDateViewModel(repo, userId) as T
            else                           -> DiaryContentViewModel(repo, userId,
                                                                    diaryEntryId, dateHolder!!) as T
        }
    }
}

inline fun <reified T : ViewModel?> viewModelInjectionHelper(activity: FragmentActivity,
                                                             userId: Int,
                                                             diaryEntryId: Int = -1, dateHolder: DiaryDateHolder?= null): T {
    val repo = activity.let { DairyRepository.getInstance(it) }
    return ViewModelProvider(activity, AppViewModelFactory(repo, userId, diaryEntryId, dateHolder)).get(
            T::class.java)
}

inline fun <reified T : ViewModel?> viewModelInjectionHelper(activity: FragmentActivity,
                                                             key: String, userId: Int,
                                                             diaryEntryId: Int = -1, dateHolder: DiaryDateHolder?= null): T {
    val repo = activity.let { DairyRepository.getInstance(it) }
    return ViewModelProvider(activity, AppViewModelFactory(repo, userId, diaryEntryId, dateHolder)).get(key,
                                                                                            T::class.java)
}