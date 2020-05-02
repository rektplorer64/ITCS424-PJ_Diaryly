package io.dairyly.dairyly.screens.entry

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.afollestad.materialdialogs.MaterialDialog
import io.dairyly.dairyly.R
import io.dairyly.dairyly.utils.viewModelInjectionHelper
import io.dairyly.dairyly.viewmodels.EntryEditorViewModel

/**
 * A container Activity that hosts an instance of EntryEditorFragment
 */
class EntryEditorActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_entry_edit)

        val activityArgs = EntryEditorActivityArgs.fromBundle(intent.extras!!)
        var entryId: String? = activityArgs.entryId

        if(entryId == getString(R.string.debug_default_id)){
           entryId = null
        }

        viewModelInjectionHelper<EntryEditorViewModel>(this, diaryEntryId = entryId,
                                                       date = activityArgs.date)
    }

    override fun onBackPressed() {
        MaterialDialog(this).show {
            title(R.string.dialog_edit_entry_exit_confirm)
            message(R.string.dialog_edit_entry_exit_message)

            positiveButton(res = R.string.confirm){
                finish()
            }
            negativeButton(res = R.string.cancel)
        }
    }
}