package com.habit.app.data

sealed class ResultState<out T> {
    data class Success<out T>(val data: T) : ResultState<T>()
    data class Error(val msg: String = "") : ResultState<Nothing>()
}