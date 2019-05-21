package com.example.u17lite.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.u17lite.R
import com.example.u17lite.adapters.ComicAdapter
import com.example.u17lite.dataBeans.Comic
import com.example.u17lite.handleListResponse
import com.example.u17lite.isWebConnect
import com.example.u17lite.sendOkHttpRequest
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_comic_list.*
import kotlinx.android.synthetic.main.fragment_comic_list.view.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException

class ComicListFragment : Fragment() {

    companion object {
        @JvmStatic
        fun newInstance(prefix: String, postfix: String) =
            ComicListFragment().apply {
                arguments = Bundle().apply {
                    putString("prefix", prefix)
                    putString("postfix", postfix)
                }
            }
    }

    lateinit var addressPrefix: String
    lateinit var addressPostfix: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            addressPrefix = it.getString("prefix")!!
            addressPostfix = it.getString("postfix")!!
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_comic_list, container, false)
        if (!isWebConnect(context!!)) {
            Toast.makeText(context, "请检查网络连接", Toast.LENGTH_SHORT).show()
        }
        getComicListFromServer()
        view.rcvComicRank.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if ((recyclerView.layoutManager as LinearLayoutManager).findLastCompletelyVisibleItemPosition() + 1 == recyclerView.adapter?.itemCount) {
                        Log.d("TAG", "到底了")
                        getComicListFromServer()
                    }
                }
            }
        })
        view.swipeRefreshLayout.setOnRefreshListener {
            if (isWebConnect(context!!)) {
                hasMore = true
                currentPage = 0
                comicList.clear()
                rcvComicRank.adapter?.notifyDataSetChanged()
                getComicListFromServer()
//                rcvComicRank.runAnimation()
            } else {
                Toast.makeText(context, "请检查网络连接", Toast.LENGTH_SHORT).show()
                swipeRefreshLayout.isRefreshing = false
            }
        }
        return view
    }

    val comicList = mutableListOf<Comic>()
    var currentPage = 0
    var hasMore: Boolean = true
    private fun getComicListFromServer(page: Int = currentPage + 1) {
        if (hasMore) {
            val address = addressPrefix +
                    "&page=$page" + addressPostfix

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
                            activity
                        )
                        adapter.hasMore = hasMore
                        activity?.runOnUiThread {
                            rcvComicRank.let {
                                it.adapter = adapter
                                it.emptyView = emptyView
                                it.setHasFixedSize(true)
                                it.layoutManager = LinearLayoutManager(activity)
                            }
                        }
                    } else {
                        activity?.runOnUiThread {
                            rcvComicRank.let {
                                (it.adapter as ComicAdapter).hasMore = hasMore
                                it.adapter?.notifyDataSetChanged()
                            }
                        }
                    }
                }

                override fun onFailure(call: Call, e: IOException) {
                    Log.d("TAG", "Failed - $address")
                }
            })
            swipeRefreshLayout?.let {
                if (it.isRefreshing) {
                    it.isRefreshing = false
                    Snackbar.make(rcvComicRank, "刷新成功", Snackbar.LENGTH_SHORT).show()
                }
            }
        }
    }
}
