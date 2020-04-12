package io.dairyly.dairyly.models.data

import android.net.Uri
import com.google.firebase.database.IgnoreExtraProperties
import java.util.*
import kotlin.collections.HashMap

class DiaryEntry(
        var id: String,
        val timeCreated: Date,
        var timeModified: Date,
        var goodBadScore: Int,
        var tags: List<DiaryTag>,
        var title: String,
        var subtitle: String,
        var content: String,
        var images: List<DiaryImage>?,
        lat: Double,
        long: Double
){
    var location: HashMap<String, Double> = hashMapOf("lat" to lat, "long" to long)

    fun toFirebaseData(): FirebaseDiaryEntry{
        val hashMap = if(!images.isNullOrEmpty()) {
            hashMapOf<String, FirebaseDiaryImage>().apply {
                for(i in images!!.indices){
                    this[images!![i].id] = images!![i].toFirebaseData()
                }
            }
        }else{
            null
        }
        return FirebaseDiaryEntry(id, timeCreated.time, timeModified.time, goodBadScore, tags,
                                  title, subtitle, content, hashMap, location)
    }



    override fun equals(other: Any?): Boolean {
        if(this === other) return true
        if(javaClass != other?.javaClass) return false

        other as DiaryEntry

        if(id != other.id) return false
        if(timeCreated != other.timeCreated) return false
        if(timeModified != other.timeModified) return false
        if(goodBadScore != other.goodBadScore) return false
        if(tags != other.tags) return false
        if(title != other.title) return false
        if(subtitle != other.subtitle) return false
        if(content != other.content) return false
        if(images != other.images) return false
        if(location != other.location) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + timeCreated.hashCode()
        result = 31 * result + timeModified.hashCode()
        result = 31 * result + goodBadScore
        result = 31 * result + tags.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + subtitle.hashCode()
        result = 31 * result + content.hashCode()
        result = 31 * result + (images?.hashCode() ?: 0)
        result = 31 * result + location.hashCode()
        return result
    }

    override fun toString(): String {
        return "DiaryEntry(id='$id', timeCreated=$timeCreated, timeModified=$timeModified, goodBadScore=$goodBadScore, tags=$tags, title='$title', subtitle='$subtitle', content='$content', images=$images, location=$location)"
    }


}

@IgnoreExtraProperties
data class FirebaseDiaryEntry(
        var id: String = "",
        val timeCreated: Long = 0L,
        var timeModified: Long = 0L,
        var goodBadScore: Int = 0,
        var tags: List<DiaryTag> = arrayListOf(),
        var title: String = "",
        var subtitle: String = "",
        var content: String = "",
        var images: HashMap<String, FirebaseDiaryImage>? = HashMap(),
        var location: HashMap<String, Double> = hashMapOf(
                "lat" to 0.0, "long" to 0.0)
){

    fun toNormalData(): DiaryEntry{
        return DiaryEntry(id, Date(timeCreated), Date(timeModified), goodBadScore, tags, title,
                          subtitle, content,
                          images?.values?.toList()?.map(FirebaseDiaryImage::toNormalData),
                          location["lat"]!!, location["long"]!!)
    }
}

data class DiaryImage(
        var id: String = "",
        val description: String = "",
        val uri: Uri = Uri.EMPTY,
        val timeCreated: Date = Calendar.getInstance().time
){
    fun toFirebaseData(): FirebaseDiaryImage {
        return FirebaseDiaryImage(id, description, uri.toString(), timeCreated.time)
    }
}

data class FirebaseDiaryImage(
        var id: String = "",
        val description: String = "",
        val uri: String = Uri.EMPTY.toString(),
        val timeCreated: Long = 0L
){

    fun toNormalData(): DiaryImage {
        return DiaryImage(id, description, Uri.parse(uri), Date(timeCreated))
    }

    override fun equals(other: Any?): Boolean {
        if(this === other) return true
        if(javaClass != other?.javaClass) return false

        other as FirebaseDiaryImage

        if(id != other.id) return false
        if(description != other.description) return false
        if(uri != other.uri) return false
        if(timeCreated != other.timeCreated) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + uri.hashCode()
        result = 31 * result + timeCreated.hashCode()
        return result
    }

    companion object{
        fun getFirebaseStoragePath(entryId: String): String {
            return "image/$entryId/"
        }
    }

}