package com.example.u17lite.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.u17lite.*
import com.example.u17lite.adapters.ChapterAdapter
import com.example.u17lite.adapters.ChapterDetailsLookup
import com.example.u17lite.adapters.ChapterItemKeyProvider
import com.example.u17lite.dataBeans.*
import com.example.u17lite.services.DownloadService
import kotlinx.android.synthetic.main.activity_chapter.*
import okhttp3.*
import java.io.File
import java.io.IOException


class ChapterActivity : AppCompatActivity() {

    var download: Boolean? = null
    lateinit var comic: Comic
    lateinit var comicDao: ComicDao
    lateinit var chapterDao: ChapterDao
    lateinit var downloadDao: DownloadDao
    val chapterList = mutableListOf<Chapter>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chapter)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "漫画详情"
        }
        comicDao = getDatabase(this).comicDao()
        chapterDao = getDatabase(this).chapterDao()
        downloadDao = getDatabase(this).downloadDao()
        comic = intent.getParcelableExtra("comic")
        if (!isWebConnect(this) && intent.getStringExtra("type") != "download") {
            Toast.makeText(this, "请检查网络连接", Toast.LENGTH_SHORT).show()
        }
        Thread {
            if (intent.getStringExtra("type") == "download") {
                download = true
                getChapterListFromFile(comic.comicId)
            } else {
                comicDao.insert(comic)
                getChapterListFromServer(comic.comicId)
            }
        }.start()
    }


    private fun getChapterListFromFile(comicId: Long) {
        File(getExternalFilesDir("imgs").absolutePath + File.separator + comicId).list().sorted()
            .forEach { chapterId ->
                Log.d("TAG", chapterId)
                chapterDao.find(chapterId.toLong())?.let {
                    chapterList.add(it)
                }
            }
        val adapter =
            ChapterAdapter(comicId, chapterList, this@ChapterActivity)
        this@ChapterActivity.runOnUiThread {
            rcvChapterList.let {
                it.adapter = adapter
                it.layoutManager = LinearLayoutManager(this@ChapterActivity)
            }
        }
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
        sendOkHttpRequest(address, object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("TAG", "Failed - 获取漫画章节")
            }

            override fun onResponse(call: Call, response: Response) {
                val res = response.body()!!.string()
                comic.lastUpdateTime = handleSubscribeResponse(res)
                chapterList.addAll(handleChapterListResponse(res))
                chapterList.forEach {
                    it.belongTo = comicId;it.comicName = comic.title;chapterDao.insert(it)
                }
                val adapter =
                    ChapterAdapter(comicId, chapterList, this@ChapterActivity)
                this@ChapterActivity.runOnUiThread {
                    rcvChapterList.let {
                        it.adapter = adapter
                        it.setHasFixedSize(true)
                        it.layoutManager = LinearLayoutManager(this@ChapterActivity)
                        tracker = SelectionTracker.Builder<Long>(
                            "selection${this@ChapterActivity}",
                            it,
                            ChapterItemKeyProvider(chapterList),
                            ChapterDetailsLookup(it),
                            StorageStrategy.createLongStorage()
                        ).withSelectionPredicate(
                            SelectionPredicates.createSelectAnything()
                        ).build()
                        adapter.tracker = tracker
                        tracker?.let { tracker ->
                            tracker.addObserver(object :
                                SelectionTracker.SelectionObserver<Long>() {
                                override fun onSelectionChanged() {
                                    if (tracker.hasSelection() && mActionMode == null) {
                                        mActionMode = startSupportActionMode(actionModeCallback)
                                    } else if (!tracker.hasSelection() && mActionMode != null) {
                                        mActionMode!!.finish()
                                        mActionMode = null
                                    } else {
                                        mActionMode?.title =
                                            "已选择${tracker?.selection?.size()}项"
                                    }
                                }
                            })
                        }
                    }
                }
            }
        })
    }

    private var tracker: SelectionTracker<Long>? = null
    var mActionMode: ActionMode? = null
    var actionModeCallback = object : ActionMode.Callback {
        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
            return when (item?.itemId) {
                R.id.download -> {
                    Toast.makeText(this@ChapterActivity, "正在获取下载链接", Toast.LENGTH_SHORT).show()
                    val selections = (tracker?.selection?.sorted())?.map { it }
                    mode?.finish()
                    Thread {
                        val client = OkHttpClient()
                        selections?.forEach { chapterId ->
                            val addressForURLs =
                                "http://app.u17.com/v3/appV3_3/android/phone/comic/chapterNew?" +
                                        "come_from=xiaomi" +
                                        "&serialNumber=7de42d2e" +
                                        "&v=4500102&model=MI+6" +
                                        "&chapter_id=$chapterId" +
                                        "&android_id=f5c9b6c9284551ad"
                            val request = Request.Builder().url(addressForURLs).build()
                            val urlResponse =
                                client.newCall(request).execute().body()!!.string()
                            val downloadURL = handleDownloadUrlResponse(urlResponse)
                            getDatabase(this@ChapterActivity).downloadDao().insert(
                                DownloadItem(comic.comicId, chapterId, downloadURL)
                            )
                            runOnUiThread {
                                Toast.makeText(
                                    this@ChapterActivity,
                                    "已添加${selections?.size}个章节到下载队列",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                        startService(
                            Intent(
                                this@ChapterActivity,
                                DownloadService::class.java
                            ).putExtra("multiTask", 1)
                        )
                    }.start()
                    true
                }
                else -> false
            }
        }

        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            Log.d(
                "ChapterActivity", "onCreateActionMode: " +
                        ""
            )
            mode?.menuInflater?.inflate(R.menu.menu_download, menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            Log.d(
                "ChapterActivity", "onPrepareActionMode: " +
                        ""
            )
            return false
        }

        override fun onDestroyActionMode(mode: ActionMode?) {
            Log.d(
                "ChapterActivity", "onDestroyActionMode: " +
                        ""
            )
            mActionMode = null
            tracker?.clearSelection()
        }

    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
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
