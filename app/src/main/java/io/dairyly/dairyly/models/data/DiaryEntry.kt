package io.dairyly.dairyly.models.data

import com.google.firebase.database.IgnoreExtraProperties
import io.dairyly.dairyly.data.models.DiaryEntryInfo
import io.dairyly.dairyly.data.models.Tag
import java.util.*
import kotlin.collections.HashMap

class DiaryEntry(
        var id: String,
        val timeCreated: Date,
        var timeModified: Date,
        var goodBadScore: Int,
        var tags: List<DiaryTag>,
        var entry: String,
        lat: Double,
        long: Double
){
    var location: HashMap<String, Double> = hashMapOf("lat" to lat, "long" to long)

    fun toFirebaseData(): FirebaseDiaryEntry{
        return FirebaseDiaryEntry(id, timeCreated.time, timeModified.time, goodBadScore, tags, entry, location)
    }

    override fun toString(): String {
        return "DiaryEntry(id='$id', timeCreated=$timeCreated, timeModified=$timeModified, goodBadScore=$goodBadScore, tags=$tags, entry='$entry', location=$location)"
    }
}

@IgnoreExtraProperties
data class FirebaseDiaryEntry(
        var id: String = "",
        val timeCreated: Long = 0L,
        var timeModified: Long = 0L,
        var goodBadScore: Int = 0,
        var tags: List<DiaryTag> = arrayListOf(),
        var entry: String = "",
        var location: HashMap<String, Double> = hashMapOf("lat" to 0.0, "long" to 0.0)
){
    // constructor() : this("", 0L, 0L, 0, arrayListOf(), "", 0.0, 0.0)

    fun toNormalData(): DiaryEntry{
        return DiaryEntry(id, Date(timeCreated), Date(timeModified), goodBadScore, tags, entry, location["lat"]!!, location["long"]!!)
    }
}
