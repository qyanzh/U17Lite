package com.example.u17lite.dataBeans

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity
class Chapter(
    @PrimaryKey val chapterId: Long,
    val name: String,
    val smallCoverURL: String,
    val publishTime: Long
)