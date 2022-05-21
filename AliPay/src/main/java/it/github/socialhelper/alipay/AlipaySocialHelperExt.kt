package it.github.socialhelper.alipay

import android.app.Activity
import com.alipay.sdk.app.AuthTask
import com.alipay.share.sdk.openapi.*
import io.github.social.SocialConfig
import io.github.social.SocialHelper
import io.github.social.utils.otherwise
import io.github.social.utils.toJsonStr
import io.github.social.utils.toObject
import io.github.social.utils.yes
import it.github.socialhelper.alipay.data.AlipayResult
import it.github.socialhelper.alipay.data.AlipayResultSubData
import it.github.socialhelper.alipay.util.OrderInfoUtil2_0
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URLDecoder
import kotlin.random.Random


lateinit var mIAPApi: IAPApi
var isInitApi = false

//支付宝分享结果回传接口
var mAliPayReqShareSuccessListener: (() -> Unit)? = null

//支付宝分享结果错误回传接口
var mAliPayReqShareErrorListener: ((String) -> Unit)? = null


/**
 * 分享文本到支付宝
 * [text] 文本内容
 * [onShareError] 分享失败的回调
 * [onShareSuccess] 分享成功的回调
 */
fun SocialHelper.shareTextToAlipay(
    text: String,
    onShareSuccess: (() -> Unit)? = null,
    onShareError: ((String) -> Unit)? = null
) {

    mAliPayReqShareSuccessListener = onShareSuccess
    mAliPayReqShareErrorListener = onShareError


    socialConfig.run {

        checkAlipayApi(this)

        if (!mIAPApi.isZFBAppInstalled) {
            mAliPayReqShareErrorListener?.let { it(this.application.getString(R.string.social_not_install_alipay_app_tip)) }
        } else {
            //组装文本消息内容对象
            val textObject = APTextObject()
            textObject.text = text
            //组装分享消息对象
            val mediaMessage = APMediaMessage()
            mediaMessage.mediaObject = textObject
            //将分享消息对象包装成请求对象
            val req = SendMessageToZFB.Req()
            req.message = mediaMessage
            //发送请求
            mIAPApi.sendReq(req)
        }
    }

}

/**
 * 分享网页到支付宝
 * [pageUrl] 网页url
 * [title] 标题
 * [description] 内容描述
 * [thumbUrl] 缩略图URL 可选
 * [onShareError] 分享失败的回调
 * [onShareSuccess] 分享成功的回调
 */
fun SocialHelper.shareWebPageToAlipay(
    pageUrl: String,
    title: String,
    description: String,
    thumbUrl: String? = null,
    onShareSuccess: (() -> Unit)? = null,
    onShareError: ((String) -> Unit)? = null
) {

    mAliPayReqShareSuccessListener = onShareSuccess
    mAliPayReqShareErrorListener = onShareError


    socialConfig.run {

        checkAlipayApi(this)

        if (!mIAPApi.isZFBAppInstalled) {
            mAliPayReqShareErrorListener?.let { it(this.application.getString(R.string.social_not_install_alipay_app_tip)) }
        } else {
            val webPageObject = APWebPageObject()
            webPageObject.webpageUrl = pageUrl
            val webMessage = APMediaMessage()
            webMessage.mediaObject = webPageObject
            webMessage.title = title
            webMessage.description = description
            webMessage.thumbUrl = thumbUrl
            val webReq = SendMessageToZFB.Req()
            webReq.message = webMessage
            webReq.transaction = "WebShare" + System.currentTimeMillis().toString()
            mIAPApi.sendReq(webReq)
        }
    }

}


/**
 * 分享图片到支付宝
 * [imageUrl] 图片地址
 * [onShareError] 分享失败的回调
 * [onShareSuccess] 分享成功的回调
 */
fun SocialHelper.shareImageToAlipay(
    imageUrl: String,
    onShareSuccess: (() -> Unit)? = null,
    onShareError: ((String) -> Unit)? = null
) {

    mAliPayReqShareSuccessListener = onShareSuccess
    mAliPayReqShareErrorListener = onShareError


    socialConfig.run {

        checkAlipayApi(this)

        if (!mIAPApi.isZFBAppInstalled) {
            mAliPayReqShareErrorListener?.let { it(this.application.getString(R.string.social_not_install_alipay_app_tip)) }
        } else {
            val imageObject = APImageObject()
            imageObject.imageUrl = imageUrl
            val mediaMessage = APMediaMessage()
            mediaMessage.mediaObject = imageObject
            val req = SendMessageToZFB.Req()
            req.message = mediaMessage
            req.transaction = "ImageShare" + System.currentTimeMillis().toString()
            mIAPApi.sendReq(req)
        }
    }

}

private fun checkAlipayApi(socialConfig: SocialConfig) {
    if (!isInitApi) {
        mIAPApi =
            APAPIFactory.createZFBApi(socialConfig.application, socialConfig.alipayAppId, false)
        isInitApi = true
    }
}

/**
 * 发起支付宝授权
 * [activity] 支付宝内部需要
 * [showLoadingDialog] 是否显示loading
 * [onError] 授权失败的回调
 * [onSuccess] 授权成功的回调 成功时会回传[AlipayResultSubData]
 */
fun SocialHelper.reqAliPayAuth(
    activity: Activity,
    showLoadingDialog: Boolean = true,
    onError: (String) -> Unit,
    onSuccess: (AlipayResultSubData) -> Unit
) {

    checkAlipayApi(socialConfig)

    (mIAPApi.isZFBAppInstalled).yes {
        socialConfig.apply {

            CoroutineScope(Dispatchers.IO).launch {
                OrderInfoUtil2_0.buildAuthInfoMap(
                    alipayPid, alipayAppId,
                    "social_${System.currentTimeMillis() + Random.nextInt(1, 999999)}", true
                ).let { authInfoMap ->

                    AuthTask(activity).let { authTask ->
                        authTask.authV2(
                            "${OrderInfoUtil2_0.buildOrderParam(authInfoMap)}&${
                                OrderInfoUtil2_0.getSign(
                                    authInfoMap,
                                    alipayPrivateKey,
                                    true
                                )
                            }", showLoadingDialog
                        ).toJsonStr().toObject(AlipayResult::class.java).let {

                            if (it.resultStatus == 9000) {
                                //授权成功
                                pareParam(URLDecoder.decode(it.result)).let {
                                    if (it.success && it.result_code == 200) {
                                        withContext(Dispatchers.Main) {
                                            onSuccess(it)
                                        }
                                    } else {
                                        withContext(Dispatchers.Main) {
                                            onError(application.getString(R.string.social_auth_code_null))
                                        }
                                    }
                                }
                            } else {
                                withContext(Dispatchers.Main) {
                                    //授权失败
                                    onError(if (it.memo.isEmpty()) application.getString(R.string.social_auth_fail) else it.memo)
                                }
                            }

                        }
                    }
                }
            }

        }
    }.otherwise {
        //支付宝未安装
        onError(socialConfig.application.getString(R.string.social_not_install_alipay_app_tip))
    }


}

fun pareParam(decode: String): AlipayResultSubData {
    val resultMap = hashMapOf<String, String>()
    decode.split("&").forEach {
        if (it.isNotEmpty()) {
            it.split("=").let {
                resultMap.put(it[0], it[1])
            }
        }
    }
    return resultMap.toJsonStr().toObject(AlipayResultSubData::class.java)
}
