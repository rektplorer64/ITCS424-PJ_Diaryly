package io.dairyly.dairyly.data.converters

import androidx.room.TypeConverter
import io.dairyly.dairyly.data.models.DiaryEntryBlockInfo
import io.dairyly.dairyly.data.models.DiaryEntryInfo
// import io.dairyly.dairyly.data.models.DairyEntryBlockInfo
// import io.dairyly.dairyly.data.models.DairyEntryInfo
import io.dairyly.dairyly.data.models.UserFile

class UserFileTypeConverter{
    @TypeConverter
    fun fromString(value: String?): UserFile.Type? {
        return value?.let {
            UserFile.Type.valueOf(it)
        }
    }

    @TypeConverter
    fun fromEnum(type: UserFile.Type?): String? {
        return type?.toString()
    }
}

class GoodBadConverter{
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
