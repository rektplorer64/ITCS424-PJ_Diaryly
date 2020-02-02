package io.dairyly.dairyly.data.converters

import androidx.room.TypeConverter
import java.io.File

class FileConverter{
    @TypeConverter
    fun fromString(value: String?): File? {
        return if(value == null){
            null
        }else{
            File(value)
        }
    }

    @TypeConverter
    fun fromFile(file: File?): String? {
        return file?.canonicalPath
    }
}