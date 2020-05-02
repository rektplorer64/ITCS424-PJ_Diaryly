package io.dairyly.dairyly.data.converters

import androidx.room.TypeConverter
import java.util.*

/**
 * A class that converts millisecond timestamp from Long to Date, and in reverse.
 */
class DateConverter {

    /**
     * Convert a millisecond-formatted timestamp Long type to a Date object
     * @param value Long? millisecond timestamp
     * @return Date? output date object
     */
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    /**
     * Convert a Date object to a millisecond-formatted timestamp
     * @param date Date? Date object
     * @return Long? millisecond timestamp
     */
    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

}
