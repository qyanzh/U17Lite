package com.example.u17lite.activities

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.u17lite.R
import com.example.u17lite.adapters.DownloadAdapter
import com.example.u17lite.dataBeans.*
import com.example.u17lite.services.DownloadService
import kotlinx.android.synthetic.main.activity_download_que.*

class DownloadQueActivity : AppCompatActivity() {

    lateinit var downloadDao: DownloadDao
    lateinit var chapterDao: ChapterDao
    lateinit var downloadList: List<DownloadItem>
    val chapterList = mutableListOf<Chapter>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_download_que)
        supportActionBar?.title = "下载队列"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        downloadDao = getDatabase(this).downloadDao()
        chapterDao = getDatabase(this).chapterDao()
        registerReceiver()
        Thread {
            downloadList = downloadDao.getAll()
            downloadList.forEach {
                chapterDao.find(it.chapterId)?.let { chapter -> chapterList.add(chapter) }
            }
            val adapter = DownloadAdapter(chapterList)
            runOnUiThread {
                rcvDownloadQueue.let {
                    it.adapter = adapter
                    it.layoutManager = LinearLayoutManager(this)
                    it.emptyView = emptyView
                }
            }
        }.start()
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Thread {
                downloadList = downloadDao.getAll()
                chapterList.clear()
                downloadList.forEach {
                    chapterDao.find(it.chapterId)?.let { chapter -> chapterList.add(chapter) }
                }
                runOnUiThread {
                    rcvDownloadQueue.adapter?.notifyDataSetChanged()
                }
            }.start()
        }
    }

    private fun registerReceiver() {
        val filter = IntentFilter()
        filter.addAction(DownloadService.RECIEVE)
        registerReceiver(receiver, filter)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }
}
