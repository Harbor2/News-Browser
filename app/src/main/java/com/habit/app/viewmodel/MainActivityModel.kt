package com.habit.app.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class MainActivityModel : ViewModel() {
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

    private val _searchUrlObserver = MutableSharedFlow<String>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val searchUrlObserver = _searchUrlObserver.asSharedFlow()
    fun setSearchUrl(url: String) {
        _searchUrlObserver.tryEmit(url)
    }

    private val _keyWorkSearchObserver = MutableSharedFlow<String>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val keyWorkSearchObserver = _keyWorkSearchObserver.asSharedFlow()
    fun setKeyWorkSearch(keyWork: String) {
        _keyWorkSearchObserver.tryEmit(keyWork)
    }

    private val _newsMoreObserver = MutableSharedFlow<Boolean>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val newsMoreObserver = _newsMoreObserver.asSharedFlow()
    fun setNewsMoreObserver() {
        _newsMoreObserver.tryEmit(true)
    }
}