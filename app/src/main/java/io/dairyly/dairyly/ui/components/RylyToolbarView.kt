package io.dairyly.dairyly.ui.components

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.observe
import androidx.viewpager.widget.ViewPager
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.card.MaterialCardView
import com.google.android.material.tabs.TabLayout
import io.dairyly.dairyly.R
import io.dairyly.dairyly.utils.toPx
import kotlinx.android.synthetic.main.bar_diary_calendar_view.view.*
import kotlin.math.abs

const val DAY_TIME_WINDOW = 14

class RylyToolbarView<TabType>(context: Context, attributes: AttributeSet) :
        MaterialToolbar(context, attributes) {

    private val LOG_TAG = this::class.java.simpleName

    // val calendarRvAdapter = CalendarRvAdapter()
    lateinit var behaviorBehaviorDelegate: RylyToolbarBehaviorDelegate<TabType>

    val tabLayoutWrapper: TabLayoutWrapper

    private val liveDataScrollX: MutableLiveData<Int> = MutableLiveData()

    val autoScrollingEnabled: Boolean

    init {
        val attrs = context.obtainStyledAttributes(attributes, R.styleable.RylyToolbarView, 0, 0)

        // Apply attributes
        val profileImageRes: Int
        attrs.apply {
            try {
                autoScrollingEnabled = getBoolean(R.styleable.RylyToolbarView_autoInitialScroll,
                                                  true)

                profileImageRes = getResourceId(R.styleable.RylyToolbarView_profileImage,
                                                R.drawable.ic_launcher_background)
            } finally {
                recycle()
            }
        }

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

        profileImageView.setImageDrawable(getDrawable(context, profileImageRes))
    }

    fun postDataUpdate(list: List<TabType>?) {
        if(list != null) {
            tabLayoutWrapper.postUpdate(list)

            liveDataScrollX.observe(context as LifecycleOwner) {
                val minPosition = calculateTagInTheMiddle(tabLayoutWrapper.calendarTab, it)
                behaviorBehaviorDelegate.onTabScrolled(defaultOverlineText, defaultHeaderText,
                                                       subtitleTextView, list[minPosition])
                // println("Closest View: ${tabLayoutWrapper.calendarTab.getTabAt(minPosition)!!.text}")
            }

            tabLayoutWrapper.calendarTab
                    .addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                        override fun onTabReselected(tab: TabLayout.Tab?) {
                            // TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                        }

                        override fun onTabUnselected(tab: TabLayout.Tab?) {
                            // TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                        }

                        override fun onTabSelected(tab: TabLayout.Tab?) {
                            behaviorBehaviorDelegate.onTabSelected(defaultOverlineText,
                                                                   defaultHeaderText,
                                                                   subtitleTextView,
                                                                   list[tab!!.position])
                        }
                    })
        }
    }

    private fun calculateTagInTheMiddle(tabLayout: TabLayout, scrollX: Int = 0): Int {
        val tabLayoutWidthCenter = tabLayout.measuredWidth / 2 - tabLayout.getTabAt(0)!!.view.width

        var minPosition = -1
        var initialDistanceDiff = 999.0
        for(i in 0 until tabLayout.tabCount) {
            // Distance Diff between middle a child view
            val averageTagHorizontal = (tabLayout.getTabAt(i)!!.view.left + tabLayout.getTabAt(
                    i)!!.view.right) / 2.0
            val distanceDiff = abs(tabLayoutWidthCenter - averageTagHorizontal + scrollX)
            if(initialDistanceDiff > distanceDiff) {
                initialDistanceDiff = distanceDiff
                minPosition = i
            }
        }
        return minPosition
    }

    fun setupViewPager(viewPager: ViewPager) {
        tabLayoutWrapper.calendarTab.setupWithViewPager(viewPager)
    }

    private val STATE_PERSISTENT_SUPER = "sajdhaskdjkahsjkda"
    private val STATE_PERSISTENT_TAB_SELECTION = "sajdhaskdjkahsjkda"

    override fun onSaveInstanceState(): Parcelable? {
        super.onSaveInstanceState()
        return Bundle().apply {
            putParcelable(STATE_PERSISTENT_SUPER, super.onSaveInstanceState())
            putInt(STATE_PERSISTENT_TAB_SELECTION, tabLayoutWrapper.calendarTab.selectedTabPosition)
        }
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        var newState = state
        if(newState is Bundle){
            val selectedTab = newState.getInt(STATE_PERSISTENT_TAB_SELECTION)
            this@RylyToolbarView.post {
                Log.d(LOG_TAG, "Scrolling to the ${selectedTab}th tab")
                tabLayoutWrapper.scrollToTab(selectedTab)
            }
            newState = newState.getParcelable(STATE_PERSISTENT_SUPER)
        }

        super.onRestoreInstanceState(newState)
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

                    val isAlwaysEmphasized: Boolean? = behaviorBehaviorDelegate
                            .isAwaysEmphasized(dataItem)
                    if(isAlwaysEmphasized != null && isAlwaysEmphasized) {
                        customView!!.findViewById<MaterialCardView>(R.id.date_root_card).apply {
                            strokeWidth = 2.toPx
                            strokeColor = this.context.getColor(R.color.colorPrimary)
                        }
                    }
                }
                calendarTab.addTab(tab)
            }

            if(autoScrollingEnabled) {
                scrollToTab()
            }
        }

        fun scrollToTab(tabIndex: Int = ((DAY_TIME_WINDOW + 1) / 2)) {
            val parent = calendarTab.getChildAt(0) as ViewGroup

            val finalTabIndex = if(tabIndex >= parent.childCount) {
                parent.childCount / 2
            } else {
                tabIndex
            }
            parent.post {
                val tab = parent.getChildAt(finalTabIndex)?: parent.getChildAt(0)
                tab?.post {
                    val right = tab.right
                    calendarTab.scrollTo(right, 0)
                    calendarTab.getTabAt(finalTabIndex)!!.select()
                }
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
