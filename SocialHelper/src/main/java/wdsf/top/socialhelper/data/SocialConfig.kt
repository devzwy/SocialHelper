package wdsf.top.socialhelper.data

import android.content.Context
import com.tencent.mm.opensdk.openapi.IWXAPI
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import okhttp3.OkHttpClient
import wdsf.top.socialhelper.utils.Log
import wdsf.top.socialhelper.utils.SpUtils
import java.lang.NullPointerException

/**
 * 配置类
 */
class SocialConfig private constructor(
    val weChatAppId: String?,
    val weChatApi: IWXAPI?,
    val weChatAppSecretKey: String?,
    val okHttpClient: OkHttpClient,
    var forceNetwork: Boolean = false
) {

    class Build(private val context: Context) {

        //微信AppID
        private var weChatAppId: String? = null

        //微信secretKey 未配置时不会查询accToken
        private var weChatAppSecretKey: String? = null

        //微信实例
        private var weChatApi: IWXAPI? = null

        //内部网络请求使用
        private var okHttpClient = OkHttpClient()

        /**
         * 可选配置 设置日志输出的TAG
         * [tag] 指定日志输出的TAG 默认：SocialHelper
         */
        fun logTag(tag: String = Constant.DEFAULT_LOG_TAG): Build {
            Log.setTag(tag)
            return this
        }

        /**
         * 可选配置 控制是否输出日志
         * [enable] true 输出  false 关闭  默认：true
         */
        fun logEnable(enable: Boolean = Constant.DEFAULT_LOG_STATUS): Build {
            Log.setEnable(enable)
            return this
        }

        /**
         * 配置http客户端，内部网络请求使用，未配置时将使用默认的配置
         */
        fun okHttpClient(okHttpClient: OkHttpClient) {
            this.okHttpClient = okHttpClient
        }

        /**
         * 可选配置
         * 传入微信AppId 应用唯一标识，在微信开放平台提交应用审核通过后获得
         */
        fun weChat(appId: String, appSecretKey: String? = null): Build {
            this.weChatAppId = appId
            this.weChatAppSecretKey = appSecretKey
            if (this.weChatAppId.isNullOrEmpty()) throw NullPointerException("微信AppId配置错误！")
            if (this.weChatAppSecretKey.isNullOrEmpty()) {
                Log.error("微信AppSecretKey未配置，将不会为您查询accessToken,请使用返回的code上传给自己的服务器获取！")
            }
            weChatApi = WXAPIFactory.createWXAPI(context, appId, false)
            // 将该app注册到微信
            weChatApi?.registerApp(appId)
            return this
        }

        fun build(): SocialConfig {

            //这里判断一下，如果一个平台都没有配置 直接build配置文件时提示一下
            if (weChatAppId.isNullOrEmpty()) {
                Log.error("未监测到配置的平台数据，所有功能无法使用！")
            }

            if (!weChatAppId.isNullOrEmpty()) {
                Log.debug("微信平台配置成功[appId:${weChatAppId},appSecretKey:${weChatAppSecretKey}]")
            }

            //初始化sp
            SpUtils.init(context)

            return SocialConfig(
                weChatAppId,
                weChatApi,
                weChatAppSecretKey,
                okHttpClient
            )
        }
    }
}