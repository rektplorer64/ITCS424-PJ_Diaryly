package io.dairyly.dairyly.screens.entry

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.observe
import io.dairyly.dairyly.R
import io.dairyly.dairyly.utils.viewModelInjectionHelper
import io.dairyly.dairyly.viewmodels.EntryDisplayViewModel
import kotlinx.android.synthetic.main.fragment_entry_view.*

class DiaryContentDisplayFragment : Fragment() {

    private val LOG_TAG = this::class.java.simpleName

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_entry_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var userId = ""
        arguments!!.apply{
            getString(BUNDLE_DIARY_ENTRY_ID).let {
                userId = it!!
            }
        }

        // TODO -> Implement this fragment to display contents!
        val viewModel = viewModelInjectionHelper<EntryDisplayViewModel>(this, diaryEntryId = userId)

        viewModel.entryContent.observe(this){
            Log.d(LOG_TAG, "Showing Diary: $it")
            if(it.data != null){
                entryContentTextView.apply {
                    text = it.data.content
                }
            }
        }
    }

    companion object{
        const val BUNDLE_DIARY_ENTRY_ID = "entryID"

        fun newInstance(entryId: String): DiaryContentDisplayFragment {
            return DiaryContentDisplayFragment().apply {
                val bundle = Bundle().apply {
                    putString(BUNDLE_DIARY_ENTRY_ID, entryId)
                }
                arguments = bundle
            }
        }
    }
}