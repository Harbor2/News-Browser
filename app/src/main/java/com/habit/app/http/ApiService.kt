package com.habit.app.http

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET

interface ApiService {
    /**
     * 拉取新闻
     */
    @GET("https://feeds.bbci.co.uk/news/world/rss.xml")
//    @GET("https://moxie.foxnews.com/google-publisher/world.xml")
//    @GET("https://rss.nytimes.com/services/xml/rss/nyt/World.xml")
//    @GET("https://www.theguardian.com/world/rss")
    suspend fun getNews(): Response<ResponseBody>
}