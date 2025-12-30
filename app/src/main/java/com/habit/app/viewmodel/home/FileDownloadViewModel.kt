package com.habit.app.viewmodel.home

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class FileDownloadViewModel() : ViewModel() {

    private val _emptyObserver = MutableLiveData<Boolean>()
    val emptyObserver: MutableLiveData<Boolean>
        get() = _emptyObserver
    fun setEmptyObserver(isEmpty: Boolean) {
        _emptyObserver.value = isEmpty
    }
}