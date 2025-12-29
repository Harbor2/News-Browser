package com.habit.app.viewmodel.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.habit.app.data.db.DBManager
import com.habit.app.data.repority.ThinkWordRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

class SearchActivityModel(
    val repository: ThinkWordRepository
) : ViewModel() {
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

    /**
     * 联想词
     */
    private val _thinkWordObserver = MutableSharedFlow<Pair<String, ArrayList<String>>>(replay = 1)
    val thinkWordObserver: SharedFlow<Pair<String, ArrayList<String>>> = _thinkWordObserver
    fun loadThinkWord(keyWord: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.getThinkWordTemplateHabit(keyWord) { wordList ->
                _thinkWordObserver.tryEmit(Pair(keyWord, wordList))
            }
        }
    }
}

class SearchActivityModelFactory(
    private val repository: ThinkWordRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SearchActivityModel(repository) as T
    }
}