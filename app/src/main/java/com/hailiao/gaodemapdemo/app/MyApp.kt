package com.hailiao.gaodemapdemo.app

import android.app.Application
import com.hailiao.gaodemapdemo.R
import com.simple.spiderman.SpiderMan
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApp: Application() {

    companion object{
        private lateinit var mInstance: MyApp
        /** 获取实例 */
        fun instance() : MyApp = mInstance
    }

    override fun onCreate() {
        super.onCreate()
        mInstance = this

        // 调试工具初始化
        SpiderMan.init(this) //设置主题样式，内置了两种主题样式light和dark
            .setTheme(R.style.SpiderManTheme_Dark)
    }
}