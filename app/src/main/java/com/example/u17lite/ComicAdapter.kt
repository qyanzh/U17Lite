package com.example.u17lite

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.rcv_item_comic.view.*

class ComicAdapter(var comicList: MutableList<Comic>) :
    RecyclerView.Adapter<ComicAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.rcv_item_comic, parent, false)
        return ViewHolder(view).apply {
            view.setOnClickListener {
                val comic = comicList[adapterPosition]
                val intent = Intent(view.context, ChapterActivity::class.java).also {
                    it.putExtra("title", comic.title)
                    it.putExtra("comicId", comic.comicId)
                    it.putExtra("coverURL", comic.coverURL)
                    it.putExtra("author", comic.author)
                    it.putExtra("description", comic.description)
                }
                view.context.startActivity(intent)
            }
        }
    }

    override fun getItemCount() = comicList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val comic = comicList[position]
        Glide.with(holder.view).load(comic.coverURL).into(holder.imgCover)
        holder.apply {
            textTitle.text = comic.title
            textAuthor.text = comic.author
            textDescription.text = comic.description
        }
    }

    class ViewHolder(var view: View) : RecyclerView.ViewHolder(view) {
        var imgCover = view.imgCover
        var textTitle = view.tvTitle
        var textAuthor = view.tvAuthor
        var textDescription = view.tvDescription
    }


}