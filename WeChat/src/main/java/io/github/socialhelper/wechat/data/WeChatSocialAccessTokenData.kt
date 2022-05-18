package io.github.socialhelper.wechat.data

/**
 * 微信accessToken回传数据
 */
data class WeChatSocialAccessTokenData(
    val access_token: String,
    val expires_in: Int,
    val openid: String,
    val refresh_token: String,
    val scope: String,
    val errcode: Int = 0,
    val errmsg: String = ""
)