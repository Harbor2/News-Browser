package com.habit.app.viewmodel.tag

import androidx.lifecycle.ViewModel
import com.habit.app.data.model.WebViewData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class TagsViewModel() : ViewModel() {

    private val _publicTagCountObserver = MutableStateFlow(0)
    val publicTagCountObserver: StateFlow<Int> = _publicTagCountObserver

    fun setPublicTagCount(count: Int) {
        _publicTagCountObserver.value = count
    }

    private val _privacyTagCountObserver = MutableStateFlow(0)
    val privacyTagCountObserver: StateFlow<Int> = _privacyTagCountObserver

    fun setPrivacyTagCount(count: Int) {
        _privacyTagCountObserver.value = count
    }

    private val _snapSelectObserver = MutableStateFlow<WebViewData?>(null)
    val snapSelectObserver: StateFlow<WebViewData?> = _snapSelectObserver

    fun setSnapSelect(snapData: WebViewData) {
        _snapSelectObserver.value = snapData
    }
}