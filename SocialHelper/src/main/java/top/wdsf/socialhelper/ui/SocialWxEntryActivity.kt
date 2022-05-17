package top.wdsf.socialhelper.ui

import android.app.Activity
import android.os.Bundle
import com.tencent.mm.opensdk.modelbase.BaseReq
import com.tencent.mm.opensdk.modelbase.BaseResp
import com.tencent.mm.opensdk.modelmsg.*
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import top.wdsf.socialhelper.SocialHelper
import top.wdsf.socialhelper.data.Constant
import top.wdsf.socialhelper.data.Platform
import top.wdsf.socialhelper.data.TokenFrom
import top.wdsf.socialhelper.data.wxapi_resp.WeChatAuth
import top.wdsf.socialhelper.listeners.OnSocialAuthListener
import top.wdsf.socialhelper.listeners.OnSocialShareListener
import top.wdsf.socialhelper.utils.Log
import top.wdsf.socialhelper.utils.NetUtil
import top.wdsf.socialhelper.utils.SpUtils
import top.wdsf.socialhelper.utils.toJsonStr


/**
 * 客户端继承自该类即可
 */
open class SocialWxEntryActivity : Activity(), IWXAPIEventHandler {

    //授权结果回调
    private var onSocialAuthListener: OnSocialAuthListener<WeChatAuth>? = null

    //分享结果回调
    private var onSocialShareListener:OnSocialShareListener?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onSocialAuthListener = SocialHelper.getSocialLoginListener()
        onSocialShareListener = SocialHelper.getSocialShareListener()
        Log.debug("SocialWxEntryActivity -> onCreate")
        SocialHelper.mSocialConfig.weChatApi?.handleIntent(intent, this)

    }

    override fun onResume() {
        super.onResume()
        Log.debug("SocialWxEntryActivity -> onResume")
    }

    override fun onPause() {
        super.onPause()
        Log.debug("SocialWxEntryActivity -> onPause")
    }

    override fun onStop() {
        super.onStop()
        Log.debug("SocialWxEntryActivity -> onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.debug("SocialWxEntryActivity -> onDestroy")
    }

    override fun onReq(mBaseReq: BaseReq) {
        Log.debug("微信(onReReq)：${mBaseReq.toJsonStr()}")
        if (Constant.WECHAT_RESULT_TYPE_SHARE == mBaseReq.type) {
            (mBaseReq as? ShowMessageFromWX.Req)?.let {
                val wxMsg: WXMediaMessage = it.message

                (wxMsg.mediaObject as? WXTextObject)?.let { wxTextObject ->
                    Log.debug(wxTextObject.toJsonStr())
                }

            }
        }

    }

    /**
     * 第三方应用发送到微信的请求处理后的响应结果，会回调到该方法
     */
    override fun onResp(mBaseResp: BaseResp) {

        when (mBaseResp.errCode) {

            BaseResp.ErrCode.ERR_OK -> {
                Log.debug("微信(onResp)：${mBaseResp.toJsonStr()}")

                if (Constant.WECHAT_RESULT_TYPE_LOGIN == mBaseResp.type) {
                    //授权登陆的回传
                    loginResult(mBaseResp = mBaseResp)

                } else if (Constant.WECHAT_RESULT_TYPE_SHARE == mBaseResp.type) {
                    //分享的回传
                    onSocialShareListener?.onShareSuccess(Platform.WECHAT)
                }

            }

            BaseResp.ErrCode.ERR_AUTH_DENIED, BaseResp.ErrCode.ERR_USER_CANCEL -> {
                Log.error("微信(onResp)：${mBaseResp.toJsonStr()}")

                if (Constant.WECHAT_RESULT_TYPE_LOGIN == mBaseResp.type) {
                    //授权登陆的回传
                    CoroutineScope(Dispatchers.Main).launch {
                        onSocialAuthListener?.onSocialAuthCancel(Platform.WECHAT)
                    }
                } else if (Constant.WECHAT_RESULT_TYPE_SHARE == mBaseResp.type) {
                    //分享的回传
                    onSocialShareListener?.onShareError(Platform.WECHAT,"取消分享")
                }


            }

            else -> {
                Log.error("微信(onResp)：${mBaseResp.toJsonStr()}")
                if (Constant.WECHAT_RESULT_TYPE_LOGIN == mBaseResp.type) {
                    //授权登陆的回传
                    CoroutineScope(Dispatchers.Main).launch {
                        onSocialAuthListener?.onSocialAuthError(
                            Platform.WECHAT,
                            "${mBaseResp.errStr}(${mBaseResp.errCode})"
                        )
                    }

                } else if (Constant.WECHAT_RESULT_TYPE_SHARE == mBaseResp.type) {
                    //分享的回传
                    onSocialShareListener?.onShareError(Platform.WECHAT,"${mBaseResp.errStr}(${mBaseResp.errCode})")
                }

            }
        }

        finish()

    }

    /**
     * 微信授权登陆回传
     */
    private fun loginResult(mBaseResp: BaseResp) {
        val resp = mBaseResp as? SendAuth.Resp
        if (resp == null) {

            CoroutineScope(Dispatchers.Main).launch {
                onSocialAuthListener?.onSocialAuthError(
                    Platform.WECHAT,
                    "内部异常，无法将BaseResp转换为SendAuth.Resp对象！"
                )
            }
        } else {
            if (SocialHelper.mSocialConfig.weChatAppSecretKey.isNullOrEmpty()) {
                Log.error("未配置appSecretKey,跳过获取accestoken,请使用返回的authCode自行获取!")
                CoroutineScope(Dispatchers.Main).launch {
                    onSocialAuthListener?.onSocialAuthSuccess(
                        Platform.WECHAT,
                        WeChatAuth(
                            TokenFrom.NETWORK,
                            resp,
                            null
                        )
                    )
                }
            } else {
                Log.debug("authCode获取成功")
                getAccessToken(resp)
            }

        }
    }

    private fun getAccessToken(wxAuthRespBean: SendAuth.Resp) {
        //读取本地token是否存在并有效
        val localToken = SpUtils.getAccessToken()

        if (localToken != null && !SocialHelper.mSocialConfig.forceNetwork) {
            //本地已经有token 查询该token是否有效
            NetUtil.authWxAccessToken(localToken.access_token, localToken.openid, {
                Log.debug("本地accessToken有效，直接回调！")
                onSocialAuthListener?.onSocialAuthSuccess(
                    Platform.WECHAT,
                    WeChatAuth(
                        TokenFrom.LOCAL,
                        wxAuthRespBean = wxAuthRespBean,
                        weChatAccessTokenResultData = localToken
                    )
                )
            }, {
                Log.error("本地accessToken已失效，开始刷新！")
                NetUtil.refreshWxAccessToken(
                    SocialHelper.mSocialConfig.weChatAppId!!,
                    localToken.refresh_token,
                    {
                        Log.debug("刷新AccessToken成功!")
                        onSocialAuthListener?.onSocialAuthSuccess(
                            Platform.WECHAT,
                            WeChatAuth(
                                TokenFrom.NETWORK,
                                wxAuthRespBean = wxAuthRespBean,
                                weChatAccessTokenResultData = SpUtils.getAccessToken()
                            )
                        )
                    },
                    {
                        Log.error("accessToken刷新失败[${it}]，尝试重新获取！")
                        SpUtils.clearAccessToken()
                        getAccessToken(wxAuthRespBean)
                    })
            })

        } else {
            NetUtil.getWxAccessToken(wxAuthRespBean.code, {
                Log.debug("获取AccessToken成功")
                onSocialAuthListener?.onSocialAuthSuccess(
                    Platform.WECHAT,
                    WeChatAuth(
                        TokenFrom.NETWORK,
                        wxAuthRespBean = wxAuthRespBean,
                        weChatAccessTokenResultData = it
                    )
                )
            }, {
                Log.error("获取AccessToken失败，${it}")
                Log.error(it)
                onSocialAuthListener?.onSocialAuthError(Platform.WECHAT, it)
            })
        }
    }

}