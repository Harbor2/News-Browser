package com.habit.app.model

/**
 * home首页快速导航item
 */
data class AccessSingleData(
    val iconRes: Int,
    val name: String = "",
    val linkUrl: String = ""
) {
    var isEdit: Boolean = false
}