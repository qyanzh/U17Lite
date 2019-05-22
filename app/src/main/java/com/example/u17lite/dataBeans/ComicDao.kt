package com.example.u17lite.dataBeans

import androidx.room.*
import androidx.room.OnConflictStrategy.REPLACE

@Dao
interface ComicDao {

    @Query("SELECT * FROM comic WHERE isSubscribed = 1 ORDER BY lastUpdateTime")
    fun getSubscribedList(): List<Comic>

    @Query("SELECT * FROM comic")
    fun getAll(): List<Comic>

    @Query("SELECT * FROM comic WHERE comicId = :comicId")
    fun find(comicId: Long): Comic?

    @Insert(onConflict = REPLACE)
    fun insert(vararg comics: Comic)

    @Delete
    fun delete(vararg comics: Comic)

    @Update
    fun update(vararg comics: Comic)

}