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
    /**
     * BBC 新闻
     * 全球：https://feeds.bbci.co.uk/news/world/rss.xml
     * 政治：https://feeds.bbci.co.uk/news/politics/rss.xml
     * 科学：https://feeds.bbci.co.uk/news/science_and_environment/rss.xml
     * 健康：https://feeds.bbci.co.uk/news/health/rss.xml
     * 体育：https://feeds.bbci.co.uk/sport/rss.xml
     * 科技：https://feeds.bbci.co.uk/news/technology/rss.xml
     * 商业：https://feeds.bbci.co.uk/news/business/rss.xml
     *
     * FOX 新闻
     * 全球：https://moxie.foxnews.com/google-publisher/world.xml
     * 政治：https://moxie.foxnews.com/google-publisher/politics.xml
     * 科学：https://moxie.foxnews.com/google-publisher/science.xml
     * 健康：https://moxie.foxnews.com/google-publisher/health.xml
     * 体育：https://moxie.foxnews.com/google-publisher/sports.xml
     * 科技：https://moxie.foxnews.com/google-publisher/tech.xml
     *
     * NYTime 新闻 （link为html）
     * 全球：https://rss.nytimes.com/services/xml/rss/nyt/World.xml
     * 政治：https://rss.nytimes.com/services/xml/rss/nyt/Politics.xml
     * 科学：https://rss.nytimes.com/services/xml/rss/nyt/Science.xml
     * 健康：https://rss.nytimes.com/services/xml/rss/nyt/Health.xml
     * 体育：https://rss.nytimes.com/services/xml/rss/nyt/Sports.xml
     * 科技：https://rss.nytimes.com/services/xml/rss/nyt/Technology.xml
     * 商业：https://rss.nytimes.com/services/xml/rss/nyt/Business.xml
     *
     * 卫报：
     * 全球: https://www.theguardian.com/world/rss
     * 政治：https://www.theguardian.com/politics/rss
     * 科学：https://www.theguardian.com/science/rss
     * 健康：https://www.theguardian.com/society/health/rss
     * 体育：https://www.theguardian.com/sport/rss
     * 科技：https://www.theguardian.com/technology/rss
     * 商业：https://www.theguardian.com/business/rss
     */
    suspend fun getNews(): ResultState<String> {
        try {
            val api = RetrofitClient.instance.create(ApiService::class.java)
            val response = api.getNews()
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
                                    curNews = RealTimeNewsData()
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
                                        it.pubTime = parser.nextText()
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
                Log.e(TAG, "解析完成的items：${newsItems}")
                return ResultState.Error()
            } else {
                Log.e(TAG, "用户信息查询失败，${response.message()}")
                return ResultState.Error()
            }
        } catch (e: Exception) {
            Log.e(TAG, "用户信息查询异常，${e.message}")
            return ResultState.Error()
        }
    }
}