package io.dairyly.dairyly

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.dairyly.dairyly.data.AppDatabase
import io.dairyly.dairyly.data.EnFileDao
import io.dairyly.dairyly.data.UserDao
import io.dairyly.dairyly.data.models.UserDetail
import io.dairyly.dairyly.data.models.UserFile
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit


@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class RxDatabaseFundamentalTest {

    private lateinit var userDao: UserDao
    private lateinit var enFileDao: EnFileDao
    private lateinit var db: AppDatabase

    private lateinit var user: UserDetail
    private lateinit var file: UserFile


    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun createDatabase() {
        db = Room.inMemoryDatabaseBuilder(
                InstrumentationRegistry.getInstrumentation().targetContext, AppDatabase::class.java)
                .allowMainThreadQueries()
                .build()

        userDao = db.userInfoDao()
        enFileDao = db.userFileDao()

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
        runBlocking {
            val a = userDao.insert(user)
            val f = enFileDao.insert(file)
        }
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun writeUserAndRead() {
        val dbUser = userDao.getRowById(user.userId).test()
                .awaitDone(1, TimeUnit.SECONDS)
                .assertValue {
            return@assertValue user.userId == it.detail.userId
        }

        val dbFile = enFileDao.getRowById(file.fileId)
                .test()
                .awaitDone(1, TimeUnit.SECONDS)
                .assertValue {
            return@assertValue file.fileId == it.fileId
        }
    }

    @Test
    fun listUsers() {
        val allUser = userDao.getAll().test().awaitDone(1, TimeUnit.SECONDS)

        // allUser.assertComplete()
        allUser.assertNoErrors()
        allUser.assertValueCount(1)
        allUser.assertValue {
            println(it)
            it.isNotEmpty() && it.size == 1
        }
        allUser.dispose()
    }

    @Test
    fun listOneUserById(){
        val allUser = userDao.getRowById(user.userId).test().awaitDone(1, TimeUnit.SECONDS)

        // allUser.assertComplete()
        allUser.assertNoErrors()
        allUser.assertValueCount(1)
        allUser.assertValue {
            println(it)
            it.detail.username == "anonk"
        }
        allUser.dispose()
    }

}