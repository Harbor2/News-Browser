package com.habit.app.viewmodel.news

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.habit.app.data.ResultState
import com.habit.app.data.model.RealTimeNewsData
import com.habit.app.data.repority.PullNewsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PullNewsViewModel(
    private val repository: PullNewsRepository
): ViewModel() {

    private val _foxNewsObserver = MutableSharedFlow<ArrayList<RealTimeNewsData>>(replay = 0)
    val foxNewsObserver = _foxNewsObserver.asSharedFlow()
    fun pullFoxNews() {
        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.getNews()
            if (result is ResultState.Success) {
                withContext(Dispatchers.Main) {
                    _foxNewsObserver.emit(result.data)
                }
            }
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