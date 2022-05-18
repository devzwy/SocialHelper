package io.github.socialhelper.wechat.data

import com.tencent.mm.opensdk.modelmsg.SendAuth

/**
 * 微信授权成功获取时回传的实体
 */
data class WeChatSocialReqAuthRespData(
    /**
     * 每次调起授权页面用户点击确认授权后都会有该值，可用该值获取accessToken进行后续业务
     */
    val authCodeData: SendAuth.Resp,

    /**
     * accessToken实体 初始化微信时传入weChatAppSecretKey后有值
     */
    val socialAccessTokenData: WeChatSocialAccessTokenData? = null
)
