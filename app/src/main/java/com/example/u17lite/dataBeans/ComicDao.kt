package com.example.u17lite.dataBeans

import androidx.room.*
import androidx.room.OnConflictStrategy.REPLACE

@Dao
interface ComicDao {
    @Query("SELECT * FROM comic")
    fun getAll(): List<Comic>

    @Query("SELECT * FROM comic WHERE comicId IN (:comicIds)")
    fun loadAllByIds(comicIds: LongArray): List<Comic>

    @Query("SELECT * FROM comic WHERE comicId = :comicId")
    fun find(comicId: Long): Comic

    @Insert(onConflict = REPLACE)
    fun insertAll(vararg users: Comic)

    @Query("SELECT * FROM comic WHERE comicId = :comic")
    fun isExist(comic: Long): Boolean

    @Update
    fun update(vararg comics: Comic)

    @Update
    fun update(chapters: List<Comic>)

    @Delete
    fun delete(user: Comic)
}