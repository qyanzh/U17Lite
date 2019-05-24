package com.example.u17lite.dataBeans

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface DownloadDao {
    @Query("SELECT * FROM downloaditem ORDER BY downloadId asc")
    fun getAll(): List<DownloadItem>

    @Query("SELECT * FROM downloaditem WHERE comicId = :comicId")
    fun getComicChapter(comicId: Long): List<DownloadItem>

    @Query("SELECT * FROM downloaditem WHERE chapterId = :chapterId")
    fun getChapter(chapterId: Long): DownloadItem?

    @Query("SELECT * FROM downloaditem WHERE downloadId = (SELECT min(downloadId) FROM downloaditem)")
    fun getNext(): DownloadItem?

    @Insert
    fun insert(vararg downloadItem: DownloadItem)

    @Delete
    fun delete(vararg downloadItem: DownloadItem)

}