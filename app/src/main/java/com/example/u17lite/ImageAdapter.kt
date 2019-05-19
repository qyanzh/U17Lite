package com.example.u17lite

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.rcv_item_image.view.*

class ImageAdapter(val list: List<String>, activity: Activity) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgComic = view.imgComic
    }

    interface OnTouchListener {
        fun onTouch()
    }

    var onTouchListener: OnTouchListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.rcv_item_image,
                parent,
                false
            ).apply {
                imgComic.setOnClickListener {
                    onTouchListener?.onTouch()
                }
            }
        )
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ViewHolder) {
            Glide.with(holder.itemView.context).load(list[position]).into(holder.imgComic)
        }
    }

}