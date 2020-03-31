package io.dairyly.dairyly

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.dairyly.dairyly.utils.viewModelInjectionHelper
import io.dairyly.dairyly.viewmodels.DiaryContentViewModel

class EntryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_entry)

        val entryActivityArgs = EntryActivityArgs.fromBundle(intent.extras!!)

        val entryId: Int = entryActivityArgs.entryId
        val userId: Int = entryActivityArgs.userId
        val dateHolder = entryActivityArgs.diaryDateHolder!!

        // val viewModelFactory = this.let { DairyRepository.getInstance(it) }.let {
        //     AppViewModelFactory(it, userId!!)
        // }

        // val entryViewModel = ViewModelProvider(this, viewModelFactory).get(DiaryContentViewModel)

        viewModelInjectionHelper<DiaryContentViewModel>(this, userId, entryId, dateHolder)
    }
}
