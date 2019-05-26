package com.example.u17lite.adapters

import android.view.MotionEvent
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.widget.RecyclerView

class ComicDetailsLookup(private val rv: RecyclerView) : ItemDetailsLookup<Long>() {
    override fun getItemDetails(e: MotionEvent): ItemDetails<Long>? {
        val view = rv.findChildViewUnder(e.x, e.y)
        return view?.let {
            val viewHolder = rv.getChildViewHolder(it)
            if (viewHolder is ComicAdapter.ComicViewHolder) {
                viewHolder.getItemDetails()
            } else {
                null
            }
        }
    }
}