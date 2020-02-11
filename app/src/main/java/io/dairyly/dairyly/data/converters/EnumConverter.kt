package io.dairyly.dairyly.data.converters

import androidx.room.TypeConverter
import io.dairyly.dairyly.data.models.DairyEntryBlockInfo
import io.dairyly.dairyly.data.models.DairyEntryInfo
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
    fun fromString(value: String?): DairyEntryInfo.GoodBad? {
        return value?.let {
            DairyEntryInfo.GoodBad.valueOf(it)
        }
    }

    @TypeConverter
    fun fromEnum(type: DairyEntryInfo.GoodBad?): String? {
        return type?.toString()
    }
}

class BlockTypeConverter{
    @TypeConverter
    fun fromString(value: String?): DairyEntryBlockInfo.Type? {
        return value?.let {
            DairyEntryBlockInfo.Type.valueOf(it)
        }
    }

    @TypeConverter
    fun fromEnum(type: DairyEntryBlockInfo.Type?): String? {
        return type?.toString()
    }
}
