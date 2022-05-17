package top.wdsf.socialhelper.utils

import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import top.wdsf.socialhelper.SocialHelper
import top.wdsf.socialhelper.data.Constant
import top.wdsf.socialhelper.data.wxapi_resp.WeChatAccessTokenResultData
import top.wdsf.socialhelper.data.wxapi_resp.WeChatUserInfoRespData
import java.io.IOException

object NetUtil {

    fun getWxUserInfo(accessToken: String,
                      openId: String,
                      onSucc: (WeChatUserInfoRespData) -> Unit,
                      onErr: (String) -> Unit
    ) {

        get(String.format(
            Constant.WECHAT_USERINFO_URL,
            accessToken,
            openId
        ), {

            Gson().fromJson(
                it,
                WeChatUserInfoRespData::class.java
            ).apply {

                if (errcode == 0) {
                    onSucc(this)
                } else {
                    //刷新失败
                    onErr(errmsg)
                }
            }

        }, {
            onErr(it)
        })
    }

    /**
     * 查询accessToken是否有效
     */
    fun refreshWxAccessToken(
        appId: String,
        refreshToken: String,
        onSucc: (WeChatAccessTokenResultData) -> Unit,
        onErr: (String) -> Unit
    ) {
        get(String.format(
            Constant.WECHAT_ACCESSTOKEN_REFRESH_URL,
            appId,
            refreshToken
        ), {

            Gson().fromJson(
                it,
                WeChatAccessTokenResultData::class.java
            ).apply {

                if (errcode == 0) {
                    //刷新成功
                    //写到缓存
                    SpUtils.putAccessToken(this)
                    onSucc(this)
                } else {
                    //刷新失败
                    onErr(errmsg)
                }
            }

        }, {
            onErr(it)
        })
    }

    /**
     * 查询accessToken是否有效
     */
    fun authWxAccessToken(
        accessToken: String,
        openId: String,
        onSucc: () -> Unit,
        onErr: (String) -> Unit
    ) {
        get(String.format(
            Constant.WECHAT_ACCESSTOKEN_AUTH_URL,
            accessToken,
            openId
        ), {
            Gson().fromJson(it, WeChatAccessTokenResultData::class.java).apply {

                if (errcode == 0) {
                    //token有效
                    onSucc()
                } else {
                    //token失效
                    onErr(errmsg)
                }
            }

        }, {
            onErr(it)
        })
    }


    /**
     * 网络获取accessToken
     */
    fun getWxAccessToken(
        authCode: String,
        onSucc: (WeChatAccessTokenResultData) -> Unit,
        onErr: (String) -> Unit,
    ) {
        get(String.format(
            Constant.WECHAT_ACCESSTOKEN_REQUEST_URL,
            SocialHelper.mSocialConfig.weChatAppId,
            SocialHelper.mSocialConfig.weChatAppSecretKey,
            authCode
        ), {

            Gson().fromJson(
                it,
                WeChatAccessTokenResultData::class.java
            ).apply {

                if (errcode == 0) {
                    //写到缓存
                    SpUtils.putAccessToken(this)
                    //获取成功
                    onSucc(this)
                } else {
                    //获取失败
                    onErr(errmsg)
                }
            }

        }, {
            onErr(it)
        })
    }

    /**
     * 发送get请求
     */
    fun get(url: String, onSucc: (String) -> Unit,onErr: (String) -> Unit) {
        SocialHelper.mSocialConfig.okHttpClient.newCall(
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

                if (response.isSuccessful && response.body != null
                ) {

                    val respStr = response.body!!.string()
                    if (!respStr.isEmpty()) {
                        //当做请求成功处理
                        CoroutineScope(Dispatchers.Main).launch {
                            onSucc(respStr)
                        }
                    } else {
                        //当做请求成功处理
                        CoroutineScope(Dispatchers.Main).launch {
                            onErr(response.message)
                        }
                    }

                } else {
                    CoroutineScope(Dispatchers.Main).launch {
                        onErr(response.message)
                    }
                }
            }
        })
    }


}