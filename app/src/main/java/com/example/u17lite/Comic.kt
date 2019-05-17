package com.example.u17lite

import android.util.Log

class Comic(
    val title: String,
    val comicId: Int,
    val author: String,
    val description: String,
    val coverURL: String
) {
    init {
        Log.d("TAG", title + comicId)
    }

}