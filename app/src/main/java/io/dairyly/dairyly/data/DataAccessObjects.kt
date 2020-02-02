package io.dairyly.dairyly.data

import androidx.room.*
import androidx.room.OnConflictStrategy.REPLACE
import io.dairyly.dairyly.data.models.DairyEntry
import io.dairyly.dairyly.data.models.UserDetail
import io.dairyly.dairyly.data.models.UserFile
import io.reactivex.Flowable

interface BasicDaoInterface<T> {
    fun getAll(): Flowable<List<T>>
    fun getRowById(id: Int): Flowable<T>
    fun insert(row: T): Long
    fun update(row: T): Int
    fun delete(row: T): Int
}

@Dao
interface UserDao : BasicDaoInterface<UserDetail> {
    @Transaction
    @Query("SELECT * FROM UserDetail")
    override fun getAll(): Flowable<List<UserDetail>>

    @Transaction
    @Query("SELECT * FROM UserDetail WHERE userId = :id LIMIT 1")
    override fun getRowById(id: Int): Flowable<UserDetail>

    @Insert(onConflict = REPLACE)
    override fun insert(row: UserDetail): Long

    @Update
    override fun update(row: UserDetail): Int

    @Delete
    override fun delete(row: UserDetail): Int
}

@Dao
interface FileDao : BasicDaoInterface<UserFile> {
    @Query("SELECT * FROM UserFile")
    override fun getAll(): Flowable<List<UserFile>>

    @Query("SELECT * FROM UserFile WHERE fileId = :id")
    override fun getRowById(id: Int): Flowable<UserFile>

    @Insert(onConflict = REPLACE)
    override fun insert(row: UserFile): Long

    @Update
    override fun update(row: UserFile): Int

    @Delete
    override fun delete(row: UserFile): Int
}

@Dao
interface DairyEntryDao : BasicDaoInterface<DairyEntry> {
    @Transaction
    @Query("SELECT * FROM DairyEntryInfo")
    override fun getAll(): Flowable<List<DairyEntry>>

    @Transaction
    @Query("SELECT * FROM DairyEntryInfo WHERE entryId = :id")
    override fun getRowById(id: Int): Flowable<DairyEntry>

    @Insert(onConflict = REPLACE)
    override fun insert(row: DairyEntry): Long

    @Update
    override fun update(row: DairyEntry): Int

    @Delete
    override fun delete(row: DairyEntry): Int
}