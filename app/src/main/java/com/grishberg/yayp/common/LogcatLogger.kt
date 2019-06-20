package com.grishberg.yayp.common

import android.util.Log
import com.grishberg.common.Logger

class LogcatLogger : Logger {
    override fun d(tag: String, message: String) {
        Log.d(tag, message)
    }
}