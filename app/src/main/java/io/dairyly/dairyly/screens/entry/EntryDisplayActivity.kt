package io.dairyly.dairyly.screens.entry

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.dairyly.dairyly.R
import io.dairyly.dairyly.utils.viewModelInjectionHelper
import io.dairyly.dairyly.viewmodels.EntryActivityViewModel

class EntryDisplayActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_entry)

        val entryActivityArgs = EntryDisplayActivityArgs.fromBundle(
                intent.extras!!)

        val entryId = entryActivityArgs.entryId
        val dateHolder = entryActivityArgs.diaryDateHolder!!

        // val viewModelFactory = this.let { DairyRepository.getInstance(it) }.let {
        //     AppViewModelFactory(it, diaryEntryId = entryId)
        // }

        // val entryViewModel = ViewModelProvider(this, viewModelFactory).get(DiaryContentViewModel)

        viewModelInjectionHelper<EntryActivityViewModel>(this, diaryEntryId = entryId, dateHolder = dateHolder)
    }


}
