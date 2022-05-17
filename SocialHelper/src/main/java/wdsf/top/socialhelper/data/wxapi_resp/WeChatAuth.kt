package wdsf.top.socialhelper.data.wxapi_resp

import com.tencent.mm.opensdk.modelmsg.SendAuth
import wdsf.top.socialhelper.data.TokenFrom
import wdsf.top.socialhelper.data.base.SocialAuth

class WeChatAuth(val tokenFrom: TokenFrom, val wxAuthRespBean: SendAuth.Resp, val weChatAccessTokenResultData: WeChatAccessTokenResultData? = null) :
    SocialAuth() {

    override fun from(): TokenFrom {
        return tokenFrom
    }
}