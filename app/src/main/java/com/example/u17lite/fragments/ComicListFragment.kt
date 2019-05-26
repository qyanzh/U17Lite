package com.example.u17lite.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.fragment.app.Fragment
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.u17lite.*
import com.example.u17lite.adapters.ComicAdapter
import com.example.u17lite.adapters.ComicDetailsLookup
import com.example.u17lite.adapters.ComicItemKeyProvider
import com.example.u17lite.dataBeans.Comic
import com.example.u17lite.dataBeans.DownloadItem
import com.example.u17lite.dataBeans.getDatabase
import com.example.u17lite.services.DownloadService
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_comic_list.*
import kotlinx.android.synthetic.main.fragment_comic_list.view.*
import okhttp3.*
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
    private var tracker: SelectionTracker<Long>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            addressPrefix = it.getString("prefix")!!
            addressPostfix = it.getString("postfix")!!
        }
        savedInstanceState?.let {
            tracker?.onRestoreInstanceState(it)
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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        tracker?.onSaveInstanceState(outState)
    }

    val comicList = mutableListOf<Comic>()
    var currentPage = 0
    var hasMore: Boolean = true
    var actionModeCallback = object : ActionMode.Callback {
        override fun onActionItemClicked(
            mode: ActionMode?,
            item: MenuItem?
        ): Boolean {
            return when (item?.itemId) {
                R.id.download -> {
                    Toast.makeText(context, "正在获取下载链接", Toast.LENGTH_SHORT).show()
                    val selections = (tracker?.selection?.sorted())?.map { it }
                    mode?.finish() // Action picked, so close the CAB
                    Thread {
                        Log.d(
                            "ComicListFragment", "onActionItemClicked: " +
                                    "${tracker?.selection?.size()}"
                        )
                        selections?.forEach {
                            val client = OkHttpClient()
                            val addressForIds =
                                "http://app.u17.com/v3/appV3_3/android/phone/comic/detail_static_new?" +
                                        "come_from=xiaomi" +
                                        "&comicid=$it" +
                                        "&serialNumber=7de42d2e" +
                                        "&v=4500102" +
                                        "&model=MI+6" +
                                        "&android_id=f5c9b6c9284551ad"
                            val request = Request.Builder().url(addressForIds).build()
                            val idsResponse = client.newCall(request).execute().body()!!.string()
                            val chapterIds = handleChapterIdsResponse(idsResponse)
                            chapterIds.forEach { chapterID ->
                                val addressForURLs =
                                    "http://app.u17.com/v3/appV3_3/android/phone/comic/chapterNew?" +
                                            "come_from=xiaomi" +
                                            "&serialNumber=7de42d2e" +
                                            "&v=4500102&model=MI+6" +
                                            "&chapter_id=$chapterID" +
                                            "&android_id=f5c9b6c9284551ad"
                                val request = Request.Builder().url(addressForURLs).build()
                                val urlResponse =
                                    client.newCall(request).execute().body()!!.string()
                                val downloadURL = handleDownloadUrlResponse(urlResponse)
                                getDatabase(context!!).downloadDao().insert(
                                    DownloadItem(it, chapterID, downloadURL)
                                )
                            }
                        }
                        activity!!.runOnUiThread {
                            Toast.makeText(
                                context!!,
                                "已添加${selections?.size}本漫画到下载队列",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        activity?.startService(
                            Intent(
                                activity,
                                DownloadService::class.java
                            ).putExtra("multiTask", 1)
                        )
                    }.start()
                    true
                }
                else -> false
            }
        }

        override fun onCreateActionMode(
            mode: ActionMode?,
            menu: Menu?
        ): Boolean {
            val inflater = mode?.menuInflater
            inflater?.inflate(R.menu.menu_download, menu)
            view?.swipeRefreshLayout?.isEnabled = false
            return true
        }

        override fun onPrepareActionMode(
            mode: ActionMode?,
            menu: Menu?
        ): Boolean {
            return false
        }


        override fun onDestroyActionMode(mode: ActionMode?) {
            mActionMode = null
            tracker?.clearSelection()
            view?.swipeRefreshLayout?.isEnabled = true
        }

    }

    var mActionMode: ActionMode? = null
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
                                adapter.tracker = SelectionTracker.Builder<Long>(
                                    "selection${activity?.toString()}",
                                    rcvComicRank,
                                    ComicItemKeyProvider(comicList),
                                    ComicDetailsLookup(it),
                                    StorageStrategy.createLongStorage()
                                ).withSelectionPredicate(
                                    SelectionPredicates.createSelectAnything()
                                ).build()
                                tracker = adapter.tracker
                                adapter.tracker?.let { tracker ->
                                    tracker.addObserver(object :
                                        SelectionTracker.SelectionObserver<Long>() {
                                        override fun onSelectionChanged() {
                                            if (tracker.hasSelection() && mActionMode == null) {
                                                mActionMode =
                                                    (activity as AppCompatActivity).startSupportActionMode(
                                                        actionModeCallback
                                                    )
                                            } else if (!tracker.hasSelection() && mActionMode != null) {
                                                mActionMode!!.finish()
                                                mActionMode = null
                                            } else {
                                                mActionMode?.title =
                                                    "已选择${tracker?.selection?.size()}项"
                                                Log.d(
                                                    "ComicListFragment", "onSelectionChanged: " +
                                                            "${tracker?.selection.size()}"
                                                )
                                            }
                                        }
                                    })
                                }
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
