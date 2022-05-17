package top.wdsf.socialhelper.utils

import android.util.Log
import top.wdsf.socialhelper.data.Constant

/**
 * SDK日志输出类
 */
class Log {

    companion object {

        /**
         * 默认使用的日志输出TAG
         */
        private var TAG = Constant.DEFAULT_LOG_TAG

        private var enableLog = Constant.DEFAULT_LOG_STATUS


        fun setTag(tag: String) {
            TAG = tag
        }

        fun setEnable(enable: Boolean) {
            enableLog = enable
        }

        /**
         * 输出调试日志
         */
        fun debug(msg: String) {
            if (enableLog)
                Log.d(TAG, "${Thread.currentThread()}" + msg)
        }

        /**
         * 输出错误日志
         */
        fun error(errMsg: String) {
            if (enableLog)
                Log.e(TAG, "${Thread.currentThread()}"+errMsg)
        }
    }
}