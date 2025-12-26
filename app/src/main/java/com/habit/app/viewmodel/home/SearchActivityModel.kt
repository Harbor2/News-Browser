package com.habit.app.viewmodel.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.habit.app.data.db.DBManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

class SearchActivityModel : ViewModel() {
    private val _loadObserver = MutableLiveData(false)
    val loadObserver: LiveData<Boolean> = _loadObserver
    fun setLoadObserver(load: Boolean) {
        _loadObserver.value = load
    }


    private val _cancelObserver = MutableLiveData(true)
    val cancelObserver: LiveData<Boolean> = _cancelObserver
    fun setCancelObserver(cancel: Boolean) {
        _cancelObserver.value = cancel
    }


    /**
     * 搜索历史
     */
    private val _searchHistory = MutableSharedFlow<ArrayList<String>>(replay = 1)
    val searchHistory: SharedFlow<ArrayList<String>> = _searchHistory
    fun loadHistory() {
        _searchHistory.tryEmit(DBManager.getDao().getAllSearchRecords())
    }
}