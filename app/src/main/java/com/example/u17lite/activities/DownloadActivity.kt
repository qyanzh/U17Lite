package com.example.u17lite.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.u17lite.R
import com.example.u17lite.adapters.ComicAdapter
import com.example.u17lite.dataBeans.Comic
import com.example.u17lite.dataBeans.getDatabase
import kotlinx.android.synthetic.main.activity_subscribe.*

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
                rcvComicList.apply {
                    adapter = ComicAdapter(list, this@DownloadActivity)
                    layoutManager = LinearLayoutManager(this@DownloadActivity)
                    emptyView = emptyView
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
