package io.dairyly.dairyly.ui.components

import android.text.format.DateFormat
import android.text.format.DateUtils
import android.util.Log
import android.widget.TextView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.textview.MaterialTextView
import io.dairyly.dairyly.R
import io.dairyly.dairyly.models.data.DiaryEntry
import io.dairyly.dairyly.utils.TIME_FORMATTER_SHORT
import org.apache.commons.lang3.time.DateUtils.isSameDay
import java.util.*

class RylyTabEntryDelegate(var onTabClickListener: ((DiaryEntry) -> Unit)? = null) : RylyToolbarBehaviorDelegate<DiaryEntry> {

    private val LOG_TAG = this::class.java.simpleName

    override val itemLayoutRes: Int
        get() = R.layout.item_tab_big

    override fun onTabScrolled(
            overlineTextView: MaterialTextView,
            headerTextView: MaterialTextView,
            subtitleTextView: MaterialTextView,
            item: DiaryEntry) {}

    override fun onTabSelected(overlineTextView: MaterialTextView,
                               headerTextView: MaterialTextView,
                               subtitleTextView: MaterialTextView,
                               item: DiaryEntry) {

        Log.d(LOG_TAG, "Selected a new entry: ${item.id}\t${item.title}")
        // bigTextView.text = TIME_FORMATTER_FULL.format(currentSelectedTime)

        overlineTextView.apply {
            val time = item.timeCreated
            val timeString = DateFormat.getTimeFormat(context).format(time)
            val relativeTimeString = DateUtils.getRelativeTimeSpanString(
                    time.time, System.currentTimeMillis(),
                    DateUtils.DAY_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE)

            text = context.getString(R.string.template_dot_2_messages,
                                     relativeTimeString, timeString)
        }
        headerTextView.text = item.title
        subtitleTextView.text = item.subtitle

        onTabClickListener?.let { it(item) }
        // bigTextView.typeface
        // subtitleTextView.text = DateUtils.getRelativeTimeSpanString(
        //         currentSelectedTime.time, System.currentTimeMillis(),
        //         DateUtils.DAY_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE)
    }

    override fun onCreateTags(tab: TabLayout.Tab, itemIndex: Int, item: DiaryEntry) {
        val goodness = tab.view.findViewById<TextView>(R.id.goodnessPointTextView)
        // if(itemIndex == 0){
        //     // tab.view.rootView.updatePaddingRelative(start = 16.toPx)
        //     tab.view.rootView.setMargins(l = 16.toPx)
        // }
        println("tab.text ==> #${itemIndex + 1}")
        tab.text = "#${(itemIndex + 1)}"
        goodness.text = TIME_FORMATTER_SHORT.format(item.timeCreated)
    }

    override fun isAwaysEmphasized(item: DiaryEntry): Boolean? {
        return isSameDay(Calendar.getInstance().time, item.timeCreated)
    }
}