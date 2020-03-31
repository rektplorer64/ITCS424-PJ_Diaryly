package io.dairyly.dairyly.ui.components

import android.content.Context
import android.text.format.DateUtils
import android.text.format.DateUtils.DAY_IN_MILLIS
import android.text.format.DateUtils.getRelativeTimeSpanString
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.observe
import androidx.viewpager.widget.ViewPager
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.card.MaterialCardView
import com.google.android.material.tabs.TabLayout
import io.dairyly.dairyly.R
import io.dairyly.dairyly.data.models.DiaryDateHolder
import io.dairyly.dairyly.data.models.DiaryEntry
import io.dairyly.dairyly.utils.CURRENT_LOCALE
import io.dairyly.dairyly.utils.toPx
import kotlinx.android.synthetic.main.bar_diary_calendar_view.view.*
import org.apache.commons.lang3.time.DateUtils.isSameDay
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

const val DAY_TIME_WINDOW = 14

interface DiaryBarBehaviorDelegate<TabType>{

    @get:LayoutRes
    val itemLayoutRes: Int

    /**
     * This method specifies how to process the data to display on the overline TextView which is located on the top of the view.
     * @param item TabType      the data item that will be bind to the target tab.
     * @return String           the string that will be set to the overline of the view.
     */
    fun updateOverlineTextOnScroll(item: TabType): String

    /**
     * This method specifies the behavior when a tab is selected.
     * @param bigTextView TextView      the text view that can be used to display item on change.
     * @param subtitleTextView TextView      the subtitle text view that can be used to display item on change.
     * @param item TabType      the data item that will be bind to the target tab.
     */
    fun onTabSelectedListener(bigTextView: TextView,
                              subtitleTextView: TextView, item: TabType)

    /**
     * This method specifies the appearance of each individual Tag on its creation time.
     * @param tab Tab           the tab that is the target.
     * @param itemIndex Int           an integer that specifies the index of the tab.
     * @param item TabType      the data item that will be bind to the target tab.
     */
    fun onCreateTags(tab: TabLayout.Tab, itemIndex: Int, item: TabType)

    /**
     * This method determines the condition that could be used to determine always selected item like
     * the current date that needed to be focused.
     * @param item TabType      the data item that will be used to determine whether to treat the item as always emphasized or not.
     * @return Boolean?         a truth value that if true -> always emphasize. Otherwise -> do not. If this returns null, it means that there is no item to emphasize.
     */
    fun isAwaysEmphasized(item: TabType): Boolean?
}

class DiaryCalendarBar<TabType>(context: Context, attributes: AttributeSet) :
        MaterialToolbar(context, attributes) {

    companion object{
        val DATE_FORMATTER_MONTH_YEAR = SimpleDateFormat("MMMM, yyyy", CURRENT_LOCALE)
        val DATE_FORMATTER_DATE = SimpleDateFormat("dd", CURRENT_LOCALE)
        val DATE_FORMATTER_FULL: DateFormat = DateFormat.getDateInstance(DateFormat.FULL)
        val DATE_FORMATTER_TIME: DateFormat = DateFormat.getTimeInstance(DateFormat.SHORT)

        private val c: Calendar = Calendar.getInstance()

        val BEHAVIOR_DIARY_DATE: DiaryBarBehaviorDelegate<DiaryDateHolder> = object : DiaryBarBehaviorDelegate<DiaryDateHolder>{

            override val itemLayoutRes: Int
                get() = R.layout.item_tab_small

            override fun updateOverlineTextOnScroll(item: DiaryDateHolder): String {
                return DATE_FORMATTER_MONTH_YEAR.format(item.date)
            }

            override fun onTabSelectedListener(bigTextView: TextView,
                                               subtitleTextView: TextView,
                                               item: DiaryDateHolder) {
                val currentSelectedTime = item.date
                bigTextView.text = DATE_FORMATTER_FULL.format(currentSelectedTime)
                bigTextView.typeface
                subtitleTextView.text = getRelativeTimeSpanString(currentSelectedTime.time, System.currentTimeMillis(), DAY_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE)
            }

            override fun onCreateTags(tab: TabLayout.Tab, itemIndex: Int, item: DiaryDateHolder) {
                val goodness = tab.view.findViewById<TextView>(R.id.goodnessPointTextView)
                tab.text = DATE_FORMATTER_DATE.format(item.date)
                goodness.text = item.goodBadScore.toString()
            }

            override fun isAwaysEmphasized(item: DiaryDateHolder): Boolean? {
                return isSameDay(c.time, item.date)
            }
        }

        val BEHAVIOR_DIARY_ENTRY: DiaryBarBehaviorDelegate<DiaryEntry> = object : DiaryBarBehaviorDelegate<DiaryEntry>{

            override val itemLayoutRes: Int
                get() = R.layout.item_tab_big

            override fun updateOverlineTextOnScroll(item: DiaryEntry): String {
                return DATE_FORMATTER_MONTH_YEAR.format(item.info.timeCreated)
            }

            override fun onTabSelectedListener(bigTextView: TextView,
                                               subtitleTextView: TextView,
                                               item: DiaryEntry) {
                val currentSelectedTime = item.info.timeCreated
                bigTextView.text = DATE_FORMATTER_FULL.format(currentSelectedTime)
                // bigTextView.typeface
                subtitleTextView.text = getRelativeTimeSpanString(currentSelectedTime.time, System.currentTimeMillis(), DAY_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE)
            }

            override fun onCreateTags(tab: TabLayout.Tab, itemIndex: Int, item: DiaryEntry) {
                val goodness = tab.view.findViewById<TextView>(R.id.goodnessPointTextView)
                // if(itemIndex == 0){
                //     // tab.view.rootView.updatePaddingRelative(start = 16.toPx)
                //     tab.view.rootView.setMargins(l = 16.toPx)
                // }
                println("tab.text ==> #${itemIndex + 1}")
                tab.text = "#${(itemIndex + 1)}"
                goodness.text = DATE_FORMATTER_TIME.format(item.info.timeCreated)
            }

            override fun isAwaysEmphasized(item: DiaryEntry): Boolean? {
                return isSameDay(c.time, item.info.timeCreated)
            }
        }
    }

    // val calendarRvAdapter = CalendarRvAdapter()
    lateinit var behaviorBehaviorDelegate: DiaryBarBehaviorDelegate<TabType>

    val tabLayoutWrapper: TabLayoutWrapper

    private val liveDataScrollX: MutableLiveData<Int> = MutableLiveData()

    init {
        val attrs = context.obtainStyledAttributes(attributes, R.styleable.DiaryCalendarBar, 0, 0)
        val profileImage = attrs.getColor(R.styleable.DiaryCalendarBar_profileImage,
                                          R.drawable.ic_launcher_background);
        // TODO: Add

        attrs.recycle()

        val root = inflate(context, R.layout.bar_diary_calendar_view, this)
        setContentInsetsAbsolute(0, 0)
        tabLayoutWrapper = TabLayoutWrapper(root)

        liveDataScrollX.postValue(tabLayoutWrapper.calendarTab.scrollX)
        tabLayoutWrapper.calendarTab.apply {
            setOnScrollChangeListener { _, scrollX, _, _, _ ->
                // val minPosition = calculateItemToDisplayMonth(v as TabLayout, scrollX)
                liveDataScrollX.postValue(scrollX)
            }
        }
    }

    fun postDataUpdate(list: List<TabType>?) {
        if(list != null) {
            tabLayoutWrapper.postUpdate(list)

            liveDataScrollX.observe(context as LifecycleOwner){
                val minPosition = calculateTagInTheMiddle(tabLayoutWrapper.calendarTab, it)
                this@DiaryCalendarBar.dateTextView.apply {
                    // text = formatter.format(list[minPosition].date)
                    text = behaviorBehaviorDelegate.updateOverlineTextOnScroll(list[minPosition])
                }

                // println("Closest View: ${tabLayoutWrapper.calendarTab.getTabAt(minPosition)!!.text}")
            }

            tabLayoutWrapper.calendarTab.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener{
                override fun onTabReselected(tab: TabLayout.Tab?) {
                    // TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {
                    // TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun onTabSelected(tab: TabLayout.Tab?) {
                    behaviorBehaviorDelegate.onTabSelectedListener(bigTextView,subtitleTextView,
                                                                   list[tab!!.position])
                }
            })
        }
    }

    private fun calculateTagInTheMiddle(tabLayout: TabLayout, scrollX: Int = 0): Int{
        val tabLayoutWidthCenter = tabLayout.measuredWidth / 2 - tabLayout.getTabAt(0)!!.view.width

        var minPosition = -1
        var initialDistanceDiff = 999.0
        for(i in 0 until tabLayout.tabCount){
            // Distance Diff between middle a child view
            val averageTagHorizontal = (tabLayout.getTabAt(i)!!.view.left + tabLayout.getTabAt(i)!!.view.right) / 2.0
            val distanceDiff = abs(tabLayoutWidthCenter - averageTagHorizontal + scrollX)
            if(initialDistanceDiff > distanceDiff){
                initialDistanceDiff = distanceDiff
                minPosition = i
            }
        }
        return minPosition
    }

    fun setupViewPager(viewPager: ViewPager) {
        tabLayoutWrapper.calendarTab.setupWithViewPager(viewPager)
    }

    inner class TabLayoutWrapper(root: View) {
        val calendarTab: FadingTabLayout = root.findViewById(R.id.date_tabs)

        init {
            calendarTab.isSmoothScrollingEnabled = true
        }

        fun postUpdate(diaryDateHolders: List<TabType>) {
            calendarTab.removeAllTabs()
            for(i in diaryDateHolders.indices) {
                val tab = calendarTab.newTab().apply {
                    setCustomView(behaviorBehaviorDelegate.itemLayoutRes)
                    val dataItem = diaryDateHolders[i]
                    behaviorBehaviorDelegate.onCreateTags(this, i, dataItem)

                    val isAlwaysEmphasized: Boolean? = behaviorBehaviorDelegate.isAwaysEmphasized(dataItem)
                    if(isAlwaysEmphasized != null && isAlwaysEmphasized) {
                        customView!!.findViewById<MaterialCardView>(R.id.date_root_card).apply {
                            strokeWidth = 2.toPx
                            strokeColor = this.context.getColor(R.color.colorPrimary)
                        }
                    }
                }
                calendarTab.addTab(tab)
            }

            scrollToTab()
        }

        private fun scrollToTab(tabIndex: Int = ((DAY_TIME_WINDOW + 1) / 2)) {
            val tab = (calendarTab.getChildAt(0) as ViewGroup).getChildAt(tabIndex)
            tab.post {
                val right = tab.right
                calendarTab.scrollTo(right, 0)
                calendarTab.getTabAt(tabIndex)!!.select()
            }
        }

    }

    class FadingTabLayout(context: Context, attributes: AttributeSet) :
            TabLayout(context, attributes) {
        override fun getLeftFadingEdgeStrength(): Float {
            return 0f
        }
    }
}

