package com.example.u17lite

import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

fun sendOkHttpRequest(address: String, callback: okhttp3.Callback) {
    val client = OkHttpClient()
    val request = Request.Builder().url(address).build()
    client.newCall(request).enqueue(callback)
}


fun handleChapterListResponse(response: String): List<Comic.Chapter> {
    if (response.isNotEmpty()) {
        try {
            var jsonObject = JSONObject(response)
            jsonObject = jsonObject.getJSONObject("data").getJSONObject("returnData")
            val jsonArray = jsonObject.getJSONArray("chapter_list")
            val size = jsonArray.length()
            val list = mutableListOf<Comic.Chapter>()
            for (i in 0 until size) {
                jsonArray.getJSONObject(i).let {
                    if (it.getInt("type") == 0) {
                        list.add(Comic.Chapter(it.getInt("chapter_id"), it.getString("name")))
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
                            it.getString("name"),
                            it.getInt("comicId"), it.getString("author"),
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