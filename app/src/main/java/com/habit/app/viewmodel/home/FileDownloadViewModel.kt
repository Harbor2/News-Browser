package com.habit.app.viewmodel.home

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class FileDownloadViewModel() : ViewModel() {

    var editObserver = MutableLiveData(false)
    fun setEditObserver(edit: Boolean) {
        editObserver.value = edit
    }

    var selectAllObserver = MutableLiveData(false)
    fun setSelectAll(all: Boolean) {
        selectAllObserver.value = all
    }
}