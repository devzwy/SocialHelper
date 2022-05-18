package io.github.social.utils

import android.util.Log

class SocialLogUtil private constructor() {


    companion object {
        var tag: String = "SocialHelper"
        var enableLog: Boolean = false

        val instance: SocialLogUtil by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            SocialLogUtil()
        }

        fun init(tag: String, enable: Boolean) {
            this.tag = tag
            this.enableLog = enable
        }
    }


    fun logD(content: String) {
        if (enableLog) {
            Log.d(tag, "[${Thread.currentThread()}]:${content}")
        }
    }

    fun logE(err: String) {
        if (enableLog) {
            Log.e(tag, "[${Thread.currentThread()}]:${err}")
        }
    }

    fun logW(warning: String) {
        if (enableLog) {
            Log.w(tag, "[${Thread.currentThread()}]:${warning}")
        }
    }


}