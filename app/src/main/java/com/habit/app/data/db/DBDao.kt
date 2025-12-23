package com.habit.app.data.db

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.habit.app.data.model.BookmarkData
import com.habit.app.data.model.FolderData
import com.habit.app.data.model.HistoryData
import com.habit.app.data.TAG
import com.habit.app.data.model.WebViewData

/**
 * 文件删除时，先本地对比是否存在相同名称文件若存在则改名。完成上述操作后插入数据库
 * 文件恢复时，先将文件复制出去、改名。删除数据库
 */
class DBDao(private val dbHelper: DBHelper) {

    /*
     * 默认用户id
     */
    private val defaultUserId = "default"

    /**
     * 通用key-value存储
     */
    fun updateKeyValue(key: String, value: String, userId: String = defaultUserId): String {
        val db: SQLiteDatabase = dbHelper.writableDatabase
        try {
            val contentValues = ContentValues()
            contentValues.put(DBConstant.KEY_NAME, key)
            contentValues.put(DBConstant.KEY_VALUE, value)
            contentValues.put(DBConstant.KEY_USER_ID, userId)
            db.replace(DBConstant.KEY_VALUE_TABLE, null, contentValues)
            Log.d("key_value", "数据库key_value表更新：key:$key,value：$value")
        } catch (e: Exception) {
            Log.e("key_value", "数据库key_value表更新异常：${e.message}")
        }
        return value
    }

    /**
     * 通用key-value读取
     */
    fun getValueByKey(key: String, userId: String = defaultUserId): String? {
        val db: SQLiteDatabase = dbHelper.writableDatabase
        val sql =
            "SELECT ${DBConstant.KEY_VALUE} FROM ${DBConstant.KEY_VALUE_TABLE} WHERE ${DBConstant.KEY_NAME} = '$key' AND ${DBConstant.KEY_USER_ID} = '$userId'"
        val cursor = db.rawQuery(sql, null)
        var valueStr: String? = null
        while (cursor.moveToNext()) {
            val index = cursor.getColumnIndex(DBConstant.KEY_VALUE)
            valueStr = if (index != -1) {
                cursor.getString(index)
            } else {
                null
            }
        }
        cursor.close()
        return valueStr
    }

    /*
     * ************************  folder 相关 *******************************
     */

    fun addFolder(folderData: FolderData) {
        val db: SQLiteDatabase = dbHelper.writableDatabase
        try {
            val value = ContentValues()
            value.put(DBConstant.FOLDER_PARENT_ID, folderData.parentId)
            value.put(DBConstant.FOLDER_NAME, folderData.folderName)
            db.insert(DBConstant.TABLE_FOLDER, null, value)
            Log.d(TAG, "插入Folder:文件路径信息${folderData.folderName}")
        } catch (e: Exception) {
            Log.e(TAG, "插入Folder异常：${e.message}")
        }
    }

    fun renameFolder(folderId: Int, newName: String) {
        val db: SQLiteDatabase = dbHelper.writableDatabase
        try {
            val values = ContentValues().apply {
                put(DBConstant.FOLDER_NAME, newName)
            }

            val whereClause = "${DBConstant.FOLDER_ID} = ?" // WHERE 条件
            val whereArgs = arrayOf(folderId.toString())

            db.update(DBConstant.TABLE_FOLDER, values, whereClause, whereArgs)
        } catch (e: Exception) {
            Log.e(TAG, "folder更新异常：${e.message}")
        }
    }

    fun getSubFolder(parentFolderId: Int): ArrayList<FolderData> {
        try {
            val db: SQLiteDatabase = dbHelper.writableDatabase
            val sql = "select * from ${DBConstant.TABLE_FOLDER} where ${DBConstant.FOLDER_PARENT_ID} = '${parentFolderId}'"
            val cursor = db.rawQuery(sql, null)
            val folderList: ArrayList<FolderData> = arrayListOf()
            while (cursor.moveToNext()) {
                val folderId = cursor.getColumnIndex(DBConstant.FOLDER_ID)
                val nameIndex = cursor.getColumnIndex(DBConstant.FOLDER_NAME)
                if (nameIndex < 0 || folderId < 0) {
                    continue
                }
                folderList.add(
                    FolderData(
                        cursor.getInt(folderId),
                        parentFolderId,
                        cursor.getString(nameIndex)
                    )
                )
            }
            cursor.close()
            return folderList
        } catch (e: Exception) {
            Log.e(TAG, "获取子目录异常：${e.message}")
            return arrayListOf()
        }
    }

    fun deleteFolders(folderIds: List<Int>) {
        if (folderIds.isEmpty()) return
        val db = dbHelper.writableDatabase
        try {
            db.beginTransaction()

            val allIdsToDelete = mutableSetOf<Int>()
            folderIds.forEach { folderId ->
                allIdsToDelete.add(folderId)
                allIdsToDelete.addAll(getAllSubFolderIds(db, folderId))
            }
            val idsString = allIdsToDelete.joinToString(",")
            db.execSQL("DELETE FROM ${DBConstant.TABLE_FOLDER} WHERE ${DBConstant.FOLDER_ID} IN ($idsString)")
            // 删除 bookmark
            db.execSQL("DELETE FROM ${DBConstant.TABLE_BOOKMARK} WHERE ${DBConstant.BOOKMARK_FOLDER_ID} IN ($idsString)")
            db.setTransactionSuccessful()
        } catch (e: Exception) {
            Log.e(TAG, "删除目录异常：${e.message}")
        } finally {
            db.endTransaction()
        }
    }

    /**
     * 递归获取所有子目录ID（包含子目录的子目录）
     */
    private fun getAllSubFolderIds(db: SQLiteDatabase, parentId: Int): ArrayList<Int> {
        val result = arrayListOf<Int>()
        val cursor = db.query(
            DBConstant.TABLE_FOLDER,
            arrayOf(DBConstant.FOLDER_ID),
            "${DBConstant.FOLDER_PARENT_ID} = ?",
            arrayOf(parentId.toString()),
            null, null, null
        )
        cursor.use {
            while (it.moveToNext()) {
                val childId = it.getInt(it.getColumnIndexOrThrow(DBConstant.FOLDER_ID))
                result.add(childId)
                result.addAll(getAllSubFolderIds(db, childId)) // 递归
            }
        }
        return result
    }

    /*
     * ************************  bookmark 相关 *******************************
     */

    /**
     * 插入单个文件
     */
    fun insertBookmarkToTable(bookmarkData: BookmarkData) {
        val db: SQLiteDatabase = dbHelper.writableDatabase
        try {
            val value = ContentValues()
            value.put(DBConstant.BOOKMARK_SIGN, bookmarkData.sign)
            value.put(DBConstant.BOOKMARK_NAME, bookmarkData.name)
            value.put(DBConstant.BOOKMARK_URL, bookmarkData.url)
            value.put(DBConstant.BOOKMARK_FOLDER_ID, bookmarkData.folderId)
            value.put(DBConstant.BOOKMARK_ICON_BITMAP, bookmarkData.webIconPath)
            db.replace(DBConstant.TABLE_BOOKMARK, null, value)
            Log.d(TAG, "插入bookmark:文件路径信息${bookmarkData.name}")
        } catch (e: Exception) {
            Log.e(TAG, "插入bookmark异常：${e.message}")
        }
    }

    /**
     * 查询folderId下的bookmark
     */
    fun getBookmarksByFolderId(folderId: Int): ArrayList<BookmarkData> {
        val db: SQLiteDatabase = dbHelper.writableDatabase
        val sql = "select * from ${DBConstant.TABLE_BOOKMARK} where ${DBConstant.BOOKMARK_FOLDER_ID} = $folderId order by ${DBConstant.BOOKMARK_ID} desc"
        val cursor = db.rawQuery(sql, null)
        val qrInfoList: ArrayList<BookmarkData> = arrayListOf()
        while (cursor.moveToNext()) {
            val signIndex = cursor.getColumnIndex(DBConstant.BOOKMARK_SIGN)
            val nameIndex = cursor.getColumnIndex(DBConstant.BOOKMARK_NAME)
            val urlIndex = cursor.getColumnIndex(DBConstant.BOOKMARK_URL)
            val iconPathIndex = cursor.getColumnIndex(DBConstant.BOOKMARK_ICON_BITMAP)
            if (signIndex < 0 || nameIndex < 0 || urlIndex < 0 || iconPathIndex < 0) {
                continue
            }
            val qrInfo = BookmarkData(
                cursor.getString(signIndex),
                cursor.getString(nameIndex),
                cursor.getString(urlIndex),
                folderId,
                cursor.getString(iconPathIndex)
            )
            qrInfoList.add(qrInfo)
        }
        cursor.close()
        Log.d(TAG, "********************** BEGIN ****************************")
        qrInfoList.forEach { item ->
            Log.d(TAG, "读取Bookmark文件${item}")
        }
        Log.d(TAG, "************************ END **************************")
        return qrInfoList
    }

    fun getBookMarkByUrl(url: String): BookmarkData? {
        if (url.isEmpty()) return null
        try {
            val db: SQLiteDatabase = dbHelper.writableDatabase
            val sql = "select * from ${DBConstant.TABLE_BOOKMARK} where ${DBConstant.BOOKMARK_URL} = '$url'"
            val cursor = db.rawQuery(sql, null)
            val qrInfoList: ArrayList<BookmarkData> = arrayListOf()
            while (cursor.moveToNext()) {
                val signIndex = cursor.getColumnIndex(DBConstant.BOOKMARK_SIGN)
                val nameIndex = cursor.getColumnIndex(DBConstant.BOOKMARK_NAME)
                val urlIndex = cursor.getColumnIndex(DBConstant.BOOKMARK_URL)
                val folderIdIndex = cursor.getColumnIndex(DBConstant.BOOKMARK_FOLDER_ID)
                val iconPathIndex = cursor.getColumnIndex(DBConstant.BOOKMARK_ICON_BITMAP)
                if (signIndex < 0 || nameIndex < 0 || urlIndex < 0 || folderIdIndex < 0 || iconPathIndex < 0) {
                    continue
                }
                qrInfoList.add(
                    BookmarkData(
                        cursor.getString(signIndex),
                        cursor.getString(nameIndex),
                        cursor.getString(urlIndex),
                        cursor.getInt(folderIdIndex),
                        cursor.getString(iconPathIndex)
                    )
                )
            }
            cursor.close()
            return qrInfoList.firstOrNull()
        } catch (e: Exception) {
            Log.e(TAG, "查询bookmark异常：${e.message}")
            return null
        }
    }

    fun updateBookmarkBySign(bookmarkData: BookmarkData) {
        val db: SQLiteDatabase = dbHelper.writableDatabase
        try {
            val values = ContentValues().apply {
                put(DBConstant.BOOKMARK_NAME, bookmarkData.name)
                put(DBConstant.BOOKMARK_URL, bookmarkData.url)
                put(DBConstant.BOOKMARK_FOLDER_ID, bookmarkData.folderId)
                put(DBConstant.BOOKMARK_ICON_BITMAP, bookmarkData.webIconPath)
            }
            val whereClause = "${DBConstant.BOOKMARK_SIGN} = ?"
            val whereArgs = arrayOf(bookmarkData.sign)

            db.update(DBConstant.TABLE_BOOKMARK, values, whereClause, whereArgs)
        } catch (e: Exception) {
            Log.e(TAG, "bookmark更新异常：${e.message}")
        }
    }

    fun deleteBookmarkByFolderId(folderIds: List<Int>) {
        if (folderIds.isEmpty()) return
        val db = dbHelper.writableDatabase
        try {
            val placeholders = folderIds.joinToString(",") { "?" }
            val whereClause = "${DBConstant.BOOKMARK_FOLDER_ID} IN ($placeholders)"
            val whereArgs = folderIds.map { it.toString() }.toTypedArray()
            db.delete(DBConstant.TABLE_BOOKMARK, whereClause, whereArgs)
        } catch (e: Exception) {
            Log.e(TAG, "删除bookmark异常：${e.message}")
        }
    }

    fun deleteBookmarkByUrl(url: String) {
        if (url.isEmpty()) return
        val db = dbHelper.writableDatabase
        try {
            val whereClause = "${DBConstant.BOOKMARK_URL} = ?"
            val whereArgs = arrayOf(url)
            db.delete(DBConstant.TABLE_BOOKMARK, whereClause, whereArgs)
        } catch (e: Exception) {
            Log.e(TAG, "删除bookmark异常：${e.message}")
        }
    }

    /*
     * ************************  tab 相关 *******************************
     */

    /**
     * 插入单个文件
     */
    fun insertWebSnapToTable(webData: WebViewData) {
        val db: SQLiteDatabase = dbHelper.writableDatabase
        try {
            val value = ContentValues()
            value.put(DBConstant.TAB_NAME, webData.name)
            value.put(DBConstant.TAB_SIGN, webData.sign)
            value.put(DBConstant.TAB_URL, webData.url)
            value.put(DBConstant.TAB_PHONE_TYPE, if (webData.isPhoneMode != false) 1 else 0)
            value.put(DBConstant.TAB_PRIVACY_TYPE, if (webData.isPrivacyMode == true) 1 else 0)
            value.put(DBConstant.TAB_COVER_BITMAP, webData.coverBitmapPath)
            value.put(DBConstant.TAB_ICON_BITMAP, webData.webIconPath)
            value.put(DBConstant.TAB_UPDATE_TIME, System.currentTimeMillis())
            db.replace(DBConstant.TABLE_TAB, null, value)
            Log.d(TAG, "插入web快照:文件路径信息${webData.name}")
        } catch (e: Exception) {
            Log.e(TAG, "插入Web快照异常：${e.message}")
        }
    }

    /**
     * 更新数据库数据
     */
    fun updateWebSnapItem(webData: WebViewData) {
        val db: SQLiteDatabase = dbHelper.writableDatabase
        try {
            val values = ContentValues().apply {
                if (webData.isPhoneMode != null) {
                    put(DBConstant.TAB_PHONE_TYPE, if (webData.isPhoneMode != false) 1 else 0)
                }
                if (webData.isPrivacyMode != null) {
                    put(DBConstant.TAB_PRIVACY_TYPE, if (webData.isPrivacyMode == true) 1 else 0)
                }
                if (webData.name.isNotEmpty()) {
                    put(DBConstant.TAB_NAME, webData.name)
                }
                if (webData.url.isNotEmpty()) {
                    put(DBConstant.TAB_URL, webData.url)
                }
                if (webData.coverBitmapPath.isNotEmpty()) {
                    put(DBConstant.TAB_COVER_BITMAP, webData.coverBitmapPath)
                }
                if (webData.webIconPath.isNotEmpty()) {
                    put(DBConstant.TAB_ICON_BITMAP, webData.webIconPath)
                }
                put(DBConstant.TAB_UPDATE_TIME, System.currentTimeMillis())
            }

            val whereClause = "${DBConstant.TAB_SIGN} = ?" // WHERE 条件
            val whereArgs = arrayOf(webData.sign)

            db.update(DBConstant.TABLE_TAB, values, whereClause, whereArgs)
            Log.e(TAG, "更新Web快照：${webData.toString()}")
        } catch (e: Exception) {
            Log.e(TAG, "更新Web快照异常：${e.message}")
        }
    }

    /**
     * TABLE_ALL_FILE: 分类查询
     */
    fun getWebSnapsFromTable(): ArrayList<WebViewData> {
        val db: SQLiteDatabase = dbHelper.writableDatabase
        val sql = "select * from ${DBConstant.TABLE_TAB} order by ${DBConstant.TAB_UPDATE_TIME} desc"
        val cursor = db.rawQuery(sql, null)
        val qrInfoList: ArrayList<WebViewData> = arrayListOf()
        while (cursor.moveToNext()) {
            val nameIndex = cursor.getColumnIndex(DBConstant.TAB_NAME)
            val signIndex = cursor.getColumnIndex(DBConstant.TAB_SIGN)
            val urlIndex = cursor.getColumnIndex(DBConstant.TAB_URL)
            val loadTypeIndex = cursor.getColumnIndex(DBConstant.TAB_PHONE_TYPE)
            val privacyTypeIndex = cursor.getColumnIndex(DBConstant.TAB_PRIVACY_TYPE)
            val bitmapPathIndex = cursor.getColumnIndex(DBConstant.TAB_COVER_BITMAP)
            val iconPathIndex = cursor.getColumnIndex(DBConstant.TAB_ICON_BITMAP)
            if (nameIndex < 0 || signIndex < 0 || urlIndex < 0 || loadTypeIndex < 0 || privacyTypeIndex < 0 || bitmapPathIndex < 0 || iconPathIndex < 0) {
                continue
            }
            val qrInfo = WebViewData(
                cursor.getString(nameIndex),
                cursor.getString(signIndex),
                cursor.getString(urlIndex),
                cursor.getInt(loadTypeIndex) == 1,
                cursor.getInt(privacyTypeIndex) == 1,
                cursor.getString(bitmapPathIndex),
                cursor.getString(iconPathIndex)
            )
            qrInfoList.add(qrInfo)
        }
        cursor.close()
        Log.d(TAG, "************************ BEGIN **************************")
        qrInfoList.forEach { item ->
            Log.d(TAG, "读取web文件${item}")
        }
        Log.d(TAG, "************************ ENG **************************")
        return qrInfoList
    }

    fun getWebSnapsBySign(sign: String?): WebViewData? {
        if (sign.isNullOrEmpty()) {
            return null
        }
        val db: SQLiteDatabase = dbHelper.writableDatabase
        val sql = "select * from ${DBConstant.TABLE_TAB} where ${DBConstant.TAB_SIGN} = '${sign}'"
        val cursor = db.rawQuery(sql, null)
        while (cursor.moveToNext()) {
            val nameIndex = cursor.getColumnIndex(DBConstant.TAB_NAME)
            val signIndex = cursor.getColumnIndex(DBConstant.TAB_SIGN)
            val urlIndex = cursor.getColumnIndex(DBConstant.TAB_URL)
            val phoneTypeIndex = cursor.getColumnIndex(DBConstant.TAB_PHONE_TYPE)
            val privacyTypeIndex = cursor.getColumnIndex(DBConstant.TAB_PRIVACY_TYPE)
            val bitmapPathIndex = cursor.getColumnIndex(DBConstant.TAB_COVER_BITMAP)
            val iconPathIndex = cursor.getColumnIndex(DBConstant.TAB_ICON_BITMAP)
            if (nameIndex < 0 || signIndex < 0 || urlIndex < 0 || phoneTypeIndex < 0 || privacyTypeIndex < 0 || bitmapPathIndex < 0 || iconPathIndex < 0) {
                continue
            }
            return WebViewData(
                cursor.getString(nameIndex),
                cursor.getString(signIndex),
                cursor.getString(urlIndex),
                cursor.getInt(phoneTypeIndex) == 1,
                cursor.getInt(privacyTypeIndex) == 1,
                cursor.getString(bitmapPathIndex),
                cursor.getString(iconPathIndex),
            )
        }
        cursor.close()
        return null
    }

    /**
     * 删除单个文件
     */
    fun deleteWebSnapFromTable(webData: WebViewData) {
        val db: SQLiteDatabase = dbHelper.writableDatabase
        val sql = "delete from ${DBConstant.TABLE_TAB} where ${DBConstant.TAB_SIGN} = '${webData.sign}'"
        db.execSQL(sql)
        Log.d(TAG, "数据库移除web快照${webData.name}")
    }

    /**
     * 删除多个文件
     */
    fun deleteWebSnapsFromTableByFilter(privacy: Boolean) {
        val db: SQLiteDatabase = dbHelper.writableDatabase
        try {
            val db: SQLiteDatabase = dbHelper.writableDatabase
            val sql = "delete from ${DBConstant.TABLE_TAB} where ${DBConstant.TAB_PRIVACY_TYPE} = if($privacy, 1, 0)"
            db.execSQL(sql)
            Log.d(TAG, "数据库移除web快照, filter: $privacy")
        } catch (e: Exception) {
            Log.d(TAG, "数据库移除web快照异常：${e.message}")
        } finally {
            // 结束事务
            db.endTransaction()
        }
    }

    /**
     * 删除多个文件
     */
    fun deleteWebSnapsFromTable(dataList: List<WebViewData>) {
        val db: SQLiteDatabase = dbHelper.writableDatabase
        try {
            // 开始事务
            db.beginTransaction()
            for (webData in dataList) {
                // 使用 delete 方法执行删除操作
                db.delete(
                    DBConstant.TABLE_TAB,
                    "${DBConstant.TAB_SIGN} = ?",
                    arrayOf(webData.sign)
                )
            }
            // 设置事务成功
            db.setTransactionSuccessful()
            Log.d(TAG, "移除${dataList.size}个web快照")
        } catch (e: Exception) {
            Log.d(TAG, "移除web快照异常：${e.message}")
        } finally {
            // 结束事务
            db.endTransaction()
        }
    }

    fun clearWebSnaps() {
        val db: SQLiteDatabase = dbHelper.writableDatabase
        try {
            val sql = "DELETE FROM ${DBConstant.TABLE_TAB}"
            db.execSQL(sql)
        } catch (e: Exception) {
            Log.d(TAG, "清空快照失败")
        }
    }

    /*
     * ************************  history 相关 *******************************
     */

    /**
     * 插入单个文件
     */
    fun insertHistoryToTable(historyData: HistoryData) {
        val db: SQLiteDatabase = dbHelper.writableDatabase
        try {
            val value = ContentValues()
            value.put(DBConstant.HISTORY_NAME, historyData.name)
            value.put(DBConstant.HISTORY_NAME, historyData.url)
            value.put(DBConstant.HISTORY_ICON_BITMAP, historyData.webIconPath)
            value.put(DBConstant.HISTORY_UPDATE_TIME, historyData.timeStamp)
            db.replace(DBConstant.TABLE_HISTORY, null, value)
            Log.d(TAG, "插入history:文件路径信息${historyData.name}")
        } catch (e: Exception) {
            Log.e(TAG, "插入history快照异常：${e.message}")
        }
    }

    /**
     * TABLE_HISTORY: 查询所有history
     */
    fun getAllHistoryFromTable(): ArrayList<HistoryData> {
        val db: SQLiteDatabase = dbHelper.writableDatabase
        val sql = "select * from ${DBConstant.TABLE_HISTORY} order by ${DBConstant.HISTORY_UPDATE_TIME} desc"
        val cursor = db.rawQuery(sql, null)
        val qrInfoList: ArrayList<HistoryData> = arrayListOf()
        while (cursor.moveToNext()) {
            val nameIndex = cursor.getColumnIndex(DBConstant.HISTORY_NAME)
            val urlIndex = cursor.getColumnIndex(DBConstant.HISTORY_URL)
            val iconPathIndex = cursor.getColumnIndex(DBConstant.HISTORY_ICON_BITMAP)
            val timeStampIndex = cursor.getColumnIndex(DBConstant.HISTORY_UPDATE_TIME)
            if (nameIndex < 0 || urlIndex < 0 || iconPathIndex < 0 || timeStampIndex < 0) {
                continue
            }
            val qrInfo = HistoryData(
                cursor.getString(nameIndex),
                cursor.getString(urlIndex),
                cursor.getString(iconPathIndex),
                cursor.getLong(timeStampIndex)
            )
            qrInfoList.add(qrInfo)
        }
        cursor.close()
        Log.d(TAG, "********************* BEGIN *****************************")
        qrInfoList.forEach { item ->
            Log.d(TAG, "读取History文件${item}")
        }
        Log.d(TAG, "********************** END ****************************")
        return qrInfoList
    }

    /**
     * 删除单个文件
     */
    fun deleteHistoryFromTable(history: HistoryData) {
        val db: SQLiteDatabase = dbHelper.writableDatabase
        val sql = "delete from ${DBConstant.TABLE_HISTORY} where ${DBConstant.HISTORY_URL} = '${history.url}' and ${DBConstant.HISTORY_UPDATE_TIME} = ${history.timeStamp}"
        db.execSQL(sql)
        Log.d(TAG, "数据库移除history${history.name}")
    }

    fun clearHistories() {
        val db: SQLiteDatabase = dbHelper.writableDatabase
        try {
            val sql = "DELETE FROM ${DBConstant.TABLE_HISTORY}"
            db.execSQL(sql)
        } catch (e: Exception) {
            Log.d(TAG, "清空history失败")
        }
    }
}