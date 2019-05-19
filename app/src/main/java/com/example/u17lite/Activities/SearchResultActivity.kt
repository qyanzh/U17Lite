package com.example.u17lite.Activities

import android.app.SearchManager
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.u17lite.Adapters.ComicAdapter
import com.example.u17lite.DataBeans.Comic
import com.example.u17lite.R
import com.example.u17lite.handleListResponse
import com.example.u17lite.isWebConnect
import com.example.u17lite.sendOkHttpRequest
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_search_result.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException

class SearchResultActivity : AppCompatActivity() {

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_result)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        // Verify the action and get the query
        if (Intent.ACTION_SEARCH == intent.action) {
            query = intent.getStringExtra(SearchManager.QUERY)
            supportActionBar?.title = "\"$query\"的搜索结果"
            getComicList()
        }
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

    val comicList = mutableListOf<Comic>()
    var query: String? = null
    var hasMore: Boolean = true
    var currentPage = 0
    private fun getComicList(page: Int = currentPage + 1) {
        if (hasMore) {
            val address = "http://app.u17.com/v3/appV3_3/android/phone/search/searchResult?" +
                    "q=$query" +
                    "&page=$page" +
                    "&come_from=xiaomi" +
                    "&serialNumber=7de42d2e" +
                    "&v=4500102" +
                    "&model=MI+6" +
                    "&android_id=f5c9b6c9284551ad"
            sendOkHttpRequest(address, object : Callback {
                override fun onResponse(call: Call, response: Response) {
                    val responseString =
                        handleListResponse(response.body()!!.string())
                    comicList.addAll(responseString.list)
                    currentPage = responseString.currentPage
                    hasMore = responseString.hasMore
                    if (responseString.currentPage == 1) {
                        val adapter = ComicAdapter(
                            comicList,
                            this@SearchResultActivity
                        )
                        adapter.hasMore = hasMore
                        this@SearchResultActivity.runOnUiThread {
                            rcvComicRank.let {
                                it.adapter = adapter
                                it.emptyView = emptyView
                                it.setHasFixedSize(true)
                                it.layoutManager = LinearLayoutManager(this@SearchResultActivity)
                            }
                        }
                    } else {
                        this@SearchResultActivity.runOnUiThread {
                            rcvComicRank.let {
                                (it.adapter as ComicAdapter).hasMore = hasMore
                                it.adapter?.notifyDataSetChanged()
                            }
                        }
                    }
                }

                override fun onFailure(call: Call, e: IOException) {
                    Log.d("TAG", "Failed - 获取搜索结果")
                }
            })
            if (swipeRefreshLayout.isRefreshing) {
                swipeRefreshLayout.isRefreshing = false
                Snackbar.make(rcvComicRank, "刷新成功", Snackbar.LENGTH_SHORT).show()
            }
        }
    }
}
