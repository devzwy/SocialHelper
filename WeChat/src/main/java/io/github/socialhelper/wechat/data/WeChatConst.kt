package io.github.socialhelper.wechat.data

class WeChatConst {
    companion object {
        //微信查询accessToken的Api地址 format时分别传入 appid secret code
        const val WECHAT_ACCESSTOKEN_REQUEST_URL =
            "https://api.weixin.qq.com/sns/oauth2/access_token?appid=%s&secret=%s&code=%s&grant_type=authorization_code"

        //微信获取用户资料 format时分别传入access_token openid
        const val WECHAT_USERINFO_URL =
            "https://api.weixin.qq.com/sns/userinfo?access_token=%s&openid=%s"

        //授权域
        const val REQ_AUTH_SCOPE = "snsapi_userinfo"
    }
}