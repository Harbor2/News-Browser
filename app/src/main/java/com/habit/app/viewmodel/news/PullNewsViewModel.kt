package com.habit.app.viewmodel.news

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.habit.app.data.NEWS_CATEGORY_BUSINESS
import com.habit.app.data.NEWS_CATEGORY_HEALTH
import com.habit.app.data.NEWS_CATEGORY_POLITICS
import com.habit.app.data.NEWS_CATEGORY_SCIENCE
import com.habit.app.data.NEWS_CATEGORY_SPORTS
import com.habit.app.data.NEWS_CATEGORY_TECHNOLOGY
import com.habit.app.data.NEWS_CATEGORY_WORLD
import com.habit.app.data.ResultState
import com.habit.app.data.TAG
import com.habit.app.data.businessList
import com.habit.app.data.healthList
import com.habit.app.data.model.RealTimeNewsData
import com.habit.app.data.politicsList
import com.habit.app.data.repority.PullNewsRepository
import com.habit.app.data.scienceList
import com.habit.app.data.sportsList
import com.habit.app.data.techList
import com.habit.app.data.worldList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlin.collections.flatten

class PullNewsViewModel(
    private val repository: PullNewsRepository
): ViewModel() {

    private val _pullNewObserver = MutableSharedFlow<ArrayList<RealTimeNewsData>>(replay = 0)
    val pullNewObserver = _pullNewObserver.asSharedFlow()

    fun pullNews(category: String = NEWS_CATEGORY_WORLD) {
        viewModelScope.launch {
            val worldNewsList = coroutineScope {
                val urlList = when (category) {
                    NEWS_CATEGORY_WORLD -> worldList
                    NEWS_CATEGORY_POLITICS -> politicsList
                    NEWS_CATEGORY_SCIENCE -> scienceList
                    NEWS_CATEGORY_HEALTH -> healthList
                    NEWS_CATEGORY_SPORTS -> sportsList
                    NEWS_CATEGORY_TECHNOLOGY -> techList
                    NEWS_CATEGORY_BUSINESS -> businessList
                    else -> null
                }
                if (urlList.isNullOrEmpty()) {
                    return@coroutineScope emptyList()
                }
                urlList
                    .map { url ->
                        async(Dispatchers.IO) {
                            val result = repository.getNews(category, url)
                            Log.d(TAG, "小组数据返回")
                            if (result is ResultState.Success) {
                                result.data
                            } else {
                                emptyList()
                            }
                        }
                    }
                    .awaitAll()
                    .flatten()
            }
            Log.d(TAG, "大组数据返回")
            Log.d(TAG, "--------------------------------")
            _pullNewObserver.emit(ArrayList(worldNewsList))
        }
    }
}

class PullNewsModelFactory(
    private val repository: PullNewsRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PullNewsViewModel(repository) as T
    }
}