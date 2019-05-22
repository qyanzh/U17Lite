package com.example.u17lite.activities

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.GravityCompat
import com.example.u17lite.R
import com.example.u17lite.dataBeans.ComicDao
import com.example.u17lite.dataBeans.getDatabase
import com.example.u17lite.fragments.ComicListFragment
import com.example.u17lite.handleSubscribeResponse
import com.example.u17lite.sendOkHttpRequest
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    companion object {
        private const val CHECK_UPDATE_TIME = 10 * 60 * 1000L
        private const val prefix =
            "http://app.u17.com/v3/appV3_3/android/phone/list/getRankComicList?" +
                    "period=total&type=2"
        private const val postfix = "&come_from=xiaomi" +
                "&serialNumber=7de42d2e" +
                "&v=450010" +
                "&model=MI+6" +
                "&android_id=f5c9b6c9284551ad"
        private const val CHANNEL_ID = "Subscribe"

    }

    lateinit var comicDao: ComicDao
    var isForeground = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        createNotificationChannel()
        comicDao = getDatabase(this).comicDao()

        runSubscribeThread()

        ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        ).let {
            drawerLayout.addDrawerListener(it)
            it.syncState()
        }
        navView.setNavigationItemSelectedListener(this)

        val fragment = ComicListFragment.newInstance(prefix, postfix)
        supportFragmentManager.beginTransaction().let {
            if (it.isEmpty) {
                it.add(R.id.fragmentHolder, fragment).commit()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isForeground = false
    }

    private fun runSubscribeThread() {
        isForeground = true
        Thread {
            while (isForeground) {
                comicDao.getSubscribedList().forEach {
                    val url =
                        "http://app.u17.com/v3/appV3_3/android/phone/comic/detail_static_new?" +
                                "come_from=xiaomi" +
                                "&comicid=" + it.comicId +
                                "&serialNumber=7de42d2e" +
                                "&v=4500102" +
                                "&model=MI+6" +
                                "&android_id=f5c9b6c9284551ad"

                    sendOkHttpRequest(url, object : Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            Log.d("TAG", "Failed - checkSubscribeUpdate")
                        }

                        override fun onResponse(call: Call, response: Response) {
                            val newLastUpdateTime =
                                handleSubscribeResponse(response.body()!!.string())
                            Log.d("TAG", "${it.lastUpdateTime} $newLastUpdateTime")
                            if (newLastUpdateTime != it.lastUpdateTime) {
                                it.lastUpdateTime = newLastUpdateTime
                                comicDao.update(it)
                                val builder =
                                    NotificationCompat.Builder(this@MainActivity, CHANNEL_ID)
                                        .setSmallIcon(R.drawable.ic_star_black_24dp)
                                        .setContentText("订阅的漫画更新啦")
                                        .setContentTitle(it.title)
                                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                with(NotificationManagerCompat.from(this@MainActivity)) {
                                    // notificationId is a unique int for each notification that you must define
                                    notify(it.comicId.toInt(), builder.build())
                                }
                            }
                        }
                    })

                }
                Log.d("TAG", "check subscribe")
                Thread.sleep(CHECK_UPDATE_TIME)
            }
            Log.d("TAG", "subscribe checking thread terminated")
        }.start()
    }

    private fun createNotificationChannel() {
        val name = "订阅通知"
        val descriptionText = "漫画更新通知"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        // Register the channel with the system
        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        // Get the SearchView and set the searchable configuration
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        (menu.findItem(R.id.search).actionView as SearchView).apply {
            // Assumes current activity is the searchable activity
            setSearchableInfo(searchManager.getSearchableInfo(componentName))
            setIconifiedByDefault(true) // Do not iconify the widget; expand it by default

        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when (item.itemId) {
            R.id.search -> {
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> {

            }
            R.id.nav_download -> {
            }
            R.id.nav_star -> {
                startActivity(Intent(this, SubscribeActivity::class.java))
            }
            R.id.nav_test -> {
                startActivity(Intent(this, TestActivity::class.java))
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}
