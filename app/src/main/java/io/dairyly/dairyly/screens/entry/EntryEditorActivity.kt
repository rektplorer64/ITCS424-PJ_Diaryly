package io.dairyly.dairyly.screens.entry

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.dairyly.dairyly.R
import io.dairyly.dairyly.utils.viewModelInjectionHelper
import io.dairyly.dairyly.viewmodels.EntryEditorViewModel

class EntryEditorActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_entry_edit)

        val activityArgs = EntryEditorActivityArgs.fromBundle(intent.extras!!)
        var entryId: String? = activityArgs.entryId

        if(entryId == getString(R.string.debug_default_id)){
           entryId = null
        }

        viewModelInjectionHelper<EntryEditorViewModel>(this, diaryEntryId = entryId)
    }
}