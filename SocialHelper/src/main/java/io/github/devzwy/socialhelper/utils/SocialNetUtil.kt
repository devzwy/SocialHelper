package io.github.devzwy.socialhelper.utils

import io.github.devzwy.socialhelper.SocialHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

object SocialNetUtil {

    /**
     * 发送get请求
     *
     */
    fun sendGet(url: String, onSucc: (String) -> Unit, onErr: (String) -> Unit) {
        SocialHelper.socialConfig.okHttpClient.newCall(
            Request.Builder()
                .url(url)
                .build()
        ).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                CoroutineScope(Dispatchers.Main).launch {
                    onErr("网络异常，${e.localizedMessage}")
                }
            }

            override fun onResponse(call: Call, response: Response) {

                if (response.isSuccessful && response.body != null) {
                    response.body!!.string().let { respStr ->
                        CoroutineScope(Dispatchers.Main).launch {
                            (respStr.isEmpty()).yes {
                                onErr("body.string was empty.")
                            }.otherwise {
                                onSucc(respStr)
                            }
                        }
                    }

                } else {
                    CoroutineScope(Dispatchers.Main).launch {
                        onErr("network error (${response.code})，${response.message}")
                    }
                }
            }
        })
    }


}