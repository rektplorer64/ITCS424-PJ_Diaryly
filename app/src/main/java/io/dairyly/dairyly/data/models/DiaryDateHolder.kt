package io.dairyly.dairyly.data.models


import org.apache.commons.lang3.time.DateUtils
import java.io.Serializable
import java.util.*

data class DiaryDateHolder(
        var date: Date,
        var goodBadScore: Int
) : Comparable<DiaryDateHolder>, Serializable {

    override fun compareTo(other: DiaryDateHolder): Int {
        return this.date.time.compareTo(other.date.time)
    }

    override fun equals(other: Any?): Boolean {
        if(other !is DiaryDateHolder){
            return false
        }
        return DateUtils.truncatedEquals(other.date, this.date, Calendar.DATE)
    }

    override fun hashCode(): Int {
        return date.hashCode()
    }
}

fun List<DiaryDateHolder>?.convertToMonthList(): List<Date>?{
    if(this == null){
        return null
    }
    val sortedMonth: TreeSet<Date> = TreeSet(kotlin.Comparator { o1, o2 -> o1.compareTo(o2) })
    for(holder in this){
        val monthTruncated = DateUtils.truncate(holder.date, Calendar.MONTH)
        sortedMonth.add(monthTruncated)
    }
    return sortedMonth.toList()
}

fun List<DiaryDateHolder>?.convertToDateList(): List<Date>?{
    if(this == null){
        return null
    }
    val list = arrayListOf<Date>()
    for(holder in this){
        list.add(holder.date)
    }
    return list.toList()
}