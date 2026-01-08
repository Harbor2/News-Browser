package com.habit.app.data.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "habit.db"
        private const val DATABASE_VERSION = 1
    }

    override fun onCreate(db: SQLiteDatabase) {
        // 创建数据库表
        db.execSQL(DBConstant.CREATE_KEY_VALUE_TABLE)
        db.execSQL(DBConstant.CREATE_TAB_TABLE)
        db.execSQL(DBConstant.CREATE_HISTORY_TABLE)
        db.execSQL(DBConstant.CREATE_BOOKMARK_TABLE)
        db.execSQL(DBConstant.CREATE_FOLDER_TABLE)
        db.execSQL(DBConstant.CREATE_SEARCH_RECORD_TABLE)
        db.execSQL(DBConstant.CREATE_DOWNLOAD_TABLE)
        db.execSQL(DBConstant.CREATE_NEWS_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // 在数据库版本更新时执行操作
        // 可以在这里添加升级数据库的逻辑
    }
}