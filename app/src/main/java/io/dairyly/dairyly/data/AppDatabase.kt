package io.dairyly.dairyly.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import io.dairyly.dairyly.data.converters.*
import io.dairyly.dairyly.data.models.UserDetail
import io.dairyly.dairyly.data.models.UserDetailFileCrossRef
import io.dairyly.dairyly.data.models.UserFile

@Database(entities = [UserDetail::class, UserFile::class, UserDetailFileCrossRef::class], version = 1)
@TypeConverters(DateConverter::class, UserFileTypeConverter::class, FileConverter::class)
abstract class AppDatabase : RoomDatabase(){
    abstract fun userInfoDao(): UserDao
    abstract fun userFileDao(): FileDao

    companion object{
        private lateinit var INSTANCE: AppDatabase

        private val migration1to2 = object : Migration(1, 2){
            override fun migrate(database: SupportSQLiteDatabase) {
                // TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        }

        fun getInstance(context: Context): AppDatabase {
            var returningInstance: AppDatabase
            synchronized(this){
                if(::INSTANCE.isInitialized) {
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