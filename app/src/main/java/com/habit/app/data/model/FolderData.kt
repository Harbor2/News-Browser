package com.habit.app.data.model

data class FolderData(
    val folderId: Int = -1,
    val parentId: Int = -1,
    val folderName: String = ""
) {

    var mSelect: Boolean? = null

    override fun toString(): String {
        return "FolderData(folderId=$folderId, parentId=$parentId, folderName='$folderName')"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FolderData

        if (parentId != other.parentId) return false
        if (folderName != other.folderName) return false

        return true
    }

    override fun hashCode(): Int {
        var result = parentId
        result = 31 * result + folderName.hashCode()
        return result
    }
}
