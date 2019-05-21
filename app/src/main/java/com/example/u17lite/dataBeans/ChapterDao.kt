package com.example.u17lite.dataBeans

import androidx.room.*
import androidx.room.OnConflictStrategy.REPLACE

@Dao
interface ChapterDao {
    @Query("SELECT * FROM chapter")
    fun getAll(): List<Chapter>

    @Query("SELECT * FROM chapter WHERE chapterId IN (:chapterIds)")
    fun loadAllByIds(chapterIds: LongArray): List<Chapter>

    @Insert(onConflict = REPLACE)
    fun insertAll(vararg users: Chapter)

    @Query("SELECT * FROM chapter WHERE chapterId = :chapter")
    fun isExist(chapter: Long): Boolean

    @Update
    fun update(vararg chapters: Chapter)

    @Update
    fun update(chapters: List<Chapter>)

    @Delete
    fun delete(user: Chapter)
}