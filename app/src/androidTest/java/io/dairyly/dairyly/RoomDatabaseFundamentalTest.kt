package io.dairyly.dairyly

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.dairyly.dairyly.data.AppDatabase
import io.dairyly.dairyly.data.FileDao
import io.dairyly.dairyly.data.UserDao
import io.dairyly.dairyly.data.models.UserDetail
import io.dairyly.dairyly.data.models.UserFile
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.IOException
import java.util.*


@RunWith(AndroidJUnit4::class)
class RoomDatabaseFundamentalTest {

    @ExperimentalCoroutinesApi
    private val testDispatcher = TestCoroutineDispatcher()
    @ExperimentalCoroutinesApi
    private val testScope = TestCoroutineScope(testDispatcher)

    private lateinit var userDao: UserDao
    private lateinit var fileDao: FileDao
    private lateinit var db: AppDatabase

    private lateinit var user: UserDetail
    private lateinit var file: UserFile

    @ExperimentalCoroutinesApi
    @Before
    fun createDatabase() {
        db = Room.inMemoryDatabaseBuilder(
                InstrumentationRegistry.getInstrumentation().targetContext, AppDatabase::class.java)
                .setTransactionExecutor(testDispatcher.asExecutor())
                .setQueryExecutor(testDispatcher.asExecutor())
                .build()

        userDao = db.userInfoDao()
        fileDao = db.userFileDao()

        user = UserDetail(1, "anonk",
                          "Anon",
                          null,
                          "Kangpanich",
                          "anon.k@gmail.com",
                          Calendar.getInstance().time,
                          Calendar.getInstance().time,
                          "1234",
                          "Lorem Text")

        file = UserFile(1,
                        File("sadas/sadas/23.png"),
                        UserFile.Type.IMAGE,
                        Calendar.getInstance().time,
                        1)
        val a = userDao.insert(user)
        val f = fileDao.insert(file)
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun writeUserAndRead() {
        val dbUser = userDao.getRowById(user.userId).blockingFirst()
        assertEquals(user.userId, dbUser.userId)

        val dbFile = fileDao.getRowById(file.fileId).blockingFirst()
        assertEquals(file.fileId, dbFile.fileId)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun writeUserAndReadInWithCoroutine() {

        val allUser = userDao.getAll().toList()
        println("allUser => $allUser")

        println("Getting User Info for ID = ${user.userId}")
        val userInfoResult = userDao.getRowById(user.userId).take(1).toList().blockingGet()
        println("${userInfoResult}, $user")

        assertEquals(user.fName, userInfoResult[0].fName)
        // assertEquals(userInfoResult[0], user)

        // assertEquals(userInfoDao.getRowByIdx(a.toInt()), user)
        // fileDao.getAll().collect{
        //     assertEquals(it[0], file)
        // }

        // val result = userDao.getUserById(user.id).take(1).toList()

        // println(result)

        // assertEquals(it.user.fName, user.fName)
        // println("${it.userFiles[0].id}\t${i}")
        // assertEquals(result[0].userFiles[0].id, file.id)

    }
}