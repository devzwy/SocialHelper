package top.wdsf.socialhelper

import com.tencent.mm.opensdk.modelmsg.SendAuth
import top.wdsf.socialhelper.data.Platform
import top.wdsf.socialhelper.data.SocialConfig
import top.wdsf.socialhelper.data.SocialShareMediaType
import top.wdsf.socialhelper.data.SocialShareToType
import top.wdsf.socialhelper.data.base.SocialAuth
import top.wdsf.socialhelper.data.base.SocialShareConfig
import top.wdsf.socialhelper.data.base.SocialUserInfo
import top.wdsf.socialhelper.data.shareconfig.SocialWeChatShareConfig
import top.wdsf.socialhelper.data.wxapi_resp.WeChatAuth
import top.wdsf.socialhelper.listeners.OnSocialAuthListener
import top.wdsf.socialhelper.listeners.OnSocialShareListener
import top.wdsf.socialhelper.listeners.OnSocialUserInfoListener
import top.wdsf.socialhelper.utils.Log
import top.wdsf.socialhelper.utils.NetUtil
import top.wdsf.socialhelper.utils.toJsonStr

/**
 * SDK所有操作发起类
 */
class SocialHelper private constructor() {

    companion object {

        /**
         * 配置类
         */
        lateinit var mSocialConfig: SocialConfig

        /**
         * 三方登陆的结果回调
         */
        private var onSocialAuthListener: OnSocialAuthListener<*>? = null

        /**
         * 三方分享结果回调
         */
        private var onSocialShareListener: OnSocialShareListener? = null

        /**
         * 获取登陆监听器
         */
        fun <T : SocialAuth> getSocialLoginListener(): OnSocialAuthListener<T>? {
            return this.onSocialAuthListener as? OnSocialAuthListener<T>
        }

        /**
         * 获取分享监听器
         */
        fun getSocialShareListener(): OnSocialShareListener? {
            return this.onSocialShareListener
        }

        /**
         * 初始化配置，优先调用，否则其他任何功能将无法正常使用
         * [socialHelperConfig] 可使用[top.wdsf.socialhelper.SocialConfig.Build]进行构造
         */
        fun init(socialConfig: SocialConfig) {
            this.mSocialConfig = socialConfig
        }

        /**
         * 分享 微信的分享无法准确获取到分享的结果 取消分享时也会进入分享成功回调
         * [platform] 分享平台
         * [shareConfig] 分享的具体配置类
         * [onSocialShareListener] 分享结果回调
         */
        fun <T : SocialShareConfig> share(
            platform: Platform,
            shareConfig: T,
            onSocialShareListener: OnSocialShareListener
        ) {

            Log.debug("发起分享[${platform.name}],[${shareConfig.toJsonStr()}],[${onSocialShareListener}]")

            this.onSocialShareListener = onSocialShareListener
            when (platform) {
                Platform.WECHAT -> {

                    //微信仅支持分享到会话、朋友圈、收藏，切分享类型为小程序时不允许分享到朋友圈
                    if (shareConfig.socialShareToType != SocialShareToType.SCENE_TIMELINE
                        && shareConfig.socialShareToType != SocialShareToType.SCENE_SESSION
                        && shareConfig.socialShareToType != SocialShareToType.SCENE_FAVORITE
                    ) {
                        onSocialShareListener.onShareError(
                            platform,
                            "微信平台仅支持分享到会话、收藏、朋友圈，请使用枚举[SCENE_SESSION/SCENE_FAVORITE/SCENE_TIMELINE]"
                        )
                        return
                    }

                    if (shareConfig.socialShareMediaType == SocialShareMediaType.WECHAT_MINI_PROGRAM && shareConfig.socialShareToType == SocialShareToType.SCENE_TIMELINE) {
                        onSocialShareListener.onShareError(platform, "微信小程序不支持分享到朋友圈功能！")
                        return
                    }

                    (shareConfig as? SocialWeChatShareConfig)?.let {
                        mSocialConfig.weChatApi?.sendReq(it.wxRequest)
                    }

                }

                else -> onSocialShareListener.onShareError(platform, "不支持的分享平台")
            }
        }

        /**
         * 授权获取
         * [platform] 授权平台
         * [extData] 扩展字段 可用平台【微信】 默认为空
         * 微信平台时代表【state】参数，官方说明：用于保持请求和回调的状态，授权请求后原样带回给第三方。该参数可用于防止 csrf 攻击（跨站请求伪造攻击），建议第三方带上该参数，可设置为简单的随机数加 session 进行校验。在state传递的过程中会将该参数作为url的一部分进行处理，因此建议对该参数进行url encode操作，防止其中含有影响url解析的特殊字符（如'#'、'&'等）导致该参数无法正确回传。
         * [forceNetwork] 是否强制从网络获取token 默认关闭状态，即本地缓存存在切有效时自动返回缓存token，否则去刷新token再返回
         * [onSocialAuthListener] 回调用户授权结果 根据平台不同请传入不同的实体接收 [Platform.WECHAT]时使用[top.wdsf.socialhelper.data.wxapi_resp.WeChatAuth]接收
         */
        fun <T : SocialAuth> startAuth(
            platform: Platform,
            extData: String? = null,
            forceNetwork: Boolean = false,
            onSocialAuthListener: OnSocialAuthListener<T>
        ) {
            this.onSocialAuthListener = onSocialAuthListener
            mSocialConfig.forceNetwork = forceNetwork
            when (platform) {
                Platform.WECHAT -> {
                    if (mSocialConfig.weChatAppId.isNullOrEmpty() || mSocialConfig.weChatApi == null) {
                        "微信平台未配置，请配置后再试！".let {
                            Log.error(it)
                            this.onSocialAuthListener?.onSocialAuthError(platform, it)
                        }
                        return
                    }

                    if (!mSocialConfig.weChatApi!!.isWXAppInstalled) {
                        "未监测到微信App，请安装微信App后再试！".let {
                            Log.error(it)
                            this.onSocialAuthListener?.onSocialAuthError(platform, it)
                        }
                        return
                    }

                    mSocialConfig.weChatApi!!.sendReq(SendAuth.Req().also {
                        it.scope = "snsapi_userinfo"
                        if (extData != null)
                            it.extData = extData
                    })

                }

                else -> {
                    this.onSocialAuthListener?.onSocialAuthError(platform, "该平台暂未支持，即将更新！")
                }

            }
        }


        /**
         * 获取用户资料
         * [mSocialAuth] 授权成功后返回的实体
         * [onSocialUserInfoListener] 回调用户结果回传 根据平台不同请传入不同的实体接收 [Platform.WECHAT]时使用[top.wdsf.socialhelper.data.wxapi_resp.WeChatUserInfoRespData]接收
         */
        fun <T : SocialUserInfo> getUserInfo(
            platform: Platform, mSocialAuth: SocialAuth,
            onSocialUserInfoListener: OnSocialUserInfoListener<T>
        ) {
            when (platform) {
                Platform.WECHAT -> {
                    (mSocialAuth as WeChatAuth).let {

                        if (it.weChatAccessTokenResultData == null || it.weChatAccessTokenResultData.access_token.isNullOrEmpty() || it.weChatAccessTokenResultData.openid.isNullOrEmpty()) {
                            onSocialUserInfoListener.onGetUserInfoError(
                                platform,
                                "请填入正确的参数，[access_token,openid]"
                            )
                            return
                        }

                        NetUtil.getWxUserInfo(
                            it.weChatAccessTokenResultData.access_token,
                            it.weChatAccessTokenResultData.openid,
                            {
                                onSocialUserInfoListener.onGetUserInfoSuccess(platform, it as T)
                            },
                            {
                                onSocialUserInfoListener.onGetUserInfoError(platform, it)
                            })
                    }
                }

                else -> onSocialUserInfoListener.onGetUserInfoError(platform, "不支持的平台！")
            }
        }


    }


}