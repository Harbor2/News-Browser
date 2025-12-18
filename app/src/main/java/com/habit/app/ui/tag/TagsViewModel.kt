package com.habit.app.ui.tag

import androidx.lifecycle.ViewModel
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
}