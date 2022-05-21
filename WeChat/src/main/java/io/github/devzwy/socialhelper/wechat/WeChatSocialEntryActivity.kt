package io.github.devzwy.socialhelper.wechat

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.tencent.mm.opensdk.modelbase.BaseReq
import com.tencent.mm.opensdk.modelbase.BaseResp
import com.tencent.mm.opensdk.modelmsg.SendAuth
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler
import io.github.devzwy.socialhelper.SocialHelper
import io.github.devzwy.socialhelper.utils.*
import io.github.devzwy.socialhelper.wechat.data.WeChatConst.Companion.WECHAT_ACCESSTOKEN_REQUEST_URL
import io.github.devzwy.socialhelper.wechat.data.WeChatSocialAccessTokenData
import io.github.devzwy.socialhelper.wechat.data.WeChatSocialReqAuthRespData


/**
 * 客户端继承自该类即可
 */
open class WeChatSocialEntryActivity : Activity(), IWXAPIEventHandler {

    //微信回传结果的类型 登陆
    val WECHAT_RESULT_TYPE_LOGIN = 1

    //微信回传结果的类型 分享
    val WECHAT_RESULT_TYPE_SHARE = 2

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

            when (mBaseResp.type) {
                WECHAT_RESULT_TYPE_LOGIN -> {
                    //授权回调
                    onReqAuthSucc(mBaseResp as SendAuth.Resp)
                }

                WECHAT_RESULT_TYPE_SHARE -> {
                    //分享回调
                    mWeChatReqShareSuccessListener?.let { it() }
                }

                else -> {
                    "unknow result type from wechat,${mBaseResp.toJsonStr()}".logE()
                }
            }

        }.otherwise {

            "${localClassName}->onResp():mBaseResp:${mBaseResp.toJsonStr()}".logE()
            when (mBaseResp.type) {

                WECHAT_RESULT_TYPE_LOGIN -> {
                    //授权失败回调
                    mWeChatReqAuthErrorListener(
                        if (mBaseResp.errCode == BaseResp.ErrCode.ERR_USER_CANCEL) application.getString(
                            R.string.social_wechat_req_auth_cancel
                        ) else if (mBaseResp.errStr != null) mBaseResp.errStr else "${mBaseResp.errCode}"
                    )
                }

                WECHAT_RESULT_TYPE_SHARE -> {
                    //分享回调
                    mWeChatReqShareErrorListener?.let {
                        it(
                            if (!mBaseResp.errStr.isNullOrEmpty()) mBaseResp.errStr else application.getString(
                                R.string.social_wechat_share_err,
                                mBaseResp.errCode
                            )
                        )
                    }
                }

                else -> {
                    "unknow result type from wechat,${mBaseResp.toJsonStr()}".logE()
                }

            }

        }

        mWeChatReqShareErrorListener = null
        mWeChatReqShareSuccessListener = null

        finish()

    }

    /**
     * 处理授权成功业务
     */
    private fun onReqAuthSucc(resp: SendAuth.Resp) {

        resp.toJsonStr().logD()
        //判断是否配置weChatAppSecretKey
        SocialHelper.socialConfig.weChatAppSecretKey.isEmpty().yes {
            //未配置时直接回传authCode即可
            mWeChatReqAuthSuccessListener(WeChatSocialReqAuthRespData(authCodeData = resp))
        }.otherwise {
            //查询accessToken  appid secret code
            SocialHelper.socialConfig.apply {
                SocialNetUtil.sendGet(
                    String.format(
                        WECHAT_ACCESSTOKEN_REQUEST_URL,
                        weChatAppId,
                        weChatAppSecretKey,
                        resp.code
                    ), {
                        it.toObject(WeChatSocialAccessTokenData::class.java)
                            .let { mWeChatAccessTokenData ->
                                (mWeChatAccessTokenData.errcode == 0).yes {
                                    mWeChatReqAuthSuccessListener(
                                        WeChatSocialReqAuthRespData(
                                            authCodeData = resp,
                                            socialAccessTokenData = mWeChatAccessTokenData
                                        )
                                    )
                                }.otherwise {
                                    mWeChatReqAuthErrorListener(
                                        application.getString(
                                            R.string.social_wechat_get_access_token_err,
                                            if (mWeChatAccessTokenData.errmsg.isEmpty()) "${mWeChatAccessTokenData.errcode}" else mWeChatAccessTokenData.errmsg
                                        )
                                    )
                                }
                            }
                    }, {
                        mWeChatReqAuthErrorListener(
                            application.getString(
                                R.string.social_wechat_get_access_token_err,
                                it
                            )
                        )
                    })
            }
        }
    }


}