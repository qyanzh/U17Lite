package com.example.u17lite.adapters

import androidx.recyclerview.selection.ItemKeyProvider
import com.example.u17lite.dataBeans.Comic

class ComicItemKeyProvider(private val comicList: MutableList<Comic>) :
    ItemKeyProvider<Long>(SCOPE_CACHED) {

    override fun getKey(position: Int): Long? {
        return comicList[position].comicId
    }

    override fun getPosition(key: Long): Int {
        for (i in 0 until comicList.size) {
            if (comicList[i].comicId == key)
                return i
        }
        return -1
    }
}