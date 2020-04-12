package io.dairyly.dairyly.screens.entry

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import io.dairyly.dairyly.R
import io.dairyly.dairyly.models.data.DiaryEntry
import io.dairyly.dairyly.ui.components.RylyTabEntryDelegate
import io.dairyly.dairyly.ui.components.RylyToolbarView
import io.dairyly.dairyly.viewmodels.EntryActivityViewModel
import kotlinx.android.synthetic.main.fragment_show_entry.*


class ShowEntryFragment : Fragment() {

    private val LOG_TAG = this::class.java.simpleName

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val viewModel = ViewModelProvider(activity!!).get(EntryActivityViewModel::class.java)

        val entryTabs = view.findViewById<RylyToolbarView<DiaryEntry>>(
                R.id.entryTabs)!!.apply {
            behaviorBehaviorDelegate = RylyTabEntryDelegate()
        }

        viewModel.dailyDiary.observe(this){
            if(it.data.isNullOrEmpty()){
                return@observe
            }

            Log.d(LOG_TAG, "${it.data.size} entries received for displaying tabs")
            val fragmentAdapter = DiaryEntryViewPagerAdapter(
                    it.data.map(DiaryEntry::id), this)
            entryTabs.postDataUpdate(it.data)

            diaryEntryViewPager.apply {
                adapter = fragmentAdapter
                TabLayoutMediator(entryTabs.tabLayoutWrapper.calendarTab, this) { tab, _ ->
                    this.setCurrentItem(tab.position, true)
                }.attach()
                offscreenPageLimit = ViewPager2.OFFSCREEN_PAGE_LIMIT_DEFAULT
            }
            entryTabs.postDataUpdate(it.data)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_show_entry, container, false)
    }
}

class DiaryEntryViewPagerAdapter(private val entryIds: List<String>, fragment: Fragment) :
        FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int {
        return entryIds.size
    }

    override fun createFragment(position: Int): Fragment {
        return DiaryContentDisplayFragment.newInstance(entryIds[position])
    }

}