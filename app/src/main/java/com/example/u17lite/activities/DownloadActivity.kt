package com.example.u17lite.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.u17lite.R
import com.example.u17lite.adapters.ComicAdapter
import com.example.u17lite.dataBeans.Comic
import com.example.u17lite.dataBeans.getDatabase
import kotlinx.android.synthetic.main.activity_download.*
import kotlinx.android.synthetic.main.activity_subscribe.rcvComicList
import kotlinx.android.synthetic.main.content_empty.view.*

class DownloadActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_download)
        val comicDao = getDatabase(this).comicDao()
        val list = mutableListOf<Comic>()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "已缓存"
        Thread {
            getExternalFilesDir("imgs").list().forEach { comicId ->
                comicDao.find(comicId.toLong())?.let {
                    list.add(it)
                }
            }
            runOnUiThread {
                rcvComicList.let {
                    it.adapter = ComicAdapter(list, this@DownloadActivity)
                    it.layoutManager = LinearLayoutManager(this@DownloadActivity)
                    it.emptyView = emptyView.also {
                        it.tvLongClick.visibility = View.VISIBLE
                        it.tvEmpty.visibility = View.GONE
                    }
                }
            }
        }.start()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_download, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> onBackPressed()
            R.id.downloading_list -> startActivity(Intent(this, DownloadQueActivity::class.java))
        }
        return super.onOptionsItemSelected(item)
    }
}
