package com.habit.app.model.db

import android.content.Context
import android.util.Log
import com.habit.app.model.TAG

object DBManager {
    private lateinit var dbHelper: DBHelper
    private lateinit var dbDao: DBDao

    /**
     * 数据库初始化
     */
    fun init(context: Context) {
        dbHelper = DBHelper(context)
        dbDao = DBDao(dbHelper)
        Log.d(TAG, "数据库初始化成功")
    }

    fun getDao(): DBDao {
        return dbDao
    }

    /**
     * 关闭数据库
     */
    fun close() {
        try {
            dbHelper.close()
            Log.d(TAG, "关闭数据库")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}