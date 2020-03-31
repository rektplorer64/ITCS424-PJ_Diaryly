package io.dairyly.dairyly.data

import io.dairyly.dairyly.data.models.*
import io.dairyly.dairyly.utils.formatToServerDateDefaults
import net.andreinc.mockneat.MockNeat
import net.andreinc.mockneat.types.enums.HostNameType
import java.io.File
import java.time.LocalDate
import java.util.*
import kotlin.random.asKotlinRandom

class DairylyGenerator(
        private val m: MockNeat,
        val totalUsers: Int,
        val totalFiles: Int,
        val totalEntryBlocks: Int,
        val totalEntries: Int,
        val totalTags: Int
) {

    val users: List<UserDetail>
    val files: List<UserFile>
    val entryBlocks: List<DiaryEntryBlockInfo>
    val entries: List<DiaryEntryInfo>
    val tags: List<Tag>
    val entryTagsCrossRefs: List<DiaryEntryTagCrossRef>

    init {
        users = generateUserDetailObjects()
        files = generateUserFileObjects()
        entryBlocks = generateDairyEntryBlockObjects()
        entries = generateDairyEntryObjects()
        tags = generateTagsObjects()
        entryTagsCrossRefs = generateEntryTagCrossRef()
    }

    private fun generateEntryTagCrossRef(): List<DiaryEntryTagCrossRef> {
        val crossRef = mutableListOf<DiaryEntryTagCrossRef>()
        for(i in 1 .. totalEntries){
            val numberOfTags = m.random.asKotlinRandom().nextInt(3, 8)
            val tagSet = hashSetOf<Int>()
            for(j in 1 .. numberOfTags){
                val tagId = m.random.asKotlinRandom().nextInt(1, totalTags)
                tagSet.add(tagId)
            }

            for(t in tagSet){
                crossRef += DiaryEntryTagCrossRef(i, t)
            }
        }
        return crossRef
    }

    private fun generateUserDetailObjects(): List<UserDetail> {
        val users = mutableListOf<UserDetail>()
        for(i in 1 .. totalUsers) {
            val username = m.regex("[A-Z]{3}[0-9]{2}").`val`()
            val fName = m.names().first().`val`()

            var mName = m.probabilites(String::class.java)
                    .add(0.8, "")
                    .add(0.2, m.names().first()).`val`()
            if(mName == "") {
                mName = null
            }

            val lName = m.names().last().`val`()
            val email = m.emails().`val`()
            val dateOfBirth =
                    m.localDates().past(LocalDate.ofYearDay(1998, 43)).toUtilDate().`val`()
            val dateCreated = m.localDates().past(LocalDate.of(2020, 1, 1)).toUtilDate().`val`()
            val password = m.passwords().strong().`val`()
            val description = m.strings().size(100).`val`()

            users += UserDetail(i, username, fName, mName, lName, email, dateOfBirth, dateCreated,
                                password, description)
        }
        return users
    }

    private fun generateUserFileObjects(): List<UserFile> {
        val userFiles = mutableListOf<UserFile>()
        for(i in 1 .. totalFiles) {
            val location = m.urls().host(HostNameType.NOUN_FIRST_NAME).`val`()
            val type = m.probabilites(UserFile.Type::class.java)
                    .add(0.25, UserFile.Type.AUDIO)
                    .add(0.25, UserFile.Type.IMAGE)
                    .add(0.25, UserFile.Type.VIDEO)
                    .add(0.25, UserFile.Type.OTHER).`val`()
            val timeAdded = m.localDates().past(LocalDate.of(2020, 1, 1)).toUtilDate().`val`()
            val ownerId = m.random.asKotlinRandom().nextInt(1, totalUsers + 1)
            userFiles += UserFile(i, File(location), type, timeAdded, ownerId)
        }
        return userFiles
    }

    private fun generateDairyEntryBlockObjects(): List<DiaryEntryBlockInfo> {
        val blocks = mutableListOf<DiaryEntryBlockInfo>()
        for(i in 1 .. totalEntryBlocks) {
            val parentEntryId = m.ints().range(1, totalEntries + 1).`val`()
            // val timeCreated = m.localDates().past(LocalDate.of(2020, 1, 1)).toUtilDate().`val`()
            val timeCreated = Calendar.getInstance().time
            val type = m.probabilites(DiaryEntryBlockInfo.Type::class.java)
                    .add(0.1, DiaryEntryBlockInfo.Type.HEADER1)
                    .add(0.1, DiaryEntryBlockInfo.Type.HEADER2)
                    .add(0.2, DiaryEntryBlockInfo.Type.HEADER3)
                    .add(0.3, DiaryEntryBlockInfo.Type.IMAGE)
                    .add(0.3, DiaryEntryBlockInfo.Type.TEXT).`val`()
            val content = m.words().adverbs().array(100).`val`().reduce { acc, s -> "$acc $s" }
            val fileId = m.ints().range(1, totalFiles + 1).`val`()
            val order = m.ints().bound(100).`val`()
            blocks += DiaryEntryBlockInfo(i, parentEntryId, timeCreated, type, content,
                                          fileId, order)
        }
        return blocks
    }

    private fun generateDairyEntryObjects(): List<DiaryEntryInfo> {
        val entryInfo = mutableListOf<DiaryEntryInfo>()

        val c = Calendar.getInstance(TimeZone.getDefault())
        val nowDay = c.get(Calendar.DAY_OF_MONTH)
        val nowMonth = c.get(Calendar.MONTH) + 1
        val nowYrs = c.get(Calendar.YEAR)

        val referenceDate = LocalDate.of(nowYrs, nowMonth, nowDay).plusDays(-3)

        for(i in 1 .. totalEntries) {
            val userId = m.ints().range(1, totalUsers + 1).`val`()
            val timeModified =
                    m.localDates().between(referenceDate, LocalDate.now()).toUtilDate()
                            .`val`()

            val timeCreated = m.localDates().past(LocalDate.parse(timeModified.formatToServerDateDefaults()))
                    .toUtilDate().`val`()
            // val timeCreated = m.localDates().past(LocalDate.of(2020, 1, 1)).toUtilDate().`val`()
            val goodBad = m.probabilites(DiaryEntryInfo.GoodBad::class.java)
                    .add(0.3, DiaryEntryInfo.GoodBad.BAD)
                    .add(0.7, DiaryEntryInfo.GoodBad.GOOD)
                    .`val`()

            entryInfo += DiaryEntryInfo(i, userId, timeCreated, timeModified, goodBad)
        }
        return entryInfo
    }

    private fun generateTagsObjects(): List<Tag> {
        val tagInfo = mutableListOf<Tag>()
        for(i in 1 .. totalTags) {
            val string = m.words().nouns().`val`()
            val timeCreated = m.localDates().past(LocalDate.of(2020, 1, 1)).toUtilDate().`val`()
            tagInfo += Tag(i, string, timeCreated)
        }
        return tagInfo
    }
}

