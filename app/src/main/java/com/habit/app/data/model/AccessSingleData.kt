package com.habit.app.data.model

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


    override fun toString(): String {
        return "AccessSingleData(name='$name', index:$sortIndex)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AccessSingleData

        return linkUrl == other.linkUrl
    }

    override fun hashCode(): Int {
        return linkUrl.hashCode()
    }
}