package io.github.devzwy.socialhelper

import android.app.Application
import io.github.devzwy.socialhelper.utils.SocialLogUtil
import io.github.devzwy.socialhelper.utils.logD
import io.github.devzwy.socialhelper.utils.logW
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
    val okHttpClient: OkHttpClient,

    /**
     * 支付宝AppId
     */
    val alipayAppId: String,

    /**
     * 支付宝商户号
     */
    val alipayPid: String,

    /**
     * 支付宝应用私钥
     */
    val alipayPrivateKey: String,

    /**
     * google client ID like:312345678907-aa345c1jn1c3kstealsio4aaqe8m888e.apps.googleusercontent.com
     */
    val googleClientId: String

) {

    companion object {
        inline fun buildSocialConfig(application: Application, block: Builder.() -> Unit) = Builder(
            application
        ).apply(block).build()
    }


    class Builder(val application: Application) {

        /**
         * 微信AppId
         */
        private var weChatAppId = ""

        /**
         * 微信secretKey
         */
        private var weChatAppSecretKey = ""

        /**
         * 内部网络请求使用
         */
        private var okHttpClient = OkHttpClient()

        /**
         * 支付宝AppId
         */
        private var alipayAppId = ""

        /**
         * 支付宝商户号
         */
        private var alipayPid = ""

        /**
         * 支付宝商户号
         */
        private var alipayPrivateKey = ""

        /**
         * google client ID like:312345678907-aa345c1jn1c3kstealsio4aaqe8m888e.apps.googleusercontent.com
         */
        private var googleClientId: String = ""

        /**
         * 开启日志  可选配置 默认关闭
         */
        fun enableLog(tag: String = "SocialHelper") {
            SocialLogUtil.init(tag, true)
        }

        /**
         * SDK内部所有网络请求将使用该httpClient完成，可选配置 默认使用SDK内部okhttp实现
         */
        fun buildCustomOkHttpClient(okHttpClient: OkHttpClient) {
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

        /**
         * 配置支付宝参数
         * [alipayAppId] 支付宝AppId
         * [alipayPid]支付宝商户号
         */
        fun enableAlipayPlatform(alipayAppId: String, alipayPid: String, privateKey: String) {
            this.alipayAppId = alipayAppId.trim()
            this.alipayPid = alipayPid.trim()
            this.alipayPrivateKey = privateKey.trim()

            if (this.alipayPid.isEmpty() || this.alipayPid.isEmpty() || this.alipayPrivateKey.isEmpty()) throw SocialException(
                "支付宝平台参数配置错误，请重新配置！"
            )

            "支付宝平台配置完成，[alipayAppId:${this.alipayAppId},alipayPid:${this.alipayPid},privateKey:${this.alipayPrivateKey}]".logD()
        }


        /**
         * 开启google平台
         * [clientId] google client ID like:312345678907-aa345c1jn1c3kstealsio4aaqe8m888e.apps.googleusercontent.com
         */
        fun enableGooglePlatform(clientId: String) {
            this.googleClientId = clientId.trim()
            "Google平台配置完成，[googleClientId:${this.googleClientId}]".logD()
        }

        fun build(): SocialConfig {
            return SocialConfig(
                this.application,
                this.weChatAppId,
                this.weChatAppSecretKey,
                this.okHttpClient,
                this.alipayAppId,
                this.alipayPid,
                this.alipayPrivateKey,
                this.googleClientId
            )
        }

    }

}