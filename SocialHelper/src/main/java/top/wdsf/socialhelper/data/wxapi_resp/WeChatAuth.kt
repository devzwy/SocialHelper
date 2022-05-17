package top.wdsf.socialhelper.data.wxapi_resp

import com.tencent.mm.opensdk.modelmsg.SendAuth
import top.wdsf.socialhelper.data.TokenFrom
import top.wdsf.socialhelper.data.base.SocialAuth

class WeChatAuth(val tokenFrom: TokenFrom, val wxAuthRespBean: SendAuth.Resp, val weChatAccessTokenResultData: WeChatAccessTokenResultData? = null) :
    SocialAuth() {

    override fun from(): TokenFrom {
        return tokenFrom
    }
}