package com.example.u17lite

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_chapter.*
import kotlinx.android.synthetic.main.content_comic.*
import kotlinx.android.synthetic.main.rcv_item_comic.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException

class ChapterActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chapter)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "漫画详情"
        }
        getChapterList()
        Glide.with(this).load(intent.getStringExtra("coverURL")).into(imgCover)
        tvTitle.text = intent.getStringExtra("title")
        tvAuthor.text = intent.getStringExtra("author")
        tvDescription.text = intent.getStringExtra("description")
    }

    private fun getChapterList() {
        val address =
            "http://app.u17.com/v3/appV3_3/android/phone/comic/detail_static_new?" +
                    "come_from=xiaomi" +
                    "&comicid=" + intent.getIntExtra("comicId", 0) +
                    "&serialNumber=7de42d2e" +
                    "&v=4500102" +
                    "&model=MI+6" +
                    "&android_id=f5c9b6c9284551ad"
        val chapterList = mutableListOf<Comic.Chapter>()
        sendOkHttpRequest(address, object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("TAG", "Failed - 获取漫画章节")
            }

            override fun onResponse(call: Call, response: Response) {
                chapterList.addAll(handleChapterListResponse(response.body()!!.string()))
                val adapter = ChapterAdapter(chapterList)
                this@ChapterActivity.runOnUiThread {
                    rcvChapterList.let {
                        it.adapter = adapter
                        it.setHasFixedSize(true)
                        it.layoutManager = LinearLayoutManager(this@ChapterActivity)
                    }
                }
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
            }
            R.id.download -> {
                TODO("下载章节")
            }
            R.id.star -> {
                TODO("订阅本漫画")
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_chapter, menu)
        return true
    }
}
