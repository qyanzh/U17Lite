package com.example.u17lite

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.rcv_item_chapter.view.*

class ChapterAdapter(val chapterList: MutableList<Comic.Chapter>) :
    RecyclerView.Adapter<ChapterAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.rcv_item_chapter, parent, false)
        return ViewHolder(view).apply {
            view.setOnClickListener {
                val chapter = chapterList[adapterPosition]
                TODO("打开漫画")
            }
        }
    }

    override fun getItemCount(): Int {
        return chapterList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val chapter = chapterList[position]
        holder.chapterName.text = chapter.name
    }

    class ViewHolder(var view: View) : RecyclerView.ViewHolder(view) {
        var chapterName = view.tvName
    }
}
