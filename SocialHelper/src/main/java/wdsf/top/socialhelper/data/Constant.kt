package wdsf.top.socialhelper.data

/**
 * SDK常量存储类
 */
interface Constant {
    companion object {
        //默认的日志输出开关
        const val DEFAULT_LOG_STATUS = true

        //默认的日志TAG
        const val DEFAULT_LOG_TAG = "SocialHelper"

        //微信回传结果的类型 登陆
        const val WECHAT_RESULT_TYPE_LOGIN = 1

        //微信回传结果的类型 分享
        const val WECHAT_RESULT_TYPE_SHARE = 2

        //微信查询accessToken的Api地址 format时分别传入 appid secret code
        const val WECHAT_ACCESSTOKEN_REQUEST_URL =
            "https://api.weixin.qq.com/sns/oauth2/access_token?appid=%s&secret=%s&code=%s&grant_type=authorization_code"

        //微信查询accessToken是否有效 format时分别传入access_token openid
        const val WECHAT_ACCESSTOKEN_AUTH_URL =
            "https://api.weixin.qq.com/sns/auth?access_token=%s&openid=%s"

        //微信刷新acctoken format时分别传入APPID  REFRESH_TOKEN
        const val WECHAT_ACCESSTOKEN_REFRESH_URL =
            "https://api.weixin.qq.com/sns/oauth2/refresh_token?appid=%s&grant_type=refresh_token&refresh_token=%s"

        //微信获取用户资料 format时分别传入access_token openid
        const val WECHAT_USERINFO_URL =
            "https://api.weixin.qq.com/sns/userinfo?access_token=%s&openid=%s"



    }
}
