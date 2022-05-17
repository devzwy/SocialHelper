package top.wdsf.socialhelper.data.wxapi_resp

/**
 * 微信accessToken回传数据
 */
data class WeChatAccessTokenResultData(
    val access_token: String,
    val expires_in: Int,
    val openid: String,
    val refresh_token: String,
    val scope: String,
    val errcode: Int = 0,
    val errmsg: String = ""
)