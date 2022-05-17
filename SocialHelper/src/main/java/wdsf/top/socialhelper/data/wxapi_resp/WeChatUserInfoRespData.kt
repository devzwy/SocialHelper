package wdsf.top.socialhelper.data.wxapi_resp

import wdsf.top.socialhelper.data.base.SocialUserInfo

class WeChatUserInfoRespData(
    val city: String,
    val country: String,
    val headimgurl: String,
    val nickname: String,
    val openid: String,
    val privilege: List<String>,
    val province: String,
    val sex: Int,
    val unionid: String,
    val errcode: Int = 0,
    val errmsg: String = ""
) : SocialUserInfo()