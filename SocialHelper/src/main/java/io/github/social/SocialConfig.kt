package io.github.social

import android.app.Application
import io.github.social.utils.SocialLogUtil
import io.github.social.utils.logD
import io.github.social.utils.logW
import okhttp3.OkHttpClient

class SocialConfig private constructor(
    /**
     * 全局application
     */
    val application: Application,
    /**
     * 微信AppId
     */
    val weChatAppId: String,
    /**
     * 微信secretKey
     */
    val weChatAppSecretKey: String = "",
    /**
     * 内部网络请求使用
     */
    val okHttpClient:OkHttpClient
) {

    companion object{
        inline fun buildSocialConfig(application: Application, block: Builder.() -> Unit) = Builder(
            application
        ).apply(block).build()
    }


    class Builder(val application: Application) {

        /*微信AppId*/
        private var weChatAppId = ""

        /*微信secretKey*/
        private var weChatAppSecretKey = ""

        //内部网络请求使用
        private var okHttpClient = OkHttpClient()

        /**
         * 开启日志  可选配置 默认关闭
         */
        fun enableLog(tag:String = "SocialHelper") {
            SocialLogUtil.init(tag,true)
        }

        /**
         * SDK内部所有网络请求将使用该httpClient完成，可选配置 默认使用SDK内部okhttp实现
         */
        fun buildCustomOkHttpClient(okHttpClient: OkHttpClient){
            this.okHttpClient = okHttpClient
        }

        /**
         * 配置微信参数
         * [weChatAppId] weChatAppId
         * [weChatAppSecretKey] 可选 不配置无法获取用户资料和accessToken
         */
        fun enableWeChatPlatform(weChatAppId: String, weChatAppSecretKey: String = "") {

            this.weChatAppId = weChatAppId.trim()
            if (this.weChatAppId.isEmpty()) throw SocialException("开启微信平台时参数[weChatAppId]不能为空！")

            this.weChatAppSecretKey = weChatAppSecretKey.trim()
            if (this.weChatAppSecretKey.isEmpty()) "微信平台[weChatAppSecretKey]未配置，无法获取accessToken(将无法通过accessToken获取用户资料！)".logW()

            "微信平台配置完成! [weChatAppId:${this.weChatAppId}]${if (this.weChatAppSecretKey.isNotEmpty()) ",[weChatAppSecretKey:${this.weChatAppSecretKey}]" else ""}".logD()
        }

        fun build(): SocialConfig {
            return SocialConfig(application,weChatAppId,weChatAppSecretKey,okHttpClient)
        }

    }

}