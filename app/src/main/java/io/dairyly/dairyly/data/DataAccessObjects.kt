package io.dairyly.dairyly.data

import androidx.room.*
import androidx.room.OnConflictStrategy.REPLACE
import io.dairyly.dairyly.data.models.*
import io.reactivex.Flowable
import io.reactivex.Single


import java.util.*

/**
 * Base Interface for Data access objects classes
 * @param T the type of output of main queries
 * @param I the type of input of the main queries
 */
interface BasicDaoInterface<T, I> {
    /**
     * List all rows of a specified database table
     */
    fun getAll(): Flowable<List<T>>

    /**
     * List a row which is identified by an Id of type Integer
     */
    fun getRowById(id: Int): Flowable<T>

    /**
     * Insert rows of type I into a database table
     */
    suspend fun insert(vararg row: I): List<Long>

    /**
     * Update rows of type I in a database table
     */
    suspend fun update(row: I): Int

    /**
     * Remove rows of type I from a database table
     */
    suspend fun delete(row: I): Int
}

@Dao
interface UserDao : BasicDaoInterface<User, UserDetail> {
    @Transaction
    @Query("SELECT * FROM UserDetail")
    override fun getAll(): Flowable<List<User>>

    @Transaction
    @Query("SELECT * FROM UserDetail WHERE userId = :id LIMIT 1")
    override fun getRowById(id: Int): Flowable<User>

    @Insert(onConflict = REPLACE)
    override suspend fun insert(vararg row: UserDetail): List<Long>

    @Update
    override suspend fun update(row: UserDetail): Int

    @Delete
    override suspend fun delete(row: UserDetail): Int
}

@Dao
interface EnFileDao : BasicDaoInterface<UserFile, UserFile> {
    @Query("SELECT * FROM UserFile")
    override fun getAll(): Flowable<List<UserFile>>

    @Query("SELECT * FROM UserFile WHERE fileId = :id")
    override fun getRowById(id: Int): Flowable<UserFile>

    @Insert(onConflict = REPLACE)
    override suspend fun insert(vararg row: UserFile): List<Long>

    @Update
    override suspend fun update(row: UserFile): Int

    @Delete
    override suspend fun delete(row: UserFile): Int
}

@Dao
interface DairyEntryDao : BasicDaoInterface<DiaryEntry, DiaryEntryInfo> {
    @Transaction
    @Query("SELECT * FROM DiaryEntryInfo")
    override fun getAll(): Flowable<List<DiaryEntry>>

    @Transaction
    @Query("SELECT * FROM DiaryEntryInfo WHERE entryId = :id")
    override fun getRowById(id: Int): Flowable<DiaryEntry>

    @Transaction
    @Query("SELECT * FROM DiaryEntryInfo WHERE userId = :id")
    fun getRowByUserId(id: Int): Flowable<List<DiaryEntry>>

    @Transaction
    @Query("SELECT * FROM DiaryEntryInfo WHERE (userId = :userId) AND (timeCreated BETWEEN :start AND :end)")
    fun getRowsByTimeRange(userId: Int, start: Long, end: Long): Flowable<List<DiaryEntry>>

    @Transaction
    @Query("SELECT * FROM DiaryEntryInfo WHERE userId = :userId AND DATE(datetime(timeCreated/1000, 'unixepoch')) = DATE(:dateString)")
    fun getRowsByDate(userId: Int, dateString: String): Flowable<List<DiaryEntry>>

    @Query("SELECT Max(entryId) FROM DiaryEntryInfo")
    fun getLatestDiaryEntryId(): Single<Int>

    @Insert(onConflict = REPLACE)
    override suspend fun insert(vararg row: DiaryEntryInfo): List<Long>

    @Update
    override suspend fun update(row: DiaryEntryInfo): Int

    @Delete
    override suspend fun delete(row: DiaryEntryInfo): Int

    @Query("SELECT SUM(goodBad) FROM DiaryEntryInfo WHERE userId = :userId AND timeCreated BETWEEN :time AND :time1")
    fun getTotalGoodBadScoreInRange(userId: Int, time: Date, time1: Date): Flowable<Int>

    @Query("SELECT cast(timeCreated / 86400000 as int) as date, SUM(goodBad) as goodBadScore FROM DiaryEntryInfo WHERE userId = :userId AND timeCreated BETWEEN :time AND :time1 GROUP BY cast(timeCreated / 86400000 as int) ORDER BY timeCreated")
    fun getGoodBadScoreListInRange(userId: Int, time: Date, time1: Date): Flowable<List<DiaryDateHolder>>

    @Transaction
    @Query("SELECT * FROM DiaryEntryInfo WHERE userId = :userId AND DATE(cast(timeCreated / 86400000 as int)) = DATE(cast(:date / 86400000 as int))")
    fun getRowsByDate(userId: Int, date: Date): Flowable<List<DiaryEntry>>
}

@Dao
interface EnDairyEntryBlockInfoDao: BasicDaoInterface<DiaryEntryBlockInfo, DiaryEntryBlockInfo>{

    @Query("SELECT * FROM DiaryEntryBlockInfo")
    override fun getAll(): Flowable<List<DiaryEntryBlockInfo>>

    @Query("SELECT * FROM DiaryEntryBlockInfo WHERE fileId = :id")
    override fun getRowById(id: Int): Flowable<DiaryEntryBlockInfo>

    @Insert(onConflict = REPLACE)
    override suspend fun insert(vararg row: DiaryEntryBlockInfo): List<Long>

    @Update
    override suspend fun update(row: DiaryEntryBlockInfo): Int

    @Delete
    override suspend fun delete(row: DiaryEntryBlockInfo): Int
}

@Dao
interface EnTagDao: BasicDaoInterface<Tag, Tag>{

    @Query("SELECT * FROM Tag")
    override fun getAll(): Flowable<List<Tag>>

    @Query("SELECT * FROM DiaryEntryTagCrossRef")
    fun getAllTagCrossRef(): Flowable<List<DiaryEntryTagCrossRef>>

    @Query("SELECT * FROM Tag WHERE tagNumber = :id")
    override fun getRowById(id: Int): Flowable<Tag>

    @Insert(onConflict = REPLACE)
    suspend fun insert(vararg row: DiaryEntryTagCrossRef): List<Long>

    @Insert(onConflict = REPLACE)
    override suspend fun insert(vararg row: Tag): List<Long>

    @Update
    override suspend fun update(row: Tag): Int

    @Delete
    override suspend fun delete(row: Tag): Int
}