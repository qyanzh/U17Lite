package com.example.u17lite.adapters

import android.app.Activity
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.NO_ID
import com.bumptech.glide.Glide
import com.example.u17lite.R
import com.example.u17lite.activities.ChapterActivity
import com.example.u17lite.activities.ReaderActivity
import com.example.u17lite.dataBeans.Chapter
import com.example.u17lite.dataBeans.getDatabase
import com.example.u17lite.isWebConnect
import kotlinx.android.synthetic.main.rcv_item_chapter.view.*
import kotlinx.android.synthetic.main.rcv_item_chapter.view.imgCover
import kotlinx.android.synthetic.main.rcv_item_comic.view.*
import java.text.SimpleDateFormat

class ChapterAdapter(
    private val comicId: Long,
    private val chapterList: MutableList<Chapter>,
    private val activity: Activity
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    init {
        setHasStableIds(true)
    }

    var tracker: SelectionTracker<Long>? = null

    override fun getItemId(position: Int): Long =
        if (position in 1..chapterList.size) chapterList[position - 1].chapterId else NO_ID

    inner class ChapterViewHolder(var view: View) : RecyclerView.ViewHolder(view) {
        var chapterName = view.tvName
        var coverImg = view.imgCover
        var publishTime = view.tvPublishTime
        fun getItemDetails(): ItemDetailsLookup.ItemDetails<Long> =
            object : ItemDetailsLookup.ItemDetails<Long>() {
                override fun getSelectionKey(): Long? = chapterList[adapterPosition - 1].chapterId
                override fun getPosition(): Int = adapterPosition
            }

        fun bind(isActive: Boolean) {
            view.isActivated = isActive
        }
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
            return ChapterComicViewHolder(view)
        }
    }

    private class ChapterComicViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        var imgCover = view.imgCover
        var textTitle = view.tvTitle
        var textAuthor = view.tvAuthor
        var textDescription = view.tvDescription
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ChapterViewHolder) {
            val chapter = chapterList[position - 1]
            holder.chapterName.text = chapter.name
            Glide.with(holder.view).load(chapter.smallCoverURL).into(holder.coverImg)
            holder.publishTime.text =
                SimpleDateFormat("yyyy-MM-dd").format(chapter.publishTime * 1000)
            tracker?.let {
                holder.bind(it.isSelected(chapter.chapterId))
            }
        } else if (holder is ChapterComicViewHolder) {
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
