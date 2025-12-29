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

    val noNetObserver = MutableLiveData(false)

    private val _searchObserver = MutableLiveData(false)
    val searchObserver: LiveData<Boolean> = _searchObserver
    fun setSearchObserver(cancel: Boolean) {
        _searchObserver.value = cancel
    }

    private val _phoneModeObserver = MutableLiveData(true)
    val phoneModeObserver: LiveData<Boolean> = _phoneModeObserver
    fun setPhoneModeObserver(phoneMode: Boolean) {
        _phoneModeObserver.value = phoneMode
    }

    private val _privacyObserver = MutableLiveData(false)
    val privacyObserver: LiveData<Boolean> = _privacyObserver
    fun setPrivacyObserver(privacy: Boolean) {
        _privacyObserver.value = privacy
    }
}