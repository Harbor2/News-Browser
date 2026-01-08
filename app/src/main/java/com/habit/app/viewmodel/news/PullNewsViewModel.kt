package com.habit.app.viewmodel.news

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.habit.app.data.ResultState
import com.habit.app.data.repority.PullNewsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class PullNewsViewModel(
    private val repository: PullNewsRepository
): ViewModel() {

    private val _foxNewsObserver = MutableSharedFlow<ResultState<String>>(replay = 0)
    val foxNewsObserver = _foxNewsObserver.asSharedFlow()
    fun pullFoxNews() {
        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.getNews()
            _foxNewsObserver.emit(result)
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