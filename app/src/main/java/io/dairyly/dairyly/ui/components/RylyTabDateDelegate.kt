package io.dairyly.dairyly.ui.components

import android.text.format.DateUtils
import android.view.View
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.datetime.datePicker
import com.google.android.material.tabs.TabLayout
import com.google.android.material.textview.MaterialTextView
import io.dairyly.dairyly.R
import io.dairyly.dairyly.data.models.DiaryDateHolder
import io.dairyly.dairyly.utils.DATE_FORMATTER_DATE
import io.dairyly.dairyly.utils.DATE_FORMATTER_FULL
import io.dairyly.dairyly.utils.DATE_FORMATTER_MONTH_YEAR
import org.apache.commons.lang3.time.DateUtils.isSameDay
import java.util.*

class RylyTabDateDelegate(private val dateDialogListener: (Calendar) -> Unit) : RylyToolbarBehaviorDelegate<DiaryDateHolder> {

    override val itemLayoutRes: Int
        get() = R.layout.item_tab_small

    override fun onTabScrolled(
            overlineTextView: MaterialTextView,
            headerTextView: MaterialTextView,
            subtitleTextView: MaterialTextView,
            item: DiaryDateHolder) {
        overlineTextView.text = DATE_FORMATTER_MONTH_YEAR.format(item.date)
    }

    override fun onTabSelected(
            overlineTextView: MaterialTextView, headerTextView: MaterialTextView,
            subtitleTextView: MaterialTextView,
            item: DiaryDateHolder) {
        val currentSelectedTime = item.date
        headerTextView.text = DATE_FORMATTER_FULL.format(currentSelectedTime)
        // headerTextView.typeface
        subtitleTextView.text = DateUtils.getRelativeTimeSpanString(
                currentSelectedTime.time, System.currentTimeMillis(),
                DateUtils.DAY_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE)
    }

    override fun onCreateTags(tab: TabLayout.Tab, itemIndex: Int, item: DiaryDateHolder) {
        val goodness = tab.view.findViewById<TextView>(R.id.goodnessPointTextView)
        tab.text = DATE_FORMATTER_DATE.format(item.date)
        goodness.text = item.goodBadScore.toString()
    }

    override fun isAwaysEmphasized(item: DiaryDateHolder): Boolean? {
        return isSameDay(Calendar.getInstance().time, item.date)
    }

    override fun onOverlineTextClickListener(textView: View, item: DiaryDateHolder) {
        MaterialDialog(textView.context).show {
            title(text = context.getString(R.string.dialog_time_window_select))

            datePicker { dialog, date ->
                // Use date (Calendar)
                dateDialogListener(date)
            }
        }
    }
}
