package com.habit.app

import android.app.Application
import com.wyz.emlibrary.em.EMLibrary
import com.habit.app.model.db.DBManager

class MyApplication : Application() {
    companion object {
        lateinit var mContext: Application
    }

    override fun onCreate() {
        super.onCreate()
        mContext = this
        // 初始化
        initOnMainProcess()
    }

    private fun initOnMainProcess() {
        EMLibrary.init(this)
        DBManager.init(this)
    }
}