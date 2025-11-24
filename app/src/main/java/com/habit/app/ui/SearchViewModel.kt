package com.habit.app.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.habit.app.model.HistoryData
import com.habit.app.model.db.DBManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SearchViewModel : ViewModel() {

    private val _input = MutableLiveData<String>()
    val input: LiveData<String> = _input
    fun setEditInput(input: String) {
        _input.value = input
    }



    private val _searchHistory = MutableStateFlow<ArrayList<HistoryData>>(arrayListOf())
    val searchHistory: StateFlow<ArrayList<HistoryData>> = _searchHistory
    fun loadHistory() {
        _searchHistory.value = DBManager.getDao().getAllHistoryFromTable()
    }


}