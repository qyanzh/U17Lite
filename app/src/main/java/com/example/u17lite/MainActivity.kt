package com.example.u17lite

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*
import okhttp3.Call
import okhttp3.Response
import java.io.IOException

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {


    val comicList = mutableListOf<Comic>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        getComicList()
        rcvComicRank.runAnimation()
        navView.setNavigationItemSelectedListener(this)
        rcvComicRank.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if ((recyclerView.layoutManager as LinearLayoutManager).findLastCompletelyVisibleItemPosition() + 1 == recyclerView.adapter?.itemCount) {
                        Log.d("TAG", "到底了")
                        getComicList()
                    }
                }
            }
        })
        if (!isWebConnect(this)) {
            Toast.makeText(this, "请检查网络连接", Toast.LENGTH_SHORT).show()
        }
        swipeRefreshLayout.setOnRefreshListener {
            if (isWebConnect(this)) {
                hasMore = true
                currentPage = 0
                comicList.clear()
                rcvComicRank.adapter?.notifyDataSetChanged()
                getComicList()
                rcvComicRank.runAnimation()
            } else {
                Toast.makeText(this, "请检查网络连接", Toast.LENGTH_SHORT).show()
                swipeRefreshLayout.isRefreshing = false
            }
        }
    }

    var hasMore: Boolean = true
    var currentPage = 0
    private fun getComicList(page: Int = currentPage + 1) {
        if (hasMore) {
            val address =
                "http://app.u17.com/v3/appV3_3/android/phone/list/getRankComicList?" +
                        "period=total&type=2" +
                        "&page=$page" +
                        "&come_from=xiaomi" +
                        "&serialNumber=7de42d2e" +
                        "&v=450010" +
                        "&model=MI+6" +
                        "&android_id=f5c9b6c9284551ad"
            sendOkHttpRequest(address, object : okhttp3.Callback {
                override fun onResponse(call: Call, response: Response) {
                    val responseString = handleListResponse(response.body()!!.string())
                    comicList.addAll(responseString.list)
                    currentPage = responseString.currentPage
                    hasMore = responseString.hasMore
                    if (responseString.currentPage == 1) {
                        val adapter = ComicAdapter(comicList, this@MainActivity)
                        adapter.hasMore = hasMore
                        this@MainActivity.runOnUiThread {
                            rcvComicRank.let {
                                it.adapter = adapter
                                it.emptyView = emptyView
                                it.setHasFixedSize(true)
                                it.layoutManager = LinearLayoutManager(this@MainActivity)
                            }
                        }
                    } else {
                        this@MainActivity.runOnUiThread {
                            rcvComicRank.let {
                                (it.adapter as ComicAdapter).hasMore = hasMore
                                it.adapter?.notifyDataSetChanged()
                            }
                        }
                    }
                    if (swipeRefreshLayout.isRefreshing) {
                        swipeRefreshLayout.isRefreshing = false
                        Snackbar.make(rcvComicRank, "刷新成功", Snackbar.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call, e: IOException) {
                    Log.d("TAG", "Failed - 获取漫画列表")
                }
            })
        }
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
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
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.search -> {
                //onSearchRequested()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_home -> {
                // Handle the camera action
            }
            R.id.nav_download -> {

            }
            R.id.nav_star -> {

            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
}
