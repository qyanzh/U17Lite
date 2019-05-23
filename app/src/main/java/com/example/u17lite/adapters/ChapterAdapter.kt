package com.example.u17lite.adapters

import android.app.Activity
import android.content.Intent
import android.util.Log
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
import com.example.u17lite.dataBeans.getDatabase
import com.example.u17lite.isWebConnect
import kotlinx.android.synthetic.main.rcv_item_chapter.view.*
import java.text.SimpleDateFormat

class ChapterAdapter(
    private val comicId: Long,
    private val chapterList: MutableList<Chapter>,
    private val activity: Activity
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    init {
        Log.d("TAG", "init")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        Log.d("TAG", "oncreate")
        if (viewType == 1) {
            Log.d("TAG", "1")
            val view =
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.rcv_item_chapter, parent, false)
            return ChapterViewHolder(view).apply {
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
        } else {
            Log.d("TAG", "2")
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.rcv_item_comic, parent, false)
            return ComicAdapter.ComicViewHolder(view)
        }
    }

    override fun getItemCount(): Int {
        return chapterList.size
    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> 0
            else -> 1
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ChapterViewHolder) {
            val chapter = chapterList[position]
            holder.chapterName.text = chapter.name
            Glide.with(holder.view).load(chapter.smallCoverURL).into(holder.coverImg)
            holder.publishTime.text =
                SimpleDateFormat("yyyy-MM-dd").format(chapter.publishTime * 1000)
        } else if (holder is ComicAdapter.ComicViewHolder) {
            Thread {
                val comic = getDatabase(activity).comicDao().find(comicId)
                activity.runOnUiThread {
                    comic?.apply {
                        Glide.with(holder.view).load(comic.coverURL).into(holder.imgCover)
                        holder.apply {
                            textTitle.text = comic.title
                            textAuthor.text = comic.author
                            textDescription.text = comic.description
                        }
                    }
                }

            }.start()
        }
    }

    class ChapterViewHolder(var view: View) : RecyclerView.ViewHolder(view) {
        var chapterName = view.tvName
        var coverImg = view.imgCover
        var publishTime = view.tvPublishTime
    }
}
