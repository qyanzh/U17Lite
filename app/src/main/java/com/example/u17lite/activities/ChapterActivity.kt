package com.example.u17lite.activities

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.u17lite.R
import com.example.u17lite.adapters.ChapterAdapter
import com.example.u17lite.dataBeans.Chapter
import com.example.u17lite.dataBeans.Comic
import com.example.u17lite.dataBeans.ComicDao
import com.example.u17lite.dataBeans.getDatabase
import com.example.u17lite.handleChapterListResponse
import com.example.u17lite.handleSubscribeResponse
import com.example.u17lite.sendOkHttpRequest
import kotlinx.android.synthetic.main.activity_chapter.*
import kotlinx.android.synthetic.main.content_comic.*
import kotlinx.android.synthetic.main.rcv_item_comic.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException

class ChapterActivity : AppCompatActivity() {

    var download: Boolean? = null
    lateinit var comic: Comic
    lateinit var comicDao: ComicDao
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chapter)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "漫画详情"
        }
        comicDao = getDatabase(this).comicDao()

        comic = intent.getParcelableExtra("comic")
        if (intent.getStringExtra("type") == "download") {
            download = true
            getChapterListFromFile(comic.comicId)
        } else {
            getChapterListFromServer(comic.comicId)
        }
        Glide.with(this).load(comic.coverURL).into(imgCover)
        tvTitle.text = comic.title
        tvAuthor.text = comic.author
        tvDescription.text = comic.description
    }

    private fun getChapterListFromFile(comicId: Long) {

    }

    private fun getChapterListFromServer(comicId: Long) {
        val address =
            "http://app.u17.com/v3/appV3_3/android/phone/comic/detail_static_new?" +
                    "come_from=xiaomi" +
                    "&comicid=" + comicId +
                    "&serialNumber=7de42d2e" +
                    "&v=4500102" +
                    "&model=MI+6" +
                    "&android_id=f5c9b6c9284551ad"
        val chapterList = mutableListOf<Chapter>()
        sendOkHttpRequest(address, object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("TAG", "Failed - 获取漫画章节")
            }

            override fun onResponse(call: Call, response: Response) {
                val res = response.body()!!.string()
                comic.lastUpdateTime = handleSubscribeResponse(res)
                chapterList.addAll(handleChapterListResponse(res))
                chapterList.forEach { it.belongTo = comicId }
                val adapter =
                    ChapterAdapter(chapterList, this@ChapterActivity)
                this@ChapterActivity.runOnUiThread {
                    rcvChapterList.let {
                        it.adapter = adapter
                        it.isNestedScrollingEnabled = false
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
                Thread {
                    comicDao.find(comic.comicId)?.let {
                        if (it.isSubscribed) {
                            runOnUiThread {
                                Toast.makeText(this, "取消订阅", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            runOnUiThread {
                                Toast.makeText(this, "订阅成功", Toast.LENGTH_SHORT).show()
                            }
                        }
                        comicDao.update(it.apply { isSubscribed = !isSubscribed })
                    } ?: let {
                        comicDao.insert(comic.apply {
                            isSubscribed = true
                        })
                        runOnUiThread {
                            Toast.makeText(this, "订阅成功", Toast.LENGTH_SHORT).show()
                        }
                    }
                    invalidateOptionsMenu()
                }.start()
            }
        }
        return super.onOptionsItemSelected(item)
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_chapter, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        Log.d("TAG", "onPrepareOptionsMenu")
        Thread {
            comicDao.find(comic.comicId)?.let {
                runOnUiThread {
                    menu?.getItem(0)?.setIcon(
                        if (it.isSubscribed) R.drawable.ic_star_black_24dp else R.drawable.ic_star_border_black_24dp
                    )
                }
            }
        }.start()
        return super.onPrepareOptionsMenu(menu)
    }
}
