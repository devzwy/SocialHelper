package top.wdsf.socialhelper.utils

import com.google.gson.Gson

fun Any.toJsonStr(): String {
    return Gson().toJson(this)
}