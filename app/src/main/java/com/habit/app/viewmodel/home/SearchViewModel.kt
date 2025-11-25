package com.habit.app.viewmodel.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habit.app.data.model.HistoryData
import com.habit.app.data.db.DBManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

class SearchViewModel : ViewModel() {
    /**
     * 输入框
     */
    private val _input = MutableLiveData<String>()
    val input: LiveData<String> = _input
    fun setEditInput(input: String) {
        _input.value = input
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