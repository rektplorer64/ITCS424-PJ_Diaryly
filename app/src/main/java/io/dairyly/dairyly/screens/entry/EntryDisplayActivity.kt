package io.dairyly.dairyly.screens.entry

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import io.dairyly.dairyly.R
import io.dairyly.dairyly.utils.viewModelInjectionHelper
import io.dairyly.dairyly.viewmodels.EntryActivityViewModel

/**
 * A container activity for the Fragments are responsible for displaying diary entry's content
 * @property LOG_TAG String String Tag string for showing Debugging Log
 */
class EntryDisplayActivity : AppCompatActivity() {

    private val LOG_TAG = this::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_entry)

        val entryActivityArgs = EntryDisplayActivityArgs.fromBundle(
                intent.extras!!)

        val entryId = entryActivityArgs.entryId
        val dateHolder = entryActivityArgs.diaryDateHolder!!

        Log.d(LOG_TAG, "The activity has received EntryID => $entryId")

        // val viewModelFactory = this.let { DairyRepository.getInstance(it) }.let {
        //     AppViewModelFactory(it, diaryEntryId = entryId)
        // }

        // val entryViewModel = ViewModelProvider(this, viewModelFactory).get(DiaryContentViewModel)

        val viewModel = viewModelInjectionHelper<EntryActivityViewModel>(this,
                                                                         diaryEntryId = entryId,
                                                                         dateHolder = dateHolder)
        viewModel.selectedDiaryEntry = entryId
    }


}
