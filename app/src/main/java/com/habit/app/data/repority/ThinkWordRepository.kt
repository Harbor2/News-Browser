package com.habit.app.data.repority

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import kotlin.math.min

/**
 * 联想词
 */
class ThinkWordRepository() {
    private val okHttpClient = OkHttpClient()

    suspend fun getThinkWordTemplateHabit(word: String, callback: (ArrayList<String>) -> Unit) {
        val result = ArrayList<String>()

        val url =
            "https://suggestqueries.google.com/complete/search?client=firefox&q=$word&hl=en"

        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        try {
            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    withContext(Dispatchers.Main) {
                        callback.invoke(result)
                    }
                    return
                }

                val body = response.body?.string() ?: run {
                    withContext(Dispatchers.Main) {
                        callback.invoke(result)
                    }
                    return
                }

                // google suggest 返回格式:
                // ["habit", ["habit tracker","habit meaning","habit building"...]]
                val jsonArray = JSONArray(body)
                val suggestArray = jsonArray.getJSONArray(1)

                for (i in 0 until min(suggestArray.length(), 5) ) {
                    result.add(suggestArray.getString(i))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        withContext(Dispatchers.Main) {
            callback.invoke(result)
        }
    }
}