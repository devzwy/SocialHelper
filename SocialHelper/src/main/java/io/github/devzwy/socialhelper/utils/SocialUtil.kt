package io.github.devzwy.socialhelper.utils

import com.google.gson.Gson

val gson = Gson()

fun Any.toJsonStr(): String = gson.toJson(this)
fun String.logD() = SocialLogUtil.instance.logD(this)
fun String.logW() = SocialLogUtil.instance.logW(this)
fun String.logE() = SocialLogUtil.instance.logE(this)

fun <T> String.toObject(clazz: Class<T>): T {
    return gson.fromJson(this, clazz)
}

sealed class BooleanExt<out T> constructor(val boolean: Boolean)

object Otherwise : BooleanExt<Nothing>(true)

class WithData<out T>(val data: T) : BooleanExt<T>(false)

inline fun <T> Boolean.yes(block: () -> T): BooleanExt<T> = when {
    this -> {
        WithData(block())
    }
    else -> Otherwise
}

inline fun <T> Boolean.no(block: () -> T) = when {
    this -> Otherwise
    else -> {
        WithData(block())
    }
}

inline infix fun <T> BooleanExt<T>.otherwise(block: () -> T): T = when (this) {
    is Otherwise -> block()
    is WithData<T> -> this.data
}

inline operator fun <T> Boolean.invoke(block: () -> T) = yes(block)