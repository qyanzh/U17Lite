package com.example.u17lite.activities

import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.u17lite.R
import com.example.u17lite.adapters.ImageAdapter
import com.example.u17lite.dataBeans.Chapter
import com.example.u17lite.handleImageListResponse
import com.example.u17lite.sendOkHttpRequest
import kotlinx.android.synthetic.main.activity_reader.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.File
import java.io.IOException

class ReaderActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_reader)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = intent.getStringExtra("chapterName")
        mVisible = true

        val chapter = intent.getParcelableExtra<Chapter>("chapter")
        if (intent.getBooleanExtra("download", false)) {
            getImageFromDownload(chapter.belongTo, chapter.chapterId)
        } else {
            getImageFromServer(chapter.chapterId)
        }
    }


    private fun getImageFromDownload(comicId: Long, chapterId: Long) {
        val imageList = mutableListOf<String>()
        File(
            getExternalFilesDir("imgs").absolutePath + File.separator + comicId
                    + File.separator + chapterId
        ).listFiles().let {
            it.sort()
            it.forEach { img ->
                imageList.add(img.absolutePath)
            }
        }
        val adapter =
            ImageAdapter(imageList, this)
        adapter.onTouchListener =
            object : ImageAdapter.OnTouchListener {
                override fun onTouch() {
                    toggle()
                }
            }
        runOnUiThread {
            zoomRecyclerView.let {
                it.adapter = adapter
                it.layoutManager = LinearLayoutManager(this)
                it.isEnableScale = true
            }
        }
    }

    private fun getImageFromServer(chapterId: Long) {
        val address =
            "http://app.u17.com/v3/appV3_3/android/phone/comic/chapterNew?" +
                    "come_from=xiaomi" +
                    "&serialNumber=7de42d2e" +
                    "&v=4500102&model=MI+6" +
                    "&chapter_id=$chapterId" +
                    "&android_id=f5c9b6c9284551ad"
        sendOkHttpRequest(address, object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("TAG", "Failed - 获取漫画图片")
            }

            override fun onResponse(call: Call, response: Response) {
                val imageList = mutableListOf<String>()
                imageList.addAll(handleImageListResponse(response.body()!!.string()))
                val adapter =
                    ImageAdapter(imageList, this@ReaderActivity)
                adapter.onTouchListener =
                    object : ImageAdapter.OnTouchListener {
                        override fun onTouch() {
                            toggle()
                        }
                    }
                this@ReaderActivity.runOnUiThread {
                    zoomRecyclerView.let {
                        it.adapter = adapter
                        it.layoutManager = LinearLayoutManager(this@ReaderActivity)
                        it.isEnableScale = true
                    }
                }
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item!!.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            R.id.mode_switch -> {
                val layoutManager = zoomRecyclerView.layoutManager as LinearLayoutManager
                val o = layoutManager.orientation;
                if (o == RecyclerView.VERTICAL) {
                    layoutManager.orientation = RecyclerView.HORIZONTAL
                    Toast.makeText(this, "切换至横向滚动模式", Toast.LENGTH_SHORT).show()
                } else {
                    layoutManager.orientation = RecyclerView.VERTICAL
                    Toast.makeText(this, "切换至纵向滚动模式", Toast.LENGTH_SHORT).show()

                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private val mHideHandler = Handler()
    private val mHidePart2Runnable = Runnable {
        zoomRecyclerView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LOW_PROFILE or
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
    }
    private val mShowPart2Runnable = Runnable {
        // Delayed display of UI elements
        supportActionBar?.show()
    }
    private var mVisible: Boolean = false
    private val mHideRunnable = Runnable { hide() }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100)
    }

    fun toggle() {
        if (mVisible) {
            hide()
        } else {
            show()
        }
    }

    private fun hide() {
        // Hide UI first
        supportActionBar?.hide()
//        fullscreen_content_controls.visibility = View.GONE
        mVisible = false

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable)
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY.toLong())
    }

    private fun show() {
        // Show the system bar
        zoomRecyclerView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        mVisible = true

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable)
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY.toLong())
    }

    private fun delayedHide(delayMillis: Int) {
        mHideHandler.removeCallbacks(mHideRunnable)
        mHideHandler.postDelayed(mHideRunnable, delayMillis.toLong())
    }

    companion object {
        private val UI_ANIMATION_DELAY = 300
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.activity_reader, menu)
        return true
    }
}
