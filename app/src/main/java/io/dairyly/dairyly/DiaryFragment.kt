package io.dairyly.dairyly

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import io.dairyly.dairyly.data.models.DiaryDateHolder
import io.dairyly.dairyly.ui.components.DiaryCalendarBar
import io.dairyly.dairyly.ui.components.DiaryCalendarBar.Companion.BEHAVIOR_DIARY_DATE
import io.dairyly.dairyly.viewmodels.DiaryDateViewModel
import kotlinx.android.synthetic.main.fragment_diary.*
import java.util.*

class DiaryFragment : Fragment() {

    private val VIEW_PAGER_OFFSCREEN_PAGE_LIMIT: Int = 5

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_diary, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val userId = 1
        val diaryDateViewModel: DiaryDateViewModel = ViewModelProvider(
                this@DiaryFragment.activity!!).get(DiaryDateViewModel::class.java)

        val calendarBar: DiaryCalendarBar<DiaryDateHolder> = view.findViewById<DiaryCalendarBar<DiaryDateHolder>>(
                R.id.calendarBar).apply {
            this.behaviorBehaviorDelegate = BEHAVIOR_DIARY_DATE
        }
        diaryDateViewModel.dateHolderListLiveData.observe(this) {
            println(it)
            val fragmentAdapter = DiaryDateViewPagerAdapter(userId, this, it)
            diaryDateViewPager.apply {
                adapter = fragmentAdapter
                TabLayoutMediator(calendarBar.tabLayoutWrapper.calendarTab, this) { tab, _ ->
                    this.setCurrentItem(tab.position, true)
                }.attach()
                offscreenPageLimit = VIEW_PAGER_OFFSCREEN_PAGE_LIMIT
            }
            calendarBar.postDataUpdate(it)

        }
        calendarBarLayout.setLiftable(true)

    }
}

class DiaryDateViewPagerAdapter(val userId: Int, fragment: Fragment,
                                private val dates: List<DiaryDateHolder>) :
        FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int {
        return dates.size
    }

    override fun createFragment(position: Int): Fragment {
        return DiaryListFragment.newInstance(userId, dates[position].apply {
            date = org.apache.commons.lang3.time.DateUtils.truncate(this.date, Calendar.DATE)
        })
    }

}
