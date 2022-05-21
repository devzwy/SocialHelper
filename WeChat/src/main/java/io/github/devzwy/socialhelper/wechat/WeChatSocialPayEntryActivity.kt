package io.github.devzwy.socialhelper.wechat

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.tencent.mm.opensdk.modelbase.BaseReq
import com.tencent.mm.opensdk.modelbase.BaseResp
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler
import io.github.devzwy.socialhelper.utils.*


/**
 * 客户端继承自该类即可
 */
open class WeChatSocialPayEntryActivity : Activity(), IWXAPIEventHandler {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        "${localClassName}->onCreate():mIWXAPI:$mIWXAPI".logD()
        mIWXAPI.handleIntent(intent, this)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        "${localClassName}->onNewIntent():mIWXAPI:$mIWXAPI".logD()
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
            mWeChatReqPaySuccessListener()
        }.otherwise {
            mWeChatReqPayErrorListener(
                if (mBaseResp.errCode == BaseResp.ErrCode.ERR_USER_CANCEL) application.getString(
                    R.string.social_wechat_pay_cancel_err
                ) else (if (mBaseResp.errStr.isNullOrEmpty()) getString(
                    R.string.social_wechat_pay_other_err,
                    mBaseResp.errCode
                ) else mBaseResp.errStr)
            )
            "${localClassName}->onResp():mBaseResp:${mBaseResp.toJsonStr()}".logE()
        }

        finish()

    }
}