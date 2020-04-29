package io.dairyly.dairyly.screens.main

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import com.kizitonwose.calendarview.model.*
import com.kizitonwose.calendarview.ui.DayBinder
import com.kizitonwose.calendarview.ui.ViewContainer
import io.dairyly.dairyly.R
import io.dairyly.dairyly.viewmodels.DiaryDateViewModel
import kotlinx.android.synthetic.main.fragment_overview.*
import kotlinx.android.synthetic.main.item_tab_small.view.*
import org.threeten.bp.YearMonth
import org.threeten.bp.temporal.WeekFields
import java.util.*

class OverviewFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_overview, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewModel = ViewModelProvider(this)[DiaryDateViewModel::class.java]

        calendarView.inDateStyle = InDateStyle.ALL_MONTHS
        calendarView.outDateStyle = OutDateStyle.END_OF_ROW
        calendarView.scrollMode = ScrollMode.PAGED
        calendarView.orientation = RecyclerView.HORIZONTAL

        calendarView.maxRowCount = 6
        calendarView.hasBoundaries = true

        // TODO: Continue doing this lol
        //   Follow this link: https://github.com/kizitonwose/CalendarView
        calendarView.dayBinder = object : DayBinder<DiaryDateViewContainer> {
            override fun bind(container: DiaryDateViewContainer, day: CalendarDay) {
                container.dateTextView.apply {
                    text = day.date.dayOfMonth.toString()

                    if (day.owner == DayOwner.THIS_MONTH) {
                        setTextColor(Color.WHITE)
                    } else {
                        setTextColor(Color.GRAY)
                    }
                }

                // container.goodBadTextView.apply {
                //
                //     val match = it.find {
                //         day.date.dayOfYear
                //
                //         val c = Calendar.getInstance().apply {
                //             time = it.date
                //         }
                //         c.get(Calendar.DAY_OF_YEAR) == day.date.dayOfYear
                //         // DateUtils.isSameDay(it.date, java.sql.Date.valueOf(day.date))
                //     }
                //
                //     text = match?.goodBadScore?.toString() ?: "0"
                // }
            }

            override fun create(view: View): DiaryDateViewContainer = DiaryDateViewContainer(view)
        }
        val currentMonth = YearMonth.now()
        val firstMonth = currentMonth.minusMonths(10)
        val lastMonth = currentMonth.plusMonths(10)
        val firstDayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek
        calendarView.setup(firstMonth, lastMonth, firstDayOfWeek)
        calendarView.scrollToMonth(currentMonth)

        viewModel.dateHolders.observe(viewLifecycleOwner){

        }
    }
}

class DiaryDateViewContainer(view: View) : ViewContainer(view) {
    val cardContainer = view.dateRootCard
    val dateTextView = view.findViewById<MaterialTextView>(android.R.id.text1)
    val goodBadTextView = view.goodnessPointTextView
}