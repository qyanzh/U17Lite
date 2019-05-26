package com.example.u17lite.adapters

import android.app.Activity
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.NO_ID
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.u17lite.R
import com.example.u17lite.activities.ChapterActivity
import com.example.u17lite.activities.DownloadActivity
import com.example.u17lite.activities.MainActivity
import com.example.u17lite.activities.SearchResultActivity
import com.example.u17lite.dataBeans.Comic
import kotlinx.android.synthetic.main.rcv_item_comic.view.*
import kotlinx.android.synthetic.main.rcv_item_load_more.view.*

class ComicAdapter(var comicList: MutableList<Comic>, var activity: Activity? = null) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    init {
        setHasStableIds(true)
    }

    var tracker: SelectionTracker<Long>? = null

    override fun getItemId(position: Int): Long =
        if (position < comicList.size) comicList[position].comicId else NO_ID.toLong()

    inner class ComicViewHolder(var view: View) : RecyclerView.ViewHolder(view) {
        var imgCover = view.imgCover
        var textTitle = view.tvTitle
        var textAuthor = view.tvAuthor
        var textDescription = view.tvDescription
        fun getItemDetails(): ItemDetailsLookup.ItemDetails<Long> =
            object : ItemDetailsLookup.ItemDetails<Long>() {
                override fun getSelectionKey(): Long? = comicList[adapterPosition].comicId
                override fun getPosition(): Int = adapterPosition
            }

        fun bind(isActive: Boolean) {
            view.isActivated = isActive
        }
    }

    class LoadMoreViewHolder(var view: View) : RecyclerView.ViewHolder(view) {
        var tvLoading = view.tvLoading
        var progressBar = view.progressBar
    }

    var hasMore = true
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == 1) {
            return LoadMoreViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.rcv_item_load_more, parent, false)
            )
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.rcv_item_comic, parent, false)
            return ComicViewHolder(view).apply {
                view.setOnClickListener {
                    val comic = comicList[adapterPosition]
                    val intent = Intent(view.context, ChapterActivity::class.java).also {
                        it.putExtra("comic", comic)
                        it.putExtra("type", if (activity is DownloadActivity) "download" else null)
                    }
                    var options: ActivityOptionsCompat? = null
                    if (activity !is DownloadActivity) {
                        activity?.run {
                            options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                                activity!!,
                                view,
                                ViewCompat.getTransitionName(view)!!
                            )
                        }
                    }
                    if (options != null) {
                        view.context.startActivity(intent, options!!.toBundle())
                    } else {
                        view.context.startActivity(intent)
                    }
                }
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is LoadMoreViewHolder) {
            if (activity is MainActivity || activity is SearchResultActivity) {
                if (itemCount == 1) holder.tvLoading.visibility = View.GONE
                if (!hasMore) {
                    Log.d("TAG", "what")
                    holder.tvLoading.text = "到底了"
                    holder.progressBar.visibility = View.GONE
                }
            } else {
                holder.tvLoading.visibility = View.GONE
                holder.progressBar.visibility = View.GONE
            }
        } else if (holder is ComicViewHolder) {
            val comic = comicList[position]
            tracker?.let {
                holder.bind(it.isSelected(comic.comicId))
            }
            Glide.with(holder.view).load(comic.coverURL).into(holder.imgCover)
            holder.apply {
                textTitle.text = comic.title
                textAuthor.text = comic.author
                textDescription.text = comic.description
            }
        }

    }

    override fun getItemCount() = comicList.size + 1

    override fun getItemViewType(position: Int) = when (position) {
        itemCount - 1 -> 1
        else -> 0
    }


}