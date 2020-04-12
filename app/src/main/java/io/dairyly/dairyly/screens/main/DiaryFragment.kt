package io.dairyly.dairyly.screens.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import io.dairyly.dairyly.R
import io.dairyly.dairyly.data.models.DiaryDateHolder
import io.dairyly.dairyly.ui.components.RylyTabDateDelegate
import io.dairyly.dairyly.ui.components.RylyToolbarView
import io.dairyly.dairyly.viewmodels.DiaryDateViewModel
import kotlinx.android.synthetic.main.fragment_diary.*
import org.apache.commons.lang3.time.DateUtils
import java.util.*

class DiaryFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_diary, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val userId = 1
        val viewModel: DiaryDateViewModel = ViewModelProvider(
                this@DiaryFragment.activity!!).get(DiaryDateViewModel::class.java)

        val calendarBar: RylyToolbarView<DiaryDateHolder> = view.findViewById<RylyToolbarView<DiaryDateHolder>>(
                R.id.calendarBar).apply {
            behaviorBehaviorDelegate = RylyTabDateDelegate()
        }

        viewModel.dateHolders.observe(this) {
            val fragmentAdapter = DiaryDateViewPagerAdapter(
                    userId, this, it)
            diaryDateViewPager.apply {
                adapter = fragmentAdapter
                TabLayoutMediator(calendarBar.tabLayoutWrapper.calendarTab, this) { tab, _ ->
                    this.setCurrentItem(tab.position, true)
                }.attach()
                offscreenPageLimit = ViewPager2.OFFSCREEN_PAGE_LIMIT_DEFAULT
            }
            calendarBar.postDataUpdate(it)
        }
        calendarBarLayout.setLiftable(true)

        addEntryFAB.setOnClickListener {
            val action = DiaryFragmentDirections.actionDiaryFragmentToEntryEditActivity()
            findNavController().navigate(action)
        }
    }
}

class DiaryDateViewPagerAdapter(val userId: Int, fragment: Fragment,
                                private val dates: List<DiaryDateHolder>) :
        FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int {
        return dates.size
    }

    override fun createFragment(position: Int): Fragment {
        return DiaryListFragment.newInstance(userId,
                                             dates[position].apply {
                                                 date = DateUtils.truncate(
                                                         this.date,
                                                         Calendar.DATE)
                                             })
    }

}
