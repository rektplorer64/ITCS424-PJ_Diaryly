package io.dairyly.dairyly.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import io.dairyly.dairyly.data.converters.*
import io.dairyly.dairyly.data.models.*
import kotlinx.coroutines.runBlocking

@Database(
        entities = [UserDetail::class, UserFile::class, UserDetailFileCrossRef::class,
            DairyEntryInfo::class, Tag::class, DairyEntryTagCrossRef::class, DairyEntryBlockInfo::class, DairyEntryBlockCrossRef::class],
        version = 1)
@TypeConverters(DateConverter::class, UserFileTypeConverter::class, FileConverter::class, GoodBadConverter::class, BlockTypeConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userInfoDao(): UserDao
    abstract fun userFileDao(): EnFileDao
    abstract fun dairyEntryDao(): DairyEntryDao
    abstract fun enDairyEntryBlockInfoDao(): EnDairyEntryBlockInfoDao
    abstract fun enTagDao(): EnTagDao

    companion object {
        @Volatile
        private lateinit var INSTANCE: AppDatabase

        private val migration1to2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        }

        fun getInstance(context: Context): AppDatabase {
            var returningInstance: AppDatabase
            synchronized(this) {
                if(!::INSTANCE.isInitialized) {
                    INSTANCE = Room
                            .databaseBuilder(context.applicationContext,
                                             AppDatabase::class.java, "MainLocalDb")
                            .addMigrations(migration1to2)
                            .build()
                }
                returningInstance = INSTANCE
            }
            return returningInstance
        }
    }
}

fun AppDatabase.populateDatabase(gen: DairylyGenerator){

    val db = this

    runBlocking{
        val userDao = db.userInfoDao()
        userDao.insert(*gen.users.toTypedArray())

        val enFileDao = db.userFileDao()
        enFileDao.insert(*gen.files.toTypedArray())

        val dairyEntryDao = db.dairyEntryDao()
        dairyEntryDao.insert(*gen.entries.toTypedArray())

        val dairyEntryBlockInfoDao = db.enDairyEntryBlockInfoDao()
        dairyEntryBlockInfoDao.insert(*gen.entryBlocks.toTypedArray())

        val tagDao = db.enTagDao()
        tagDao.insert(*gen.tags.toTypedArray())
    }
}