package io.dairyly.dairyly.data

import androidx.room.*
import androidx.room.OnConflictStrategy.REPLACE
import io.dairyly.dairyly.data.models.*
import io.reactivex.Flowable
import io.reactivex.Single

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
interface DairyEntryDao : BasicDaoInterface<DairyEntry, DairyEntryInfo> {
    @Transaction
    @Query("SELECT * FROM DairyEntryInfo")
    override fun getAll(): Flowable<List<DairyEntry>>

    @Transaction
    @Query("SELECT * FROM DairyEntryInfo WHERE entryId = :id")
    override fun getRowById(id: Int): Flowable<DairyEntry>

    @Transaction
    @Query("SELECT * FROM DairyEntryInfo WHERE userId = :id")
    fun getRowByUserId(id: Int): Flowable<List<DairyEntry>>

    @Transaction
    @Query("SELECT * FROM DairyEntryInfo WHERE (userId = :id) AND (timeCreated BETWEEN :start AND :end)")
    fun getRowsByTimeRange(id: Int, start: Long, end: Long): Flowable<List<DairyEntry>>

    @Query("SELECT Max(entryId) FROM DairyEntryInfo")
    fun getLatestDiaryEntryId(): Single<Int>

    @Insert(onConflict = REPLACE)
    override suspend fun insert(vararg row: DairyEntryInfo): List<Long>

    @Update
    override suspend fun update(row: DairyEntryInfo): Int

    @Delete
    override suspend fun delete(row: DairyEntryInfo): Int
}

@Dao
interface EnDairyEntryBlockInfoDao: BasicDaoInterface<DairyEntryBlockInfo, DairyEntryBlockInfo>{

    @Query("SELECT * FROM DairyEntryBlockInfo")
    override fun getAll(): Flowable<List<DairyEntryBlockInfo>>

    @Query("SELECT * FROM DairyEntryBlockInfo WHERE fileId = :id")
    override fun getRowById(id: Int): Flowable<DairyEntryBlockInfo>

    @Insert(onConflict = REPLACE)
    override suspend fun insert(vararg row: DairyEntryBlockInfo): List<Long>

    @Update
    override suspend fun update(row: DairyEntryBlockInfo): Int

    @Delete
    override suspend fun delete(row: DairyEntryBlockInfo): Int
}

@Dao
interface EnTagDao: BasicDaoInterface<Tag, Tag>{

    @Query("SELECT * FROM Tag")
    override fun getAll(): Flowable<List<Tag>>

    @Query("SELECT * FROM Tag WHERE tagNumber = :id")
    override fun getRowById(id: Int): Flowable<Tag>

    @Insert(onConflict = REPLACE)
    override suspend fun insert(vararg row: Tag): List<Long>

    @Update
    override suspend fun update(row: Tag): Int

    @Delete
    override suspend fun delete(row: Tag): Int
}