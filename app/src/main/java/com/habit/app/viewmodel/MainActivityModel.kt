package com.habit.app.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainActivityModel : ViewModel() {
    private val _loadObserver = MutableLiveData(false)
    val loadObserver: LiveData<Boolean> = _loadObserver
    fun setLoadObserver(load: Boolean) {
        _loadObserver.value = load
    }


    private val _searchObserver = MutableLiveData(false)
    val searchObserver: LiveData<Boolean> = _searchObserver
    fun setSearchObserver(cancel: Boolean) {
        _searchObserver.value = cancel
    }
}