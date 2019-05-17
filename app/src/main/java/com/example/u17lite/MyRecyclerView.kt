package com.example.u17lite

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class MyRecyclerView(context: Context) : RecyclerView(context) {
    private var emptyView: View? = null
        set(emptyView) {
            checkIfEmpty()
            field = emptyView
        }

    fun checkIfEmpty() {
        adapter?.let {
            emptyView?.visibility = if (it.itemCount > 0) View.GONE else View.VISIBLE
        }
    }

    var o: OnItemChangedObserver? = null

    interface OnItemChangedObserver {
        fun onItemChanged() {}
    }

    val observer = object : AdapterDataObserver() {
        override fun onChanged() {
            super.onChanged()
            o?.onItemChanged()
            checkIfEmpty()
            invalidateItemDecorations()
        }

        override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
            super.onItemRangeChanged(positionStart, itemCount)
            o?.onItemChanged()
            checkIfEmpty()
            invalidateItemDecorations()
        }

        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
            super.onItemRangeRemoved(positionStart, itemCount)
            o?.onItemChanged()
            checkIfEmpty()
            invalidateItemDecorations()
        }
    }

    override fun setAdapter(adapter: Adapter<*>?) {
        val oldAdapter = getAdapter()
        oldAdapter?.unregisterAdapterDataObserver(observer)
        super.setAdapter(adapter)
        adapter?.registerAdapterDataObserver(observer)
    }

}