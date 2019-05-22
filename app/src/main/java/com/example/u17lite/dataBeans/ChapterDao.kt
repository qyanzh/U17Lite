package com.example.u17lite.dataBeans

import androidx.room.*
import androidx.room.OnConflictStrategy.REPLACE

@Dao
interface ChapterDao {
    @Query("SELECT * FROM chapter")
    fun getAll(): List<Chapter>

    @Query("SELECT * FROM chapter WHERE chapterId = :chapterId")
    fun find(chapterId: Long): Chapter?

    @Insert(onConflict = REPLACE)
    fun insert(vararg users: Chapter)

    @Update
    fun update(vararg chapters: Chapter)

    @Delete
    fun delete(chapter: Chapter)
}