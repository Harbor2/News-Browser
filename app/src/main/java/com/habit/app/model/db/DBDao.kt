package com.habit.app.model.db

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.util.Log

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
}