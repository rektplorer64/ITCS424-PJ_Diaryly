package io.dairyly.dairyly.data.models

import androidx.room.*
import androidx.room.ForeignKey.CASCADE
import java.util.*

@Entity(foreignKeys = [ForeignKey(entity = UserDetail::class,
                                  parentColumns = ["userId"],
                                  childColumns = ["userId"],
                                  onDelete = CASCADE)],
        indices = [Index("userId")])
data class DairyEntryInfo(
        @PrimaryKey(autoGenerate = true)
        val entryId: Int,
        val userId: Int,
        val timeCreated: Date,
        val timeModified: Date,
        val goodBad: GoodBad
) {
    enum class GoodBad {
        GOOD, BAD
    }
}

@Entity
data class Tag(
        @PrimaryKey(autoGenerate = true)
        val tagNumber: Int,
        val string: String,
        val timeCreated: Date
)

@Entity(primaryKeys = ["entryId", "tagNumber"],
        indices = [Index("tagNumber")],
        foreignKeys = [
            ForeignKey(entity = Tag::class,
                       parentColumns = ["tagNumber"],
                       childColumns = ["tagNumber"],
                       onDelete = CASCADE),
            ForeignKey(entity = DairyEntryInfo::class,
                       parentColumns = ["entryId"],
                       childColumns = ["tagNumber"],
                       onDelete = CASCADE)
        ])
data class DairyEntryTagCrossRef(
        val entryId: Int,
        val tagNumber: Int
)

data class DiaryEntry(
        @Embedded
        val info: DairyEntryInfo,

        @Relation(
                parentColumn = "entryId",
                entityColumn = "tagNumber",
                entity = Tag::class,
                associateBy = Junction(DairyEntryTagCrossRef::class)
        )
        val tags: List<Tag>,

        @Relation(
                parentColumn = "entryId",
                entity = DairyEntryBlockInfo::class,
                entityColumn = "parentEntryId"
        )
        val blockInfo: List<DairyEntryBlockInfo>
)


@Entity(foreignKeys = [ForeignKey(entity = DairyEntryInfo::class,
                                  parentColumns = ["entryId"],
                                  childColumns = ["parentEntryId"],
                                  onDelete = CASCADE)],
        indices = [Index("parentEntryId")])
data class DairyEntryBlockInfo(
        @PrimaryKey
        val blockNum: Int,

        val parentEntryId: Int,
        val timeCreated: Date,
        val type: Type,
        val content: String,
        val fileId: Int?,
        val order: Int
) {
    enum class Type {
        HEADER1, HEADER2, HEADER3, IMAGE, TEXT
    }
}