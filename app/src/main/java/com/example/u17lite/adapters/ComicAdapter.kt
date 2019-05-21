package com.example.u17lite.adapters

import android.app.Activity
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.u17lite.R
import com.example.u17lite.activities.ChapterActivity
import com.example.u17lite.dataBeans.Comic
import kotlinx.android.synthetic.main.rcv_item_comic.view.*
import kotlinx.android.synthetic.main.rcv_item_load_more.view.*

class ComicAdapter(var comicList: MutableList<Comic>, var activity: Activity? = null) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var hasMore = true

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is LoadMoreViewHolder) {
            if (itemCount == 1) holder.tvLoading.visibility = View.GONE
            if (!hasMore) {
                Log.d("TAG", "what")
                holder.tvLoading.text = "到底了"
                holder.progressBar.visibility = View.GONE
            }
        } else if (holder is ComicViewHolder) {
            val comic = comicList[position]
            Glide.with(holder.view).load(comic.coverURL).into(holder.imgCover)
            holder.apply {
                textTitle.text = comic.title
                textAuthor.text = comic.author
                textDescription.text = comic.description
            }
        }

    }

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
                    }
                    var options: ActivityOptionsCompat? = null
                    activity?.run {
                        options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                            activity!!,
                            view,
                            ViewCompat.getTransitionName(view)!!
                        )
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

    override fun getItemCount() = comicList.size + 1


    override fun getItemViewType(position: Int): Int {
        return when (position) {
            itemCount - 1 -> 1
            else -> 0
        }
    }

    class ComicViewHolder(var view: View) : RecyclerView.ViewHolder(view) {
        var imgCover = view.imgCover
        var textTitle = view.tvTitle
        var textAuthor = view.tvAuthor
        var textDescription = view.tvDescription
    }

    class LoadMoreViewHolder(var view: View) : RecyclerView.ViewHolder(view) {
        var tvLoading = view.tvLoading
        var progressBar = view.progressBar
    }

}