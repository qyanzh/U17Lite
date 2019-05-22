package com.example.u17lite.activities

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.u17lite.R
import com.example.u17lite.adapters.ComicAdapter
import com.example.u17lite.dataBeans.Comic
import com.example.u17lite.dataBeans.getDatabase
import kotlinx.android.synthetic.main.activity_subscribe.*

class SubscribeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_subscribe)
        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.title = "订阅"
        }
        val list = mutableListOf<Comic>()
        Thread {
            list.addAll(getDatabase(this).comicDao().getSubscribedList())
            runOnUiThread {
                rcvComicList.apply {
                    adapter = ComicAdapter(list, this@SubscribeActivity)
                    emptyView = emptyview
                    setHasFixedSize(true)
                    layoutManager = LinearLayoutManager(this@SubscribeActivity)
                }
            }
        }.start()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
