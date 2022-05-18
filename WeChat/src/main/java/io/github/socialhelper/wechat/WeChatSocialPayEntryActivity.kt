package io.github.socialhelper.wechat

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.tencent.mm.opensdk.modelbase.BaseReq
import com.tencent.mm.opensdk.modelbase.BaseResp
import com.tencent.mm.opensdk.modelmsg.SendAuth
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler
import io.github.social.SocialHelper
import io.github.social.utils.*
import io.github.socialhelper.wechat.data.WeChatConst.Companion.WECHAT_ACCESSTOKEN_REQUEST_URL
import io.github.socialhelper.wechat.data.WeChatSocialAccessTokenData
import io.github.socialhelper.wechat.data.WeChatSocialReqAuthRespData


/**
 * 客户端继承自该类即可
 */
open class WeChatSocialPayEntryActivity : Activity(), IWXAPIEventHandler {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        "${localClassName}->onCreate():mIWXAPI:${mIWXAPI}".logD()
        mIWXAPI.handleIntent(intent, this)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        "${localClassName}->onNewIntent():mIWXAPI:${mIWXAPI}".logD()
        setIntent(intent)
        mIWXAPI.handleIntent(intent, this)
    }

    override fun onDestroy() {
        super.onDestroy()
        "${localClassName}->onDestroy()".logD()
    }

    override fun onReq(p0: BaseReq) {

    }

    override fun onResp(mBaseResp: BaseResp) {

        (mBaseResp.errCode == BaseResp.ErrCode.ERR_OK).yes {
            "${localClassName}->onResp():mBaseResp:${mBaseResp.toJsonStr()}".logD()


        }.otherwise {

            "${localClassName}->onResp():mBaseResp:${mBaseResp.toJsonStr()}".logE()

        }

        mWeChatReqShareErrorListener = null
        mWeChatReqShareSuccessListener = null

        finish()

    }
}