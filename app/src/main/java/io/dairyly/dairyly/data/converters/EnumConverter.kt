package io.dairyly.dairyly.data.converters

import androidx.room.TypeConverter
import io.dairyly.dairyly.data.models.DiaryEntryBlockInfo
import io.dairyly.dairyly.data.models.DiaryEntryInfo
// import io.dairyly.dairyly.data.models.DairyEntryBlockInfo
// import io.dairyly.dairyly.data.models.DairyEntryInfo
import io.dairyly.dairyly.data.models.UserFile

/**
 * A class that indicate file type enum to a string
 */
class UserFileTypeConverter{

    /**
     * Convert a enum to a string
     * @param value String? string type
     * @return UserFile.Type? an enum type
     */
    @TypeConverter
    fun fromString(value: String?): UserFile.Type? {
        return value?.let {
            UserFile.Type.valueOf(it)
        }
    }

    /**
     * Convert an FileType enum to a string
     * @param type Type? FileType enum
     * @return String? a string representation
     */
    @TypeConverter
    fun fromEnum(type: UserFile.Type?): String? {
        return type?.toString()
    }
}

/**
 * Convert GoodBad enum class to an integer
 */
class GoodBadConverter{

    /**
     * Convert a value integer to an enum of GoodBad
     * @param value Int? GoodBad value integer
     * @return DiaryEntryInfo.GoodBad?  GoodBad enum
     */
    @TypeConverter
    fun fromString(value: Int?): DiaryEntryInfo.GoodBad? {
        return when(value){
            -1 -> {
                DiaryEntryInfo.GoodBad.BAD
            }
            else -> {
                DiaryEntryInfo.GoodBad.GOOD
            }
        }
    }


    /**
     * Convert an enum of GoodBad to integer
     * @param type GoodBad? GoodBad enum
     * @return Int? integer representation
     */
    @TypeConverter
    fun fromEnum(type: DiaryEntryInfo.GoodBad?): Int? {
        return when(type.toString()){
            DiaryEntryInfo.GoodBad.GOOD.toString() -> 1
            else                                   -> -1
        }
    }
}

class BlockTypeConverter{
    @TypeConverter
    fun fromString(value: String?): DiaryEntryBlockInfo.Type? {
        return value?.let {
            DiaryEntryBlockInfo.Type.valueOf(it)
        }
    }

    @TypeConverter
    fun fromEnum(type: DiaryEntryBlockInfo.Type?): String? {
        return type?.toString()
    }
}
