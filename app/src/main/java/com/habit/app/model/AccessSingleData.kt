package com.habit.app.model

/**
 * home首页快速导航item
 */
data class AccessSingleData(
    val iconRes: Int,
    val name: String = "",
    val linkUrl: String = "",
    var isSpecial: Boolean = false
) {
    /**
     * 排序索引
     */
    var sortIndex: Int = -1
    var isEdit: Boolean = false
}