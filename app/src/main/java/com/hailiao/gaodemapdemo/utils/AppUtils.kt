package com.hailiao.gaodemapdemo.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import android.content.IntentFilter





/**
 * 显示Toast消息
 * @param msg 消息内容
 * @param duration 显示时间长度
 */
fun Context.showToast(msg: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, null, duration).apply {
        setText(msg)
        show()
    }
}

/**
 * 显示SnackBar
 * @param msg 消息内容
 * @param duration 显示时间长度
 */
fun View.showSnackBar(msg: String, duration: Int = Snackbar.LENGTH_LONG) {
    Snackbar.make(this, msg, duration).show()
}

/**
 * 清空整个Task并跳转页面
 * @param clz 跳转页面
 */
fun Activity.clearTaskGoTo(clz: Class<*>) {
    val intent = Intent(this, clz)
    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
    startActivity(intent)
}

/**
 * 关闭当前页面并跳转下一个页面
 * @param clz 跳转页面
 */
fun Activity.finishGoTo(clz: Class<*>) {
    goTo(clz)
    finish()
}

/**
 * 跳转下一个页面，如果栈中有相同的ACT会只保留最新一个到前台
 * @param clz 跳转页面
 */
fun Activity.goTo(clz: Class<*>) {
    val intent = getClearTopIntent(clz)
    startActivity(intent)
}

/**
 * 跳转下一个页面，并带返回结果
 * @param clz 跳转页面
 * @param requestCode 请求码
 */
fun Activity.goToForResult(clz: Class<*>, requestCode: Int) {
    val intent = getClearTopIntent(clz)
    startActivityForResult(intent, requestCode)
}

/**
 * 获取跳转页面Intent
 * @param clz 跳转页面
 * @return [Intent] Intent
 */
fun Activity.getClearTopIntent(clz: Class<*>) : Intent {
    val intent = Intent(this, clz)
    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
    return intent
}

/**
 * 读取系统电量
 * @receiver Context
 * @return Int
 */
fun Context.getSystemBattery(): Int {
    val batteryInfoIntent = applicationContext.registerReceiver(
        null,
        IntentFilter(Intent.ACTION_BATTERY_CHANGED)
    )
    val level = batteryInfoIntent?.getIntExtra("level", 0)?: 0
    val batterySum = batteryInfoIntent?.getIntExtra("scale", 100)?: 100
    return 100 * level / batterySum
}

