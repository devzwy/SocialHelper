package it.github.socialhelper.alipay

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.alipay.share.sdk.openapi.BaseReq
import com.alipay.share.sdk.openapi.BaseResp
import com.alipay.share.sdk.openapi.IAPAPIEventHandler
import io.github.social.utils.logD
import io.github.social.utils.toJsonStr

open class AlipaySocialEntryActivity : Activity(), IAPAPIEventHandler {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mIAPApi.handleIntent(intent, this)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        mIAPApi.handleIntent(intent, this)
    }

    override fun onReq(p0: BaseReq) {
    }

    override fun onResp(p0: BaseResp) {
        if (p0.errCode == 0) {
            mAliPayReqShareSuccessListener?.let { it() }
        } else {
            mAliPayReqShareErrorListener?.let { it(p0.errStr) }
        }

        finish()
    }
}