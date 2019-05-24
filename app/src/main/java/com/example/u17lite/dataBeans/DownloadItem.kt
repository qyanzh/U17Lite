package com.example.u17lite.dataBeans


import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class DownloadItem(
    val comicId: Long,
    val chapterId: Long,
    val url: String
) {
    @PrimaryKey(autoGenerate = true)
    var downloadId: Long = 0

}
