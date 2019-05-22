package com.example.u17lite.customViews

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.RecyclerView
import com.example.u17lite.R

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
            Log.d("TAG", "" + emptyView?.visibility)
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

    fun runAnimation() {
        val controller =
            AnimationUtils.loadLayoutAnimation(
                context,
                R.anim.layout_animation_slide_in_bottom
            )
        layoutAnimation = controller
        scheduleLayoutAnimation()
    }

}