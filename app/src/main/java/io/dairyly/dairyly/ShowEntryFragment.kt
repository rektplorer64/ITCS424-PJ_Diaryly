package io.dairyly.dairyly

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import io.dairyly.dairyly.data.models.DiaryEntry
import io.dairyly.dairyly.ui.components.DiaryCalendarBar
import io.dairyly.dairyly.ui.components.DiaryCalendarBar.Companion.BEHAVIOR_DIARY_ENTRY
import io.dairyly.dairyly.viewmodels.DiaryContentViewModel
import kotlinx.android.synthetic.main.fragment_show_entry.*


class ShowEntryFragment : Fragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val entryTabs = view.findViewById<DiaryCalendarBar<DiaryEntry>>(R.id.entryTabs)!!.apply {
            behaviorBehaviorDelegate = BEHAVIOR_DIARY_ENTRY
        }
        val viewModel = ViewModelProvider(requireActivity()).get(DiaryContentViewModel::class.java)

        viewModel.dailyDiary.observe(this){
            entryTabs.postDataUpdate(it.data)
        }

        // val viewModel: DiaryContentViewModel by viewModels { viewModelInjectionHelper(this@ShowEntryFragment.context, 1, entryId?: -1) }
        viewModel.entryContent.observe(this){
            // Toast.makeText(this@ShowEntryFragment.context, it.data.toString(), Toast.LENGTH_LONG).show()
            diaryTextView.apply {
                val stringBuilder = StringBuilder()
                it.data?.blockInfo?.forEach { it ->
                    stringBuilder.append(it.content)
                }

                text = stringBuilder
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_show_entry, container, false)
    }
}
