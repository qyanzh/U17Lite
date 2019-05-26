package com.example.u17lite.adapters

import androidx.recyclerview.selection.ItemKeyProvider
import com.example.u17lite.dataBeans.Chapter

class ChapterItemKeyProvider(private val chapterList: MutableList<Chapter>) :
    ItemKeyProvider<Long>(SCOPE_CACHED) {
    override fun getKey(position: Int): Long? {
        return chapterList[position - 1].chapterId
    }

    override fun getPosition(key: Long): Int {
        for (i in 0 until chapterList.size) {
            if (chapterList[i].chapterId == key) {
                return i + 1
            }
        }
        return -1
    }
}
