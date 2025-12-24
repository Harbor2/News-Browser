package com.habit.app.viewmodel.home

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class BHActivityModel : ViewModel() {

    var initBookmarkUrl: String? = null
    fun setBookMarkInit(url: String? = null) {
        initBookmarkUrl = url
    }

    var editObserver = MutableLiveData(false)
    fun setEditObserver(edit: Boolean) {
        editObserver.value = edit
    }

    var bookmarkSelectAllObserver = MutableLiveData(false)
    fun setBookmarkSelectAll(all: Boolean) {
        bookmarkSelectAllObserver.value = all
    }

}