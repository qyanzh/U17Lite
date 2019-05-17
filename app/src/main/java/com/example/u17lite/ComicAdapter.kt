package com.example.u17lite

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.rcv_item_comic.view.*

class ComicAdapter(comicList: MutableList<Comic>) :
    RecyclerView.Adapter<ComicAdapter.ViewHolder>() {
    var list = comicList
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.rcv_item_comic, parent, false)
        return ViewHolder(view).apply {
            comicView.setOnClickListener {
                val comic = list[adapterPosition]
                TODO("跳转到漫画详情页")
            }
        }
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val comic = list[position]
        Glide.with(holder.comicView).load(comic.coverURL).into(holder.imgCover)
        holder.apply {
            textTitle.text = comic.title
            textAuthor.text = comic.author
            textDescription.text = comic.description
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var comicView: View = view
        var imgCover = view.imgCover
        var textTitle = view.tvTitle
        var textAuthor = view.tvAuthor
        var textDescription = view.tvDescription
    }


}