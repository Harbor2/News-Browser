package com.habit.app.data.repority

import android.util.Log
import android.util.Xml
import com.habit.app.data.ResultState
import com.habit.app.data.TAG
import com.habit.app.data.model.RealTimeNewsData
import com.habit.app.http.ApiService
import com.habit.app.http.RetrofitClient
import org.xmlpull.v1.XmlPullParser


class PullNewsRepository {
    suspend fun getNews(category: String, url: String): ResultState<ArrayList<RealTimeNewsData>> {
        try {
            val api = RetrofitClient.instance.create(ApiService::class.java)
            val response = api.getNewsRss(url)

            if (response.isSuccessful) {
                val inputStream = response.body()?.byteStream() ?: return ResultState.Error()

                val parser: XmlPullParser = Xml.newPullParser()
                parser.setInput(inputStream, null)

                var curNews: RealTimeNewsData? = null
                var eventType = parser.eventType
                val newsItems = arrayListOf<RealTimeNewsData>()

                while (eventType != XmlPullParser.END_DOCUMENT) {
                    val tagName = parser.name
                    when (eventType) {
                        XmlPullParser.START_TAG -> {
                            when (tagName) {
                                "item" -> {
                                    curNews = RealTimeNewsData(category = category)
                                }
                                "title" -> {
                                    curNews?.let {
                                        it.title = parser.nextText()
                                    }
                                }
                                "link" -> {
                                    curNews?.let {
                                        it.newsUrl = parser.nextText()
                                    }
                                }
                                "pubDate" -> {
                                    curNews?.let {
                                        curNews.transFormatPubTime(parser.nextText())
                                    }
                                }
                                "guid" -> {
                                    curNews?.let {
                                        it.guid = parser.nextText()
                                    }
                                }
                                "thumbnail", "content" -> {
                                    curNews?.let {
                                        it.thumbUrl = parser.getAttributeValue(null, "url")
                                    }
                                }

                            }
                        }

                        XmlPullParser.END_TAG -> {
                            if (tagName == "item" && curNews != null) {
                                if (curNews.isValid()) {
                                    newsItems.add(curNews)
                                }
                                curNews = null
                            }
                        }
                    }
                    eventType = parser.next()
                }
                inputStream.close()
                return ResultState.Success(newsItems)
            } else {
                Log.e(TAG, "拉取新闻失败：${response.message()}")
                return ResultState.Error()
            }
        } catch (e: Exception) {
            Log.e(TAG, "拉取新闻异常，${e.message}")
            return ResultState.Error()
        }
    }
}