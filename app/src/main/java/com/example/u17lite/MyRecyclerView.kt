package com.example.u17lite

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class MyRecyclerView(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) :
    RecyclerView(context, attrs, defStyle) {

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context) : this(context, defStyle = 0)

    var emptyView: View? = null
        set(emptyView) {
            field = emptyView
            checkIfEmpty()
        }

    fun checkIfEmpty() {
        adapter?.let {
            emptyView?.visibility = if (it.itemCount > 1) View.GONE else View.VISIBLE
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