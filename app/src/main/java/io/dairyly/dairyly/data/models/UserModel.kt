package io.dairyly.dairyly.data.models

import androidx.room.*
import androidx.room.ForeignKey.CASCADE
import java.io.File
import java.util.*

@Entity
data class UserDetail(
        @PrimaryKey(autoGenerate = true)
        val userId: Int,
        val username: String,

        val fName: String,
        val mName: String?,
        val lName: String,

        val email: String,
        val dob: Date,
        val timeCreated: Date,
        val password: String,
        val description: String
)

@Entity(foreignKeys = [ForeignKey(
        entity = UserDetail::class,
        parentColumns = ["userId"],
        childColumns = ["ownerId"],
        onDelete = ForeignKey.CASCADE)],
        indices = [Index(
                "ownerId")])
data class UserFile(
        @PrimaryKey(autoGenerate = true)
        var fileId: Int,
        var location: File,
        var type: Type,
        var timeAdded: Date,
        var ownerId: Int
) {
    enum class Type {
        AUDIO, IMAGE, VIDEO, OTHER
    }
}

@Entity(primaryKeys = ["userId", "fileId"],
        indices = [Index(
                "fileId")],
        foreignKeys = [
            ForeignKey(entity = UserFile::class,
                       parentColumns = ["fileId"],
                       childColumns = ["fileId"],
                       onDelete = CASCADE),
            ForeignKey(entity = UserDetail::class,
                       parentColumns = ["userId"],
                       childColumns = ["userId"],
                       onDelete = CASCADE)
        ])
data class UserDetailFileCrossRef(
        val userId: Int,
        val fileId: Int
)

data class User(
        @Embedded
        val detail: UserDetail,

        @Relation(
                parentColumn = "userId",
                entityColumn = "fileId",
                associateBy = Junction(
                        UserDetailFileCrossRef::class)
        )
        val files: List<UserFile>
)

data class UserFileToUserDetail(
        @Embedded
        val file: UserFile,

        @Relation(
                parentColumn = "fileId",
                entityColumn = "userId",
                associateBy = Junction(
                        UserDetailFileCrossRef::class)
        )
        val userDetails: List<UserDetail>
)