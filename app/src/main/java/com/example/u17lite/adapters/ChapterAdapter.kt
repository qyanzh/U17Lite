package com.example.u17lite.adapters

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.u17lite.R
import com.example.u17lite.activities.ChapterActivity
import com.example.u17lite.activities.ReaderActivity
import com.example.u17lite.dataBeans.Chapter
import com.example.u17lite.isWebConnect
import kotlinx.android.synthetic.main.rcv_item_chapter.view.*
import java.text.SimpleDateFormat

class ChapterAdapter(
    private val chapterList: MutableList<Chapter>,
    private val activity: Activity
) :
    RecyclerView.Adapter<ChapterAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.rcv_item_chapter, parent, false)
        return ViewHolder(view).apply {
            view.setOnClickListener {
                val chapter = chapterList[adapterPosition]
                if (isWebConnect(activity)) {
                    view.context.startActivity(
                        Intent(
                            view.context,
                            ReaderActivity::class.java
                        ).apply {
                            putExtra("download", (activity as ChapterActivity).download)
                            putExtra("chapter", chapter)
                        })
                } else {
                    Toast.makeText(activity, "请检查网络连接", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return chapterList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val chapter = chapterList[position]
        holder.chapterName.text = chapter.name
        Glide.with(holder.view).load(chapter.smallCoverURL).into(holder.coverImg)
        holder.publishTime.text = SimpleDateFormat("yyyy-MM-dd").format(chapter.publishTime * 1000)
    }

    class ViewHolder(var view: View) : RecyclerView.ViewHolder(view) {
        var chapterName = view.tvName
        var coverImg = view.imgCover
        var publishTime = view.tvPublishTime
    }
}
