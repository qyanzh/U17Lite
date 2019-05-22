package com.example.u17lite

import android.content.Context
import android.net.ConnectivityManager
import com.example.u17lite.dataBeans.Chapter
import com.example.u17lite.dataBeans.Comic
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

fun isWebConnect(context: Context): Boolean {
    val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    manager.activeNetworkInfo?.let {
        return it.isConnected
    }
    return false
}

fun sendOkHttpRequest(address: String, callback: okhttp3.Callback) {
    val client = OkHttpClient()
    val request = Request.Builder().url(address).build()
    client.newCall(request).enqueue(callback)
}


fun handleSubscribeResponse(response: String): Long {
    if (response.isNotEmpty()) {
        try {
            var jsonObject = JSONObject(response)
            jsonObject = jsonObject.getJSONObject("data")
                .getJSONObject("returnData").getJSONObject("comic")
            return jsonObject.getLong("last_update_time")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    return 0
}

fun handleImageListResponse(response: String): List<String> {
    if (response.isNotEmpty()) {
        try {
            var jsonObject = JSONObject(response)
            jsonObject = jsonObject.getJSONObject("data").getJSONObject("returnData")
            val jsonArray = jsonObject.getJSONArray("image_list")
            val size = jsonArray.length()
            val list = mutableListOf<String>()
            for (i in 0 until size) {
                list.add(jsonArray.getJSONObject(i).getString("img05"))
            }
            return list
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    return listOf()
}

fun handleChapterListResponse(response: String): List<Chapter> {
    if (response.isNotEmpty()) {
        try {
            var jsonObject = JSONObject(response)
            jsonObject = jsonObject.getJSONObject("data").getJSONObject("returnData")
            val jsonArray = jsonObject.getJSONArray("chapter_list")
            val size = jsonArray.length()
            val list = mutableListOf<Chapter>()
            for (i in 0 until size) {
                jsonArray.getJSONObject(i).let {
                    if (it.getInt("type") == 0) {
                        list.add(
                            Chapter(
                                it.getLong("chapter_id"),
                                it.getString("name"),
                                it.getString("smallPlaceCover"),
                                it.getLong("publish_time")
                            )
                        )
                    }
                }
            }
            return list
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    return listOf()
}

fun handleListResponse(response: String): Result<Comic> {
    if (response.isNotEmpty()) {
        try {
            var jsonObject = JSONObject(response)
            jsonObject = jsonObject.getJSONObject("data").getJSONObject("returnData")
            val jsonArray = jsonObject.getJSONArray("comics")
            val size = jsonArray.length()
            val list = mutableListOf<Comic>()
            for (i in 0 until size) {
                jsonArray.getJSONObject(i).let {
                    list.add(
                        Comic(
                            it.getLong("comicId"), it.getString("name"), it.getString("author"),
                            it.getString("description"), it.getString("cover")
                        )
                    )
                }
            }
            val hasMore = jsonObject.getBoolean("hasMore")
            val currentPage = jsonObject.getInt("page")
            return Result(list, currentPage, hasMore)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    return Result(listOf(), 0, false)
}

data class Result<T>(val list: List<T>, val currentPage: Int, val hasMore: Boolean)