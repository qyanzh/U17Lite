package com.example.u17lite.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.u17lite.R
import com.example.u17lite.dataBeans.Chapter
import kotlinx.android.synthetic.main.rcv_item_download.view.*

class DownloadAdapter(val list: MutableList<Chapter>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var name = view.tvName
        var status = view.tvStatus
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.rcv_item_download,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val chapter = list[position]
        if (holder is ViewHolder) {
            holder.name.text = "${chapter.comicName} ${chapter.name}"
            holder.status.text = if (position == 0) "正在下载" else "正在等待"
        }
    }

    override fun getItemCount() = list.size

}

