package com.habit.app.model

/**
 * home首页快速导航item
 */
data class AccessSingleData(
    val iconResName: String = "",
    val name: String = "",
    val linkUrl: String = ""
) {
    /**
     * 排序索引
     */
    var sortIndex: Int = Int.MAX_VALUE

    var isSpecial: Boolean = false
    var isEdit: Boolean = false
}