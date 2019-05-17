package com.example.u17lite

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.lang.Exception

fun sendOkHttpRequest(address: String, callback: okhttp3.Callback) {
    val client = OkHttpClient()
    val request = Request.Builder().url(address).build()
    client.newCall(request).enqueue(callback)
}

fun handleListResponse(response: String): List<Comic> {
    if (response.isNotEmpty()) {
        try {
            var jsonObject = JSONObject(response)
            Log.d("TAG", jsonObject.getString("msg"))
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
            return list
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    return listOf()
}