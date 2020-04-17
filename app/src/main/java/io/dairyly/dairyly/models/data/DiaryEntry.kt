package io.dairyly.dairyly.models.data

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.annotation.ColorInt
import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties
import io.dairyly.dairyly.R
import io.dairyly.dairyly.utils.calculateForegroundColorToPair
import java.util.*
import kotlin.collections.HashMap

const val DEFAULT_COLOR = -1
const val DEFAULT_ID = "NO___ID"

class DiaryEntry(
        var id: String,
        var title: String,
        var subtitle: String,
        var content: String,
        var tags: List<DiaryTag>,
        var goodBadScore: Int,
        @ColorInt
        var color: Int = DEFAULT_COLOR,
        val timeCreated: Date,
        var timeModified: Date,
        var images: List<DiaryImage>?,
        lat: Double,
        long: Double
) {
    var location: Pair<Double, Double> = Pair(lat, long)

    fun toFirebaseData(): FirebaseDiaryEntry {
        val hashMap = if(!images.isNullOrEmpty()) {
            hashMapOf<String, FirebaseDiaryImage>().apply {
                var defaultIdCount = 0
                for(i in images!!.indices) {
                    val key = if(images!![i].id == DEFAULT_ID){
                        defaultIdCount++.toString()
                    }else{
                        images!![i].id
                    }
                    this[key] = images!![i].toFirebaseData()
                }
            }
        } else {
            null
        }

        return FirebaseDiaryEntry(id, title, content, subtitle, tags,
                                  goodBadScore, color, timeCreated.time, timeModified.time, hashMap,
                                  hashMapOf("lat" to location.first, "long" to location.second))
    }

    override fun toString(): String {
        return "DiaryEntry(id='$id', timeCreated=$timeCreated, timeModified=$timeModified, goodBadScore=$goodBadScore, tags=$tags, title='$title', subtitle='$subtitle', content='$content', images=$images, location=$location)"
    }

    override fun equals(other: Any?): Boolean {
        if(this === other) return true
        if(javaClass != other?.javaClass) return false

        other as DiaryEntry

        if(id != other.id) return false
        if(title != other.title) return false
        if(subtitle != other.subtitle) return false
        if(content != other.content) return false
        if(tags != other.tags) return false
        if(goodBadScore != other.goodBadScore) return false
        if(color != other.color) return false
        if(timeCreated != other.timeCreated) return false
        if(timeModified != other.timeModified) return false
        if(images != other.images) return false
        if(location != other.location) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + subtitle.hashCode()
        result = 31 * result + content.hashCode()
        result = 31 * result + tags.hashCode()
        result = 31 * result + goodBadScore
        result = 31 * result + color
        result = 31 * result + timeCreated.hashCode()
        result = 31 * result + timeModified.hashCode()
        result = 31 * result + (images?.hashCode() ?: 0)
        result = 31 * result + location.hashCode()
        return result
    }


}

fun DiaryEntry.hasSpecifiedColor(): Boolean {
    return this.color != DEFAULT_COLOR || this.color == 0
}

fun DiaryEntry.getForegroundAndAccentColor(context: Context): Pair<Int, Int> {
    return if(hasSpecifiedColor()) {
        calculateForegroundColorToPair(this.color)
    } else {
        calculateForegroundColorToPair(context.getColor(R.color.colorPrimaryDark))
    }
}

@IgnoreExtraProperties
data class FirebaseDiaryEntry(
        var id: String = "",
        var title: String = "",
        var content: String = "",
        var subtitle: String = "",
        var tags: List<DiaryTag> = arrayListOf(),
        var goodBadScore: Int = 0,
        @ColorInt
        var color: Int = DEFAULT_COLOR,
        val timeCreated: Long = 0L,
        var timeModified: Long = 0L,
        var images: HashMap<String, FirebaseDiaryImage>? = HashMap(),
        var location: HashMap<String, Double> = hashMapOf(
                "lat" to 0.0, "long" to 0.0)
) {

    fun toNormalData(): DiaryEntry {

        return DiaryEntry(id, title, subtitle, content, tags, goodBadScore,
                          color, Date(timeCreated),
                          Date(timeModified),
                          images?.values?.toList()?.map(FirebaseDiaryImage::toNormalData),
                          location["lat"]!!, location["long"]!!)
    }
}

data class DiaryImage(
        var id: String = DEFAULT_ID,
        val description: String = "",
        val uri: Uri = Uri.EMPTY,
        val timeCreated: Date = Calendar.getInstance().time,
        val entryId: String = "",
        val isUploaded: Boolean = false,
        val imageBitmap: Bitmap? = null,
        var markedForDeletion: Boolean = false
) {
    fun toFirebaseData(): FirebaseDiaryImage {
        return FirebaseDiaryImage(id, description, uri.toString(), timeCreated.time, entryId)
    }
}

class FirebaseDiaryImage(
        var id: String = "",
        val description: String = "",
        val uri: String = Uri.EMPTY.toString(),
        val timeCreated: Long = Calendar.getInstance().timeInMillis,
        var entryId: String = "",
        @get:Exclude var isUploaded: Boolean = false
) {

    @get:Exclude
    val markedForDeletion = false

    fun toNormalData(): DiaryImage {
        return DiaryImage(id, description, Uri.parse(uri), Date(timeCreated), entryId, true)
    }

    override fun equals(other: Any?): Boolean {
        if(this === other) return true
        if(javaClass != other?.javaClass) return false

        other as FirebaseDiaryImage

        if(id != other.id) return false
        if(description != other.description) return false
        if(uri != other.uri) return false
        if(timeCreated != other.timeCreated) return false
        if(entryId != other.entryId) return false
        if(markedForDeletion != other.markedForDeletion) return false
        if(isUploaded != other.isUploaded) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + uri.hashCode()
        result = 31 * result + timeCreated.hashCode()
        result = 31 * result + entryId.hashCode()
        result = 31 * result + markedForDeletion.hashCode()
        result = 31 * result + isUploaded.hashCode()
        return result
    }


    companion object {
        fun getFirebaseStoragePath(entryId: String): String {
            return "image/$entryId/"
        }
    }

}