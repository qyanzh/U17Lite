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

    class ChapterViewHolder(var view: View) : RecyclerView.ViewHolder(view) {
        var chapterName = view.tvName
        var coverImg = view.imgCover
        var publishTime = view.tvPublishTime
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        Log.d("TAG", "oncreate")
        if (viewType == 1) {
            val view =
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.rcv_item_chapter, parent, false)
            return ChapterViewHolder(view).apply {
                view.setOnClickListener {
                    val chapter = chapterList[adapterPosition - 1]
                    if (isWebConnect(activity) || (activity as ChapterActivity).download!!) {
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
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.rcv_item_comic, parent, false)
            return ComicAdapter.ComicViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        Log.d("TAG", "" + chapterList.size)
        if (holder is ChapterViewHolder) {
            val chapter = chapterList[position - 1]
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

    override fun getItemCount() = chapterList.size + 1

    override fun getItemViewType(position: Int) = when (position) {
        0 -> 0
        else -> 1
    }
}
