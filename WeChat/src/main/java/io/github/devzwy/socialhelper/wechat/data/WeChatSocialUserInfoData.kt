package io.github.devzwy.socialhelper.wechat.data

class WeChatSocialUserInfoData(
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
)