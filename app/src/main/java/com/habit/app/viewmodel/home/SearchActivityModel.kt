package com.habit.app.viewmodel.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.habit.app.data.model.HistoryData
import com.habit.app.data.db.DBManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

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
    private val _searchHistory = MutableStateFlow<ArrayList<HistoryData>>(arrayListOf())
    val searchHistory: StateFlow<ArrayList<HistoryData>> = _searchHistory
    fun loadHistory() {
        _searchHistory.value = DBManager.getDao().getAllHistoryFromTable()
    }
}