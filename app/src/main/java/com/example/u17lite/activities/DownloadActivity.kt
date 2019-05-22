package com.example.u17lite.activities

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.u17lite.R
import com.example.u17lite.adapters.ComicAdapter
import com.example.u17lite.dataBeans.Comic
import kotlinx.android.synthetic.main.activity_subscribe.*

class DownloadActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_download)
        val list = mutableListOf<Comic>()
        //TODO:获取文件列表
        Thread {
            list.forEach { Log.d("TAG", it.toString()) }
            runOnUiThread {
                rcvComicList.apply {
                    adapter = ComicAdapter(list, this@DownloadActivity)
                    layoutManager = LinearLayoutManager(this@DownloadActivity)
                    emptyView = emptyView
                }
            }
        }.start()
    }
}
