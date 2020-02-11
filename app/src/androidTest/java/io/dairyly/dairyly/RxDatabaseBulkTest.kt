package io.dairyly.dairyly

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.dairyly.dairyly.data.*
import io.dairyly.dairyly.utils.DATA_GEN_OPTION
import kotlinx.coroutines.ExperimentalCoroutinesApi
import net.andreinc.mockneat.MockNeat
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException


@RunWith(AndroidJUnit4::class)
class RxDatabaseBulkTest {

    private val totalUsers = DATA_GEN_OPTION["totalUsers"]!!
    private val totalFiles = DATA_GEN_OPTION["totalFiles"]!!
    private val totalEntryBlocks = DATA_GEN_OPTION["totalEntryBlocks"]!!
    private val totalEntries = DATA_GEN_OPTION["totalEntries"]!!
    private val totalTags = DATA_GEN_OPTION["totalTags"]!!

    private lateinit var userDao: UserDao

    private lateinit var enFileDao: EnFileDao
    private lateinit var dairyEntryDao: DairyEntryDao
    private lateinit var dairyEntryBlockInfoDao: EnDairyEntryBlockInfoDao
    private lateinit var tagDao: EnTagDao

    private lateinit var db: AppDatabase

    @ExperimentalCoroutinesApi
    @Before
    fun createDatabase() {

        val gen = DairylyGenerator(MockNeat.threadLocal(), totalUsers, totalFiles, totalEntryBlocks,
                                   totalEntries, totalTags)
        db = Room.inMemoryDatabaseBuilder(
                InstrumentationRegistry.getInstrumentation().targetContext, AppDatabase::class.java)
                .build()

        userDao = db.userInfoDao()
        enFileDao = db.userFileDao()
        dairyEntryDao = db.dairyEntryDao()
        dairyEntryBlockInfoDao = db.enDairyEntryBlockInfoDao()
        tagDao = db.enTagDao()

        db.populateDatabase(gen)
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun writeUserAndRead() {
        assertEquals(userDao.getAll().blockingFirst().size, totalUsers)
        assertEquals(enFileDao.getAll().blockingFirst().size, totalFiles)
        assertEquals(dairyEntryDao.getAll().blockingFirst().size, totalEntries)
        assertEquals(dairyEntryBlockInfoDao.getAll().blockingFirst().size, totalEntryBlocks)
        assertEquals(tagDao.getAll().blockingFirst().size, totalTags)
    }

    @Test
    fun readDairyEntryByUserId(){
        val result = userDao.getRowById(1).blockingFirst()
        println(result.detail)

        for(i in result.files){
            println(i)
            assertEquals(result.detail.userId, i.ownerId)
        }
    }
}