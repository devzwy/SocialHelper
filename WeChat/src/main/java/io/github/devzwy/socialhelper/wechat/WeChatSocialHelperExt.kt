package io.github.devzwy.socialhelper.wechat

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import com.tencent.mm.opensdk.modelmsg.*
import com.tencent.mm.opensdk.modelpay.PayReq
import com.tencent.mm.opensdk.openapi.IWXAPI
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import io.github.devzwy.socialhelper.SocialHelper
import io.github.devzwy.socialhelper.utils.*
import io.github.devzwy.socialhelper.wechat.data.WeChatConst.Companion.REQ_AUTH_SCOPE
import io.github.devzwy.socialhelper.wechat.data.WeChatConst.Companion.WECHAT_USERINFO_URL
import io.github.devzwy.socialhelper.wechat.data.WeChatShareType
import io.github.devzwy.socialhelper.wechat.data.WeChatSocialReqAuthRespData
import io.github.devzwy.socialhelper.wechat.data.WeChatSocialUserInfoData
import java.io.ByteArrayOutputStream
import java.util.*
import kotlin.random.Random

//微信授权api
lateinit var mIWXAPI: IWXAPI

//微信授权结果回传接口
lateinit var mWeChatReqAuthSuccessListener: (WeChatSocialReqAuthRespData) -> Unit

//微信授权结果错误回传接口
lateinit var mWeChatReqAuthErrorListener: (String) -> Unit

//微信分享结果回传接口
var mWeChatReqShareSuccessListener: (() -> Unit)? = null

//微信分享结果错误回传接口
var mWeChatReqShareErrorListener: ((String) -> Unit)? = null

//微信支付结果回传接口
lateinit var mWeChatReqPaySuccessListener: () -> Unit

//微信支付结果错误回传接口
lateinit var mWeChatReqPayErrorListener: (String) -> Unit

//是否初始化微信SDK的标识
var isInitWeChatApi = false

/**
 * 发起微信授权 如果配置了appSecretKey则会返回 authCode + accessToken，否则将只返回 authCode
 *  [extData] 微信授权的扩展字段 用于保持请求和回调的状态，授权请求后原样带回给第三方。该参数可用于防止 csrf 攻击（跨站请求伪造攻击），建议第三方带上该参数，可设置为简单的随机数加 session 进行校验。在state传递的过程中会将该参数作为url的一部分进行处理，因此建议对该参数进行url encode操作，防止其中含有影响url解析的特殊字符（如'#'、'&'等）导致该参数无法正确回传。
 */
fun SocialHelper.reqWeChatAuth(
    extData: String = "",
    onWeChatReqAuthSuccess: (WeChatSocialReqAuthRespData) -> Unit,
    onWeChatReqAuthError: (String) -> Unit
) {

    regWeChatSDK(this)

    mIWXAPI.isWXAppInstalled.no {
        onWeChatReqAuthError(socialConfig.application.getString(R.string.social_not_install_wechat_app_tip))
        return
    }

    mIWXAPI.sendReq(SendAuth.Req().also {
        it.scope = REQ_AUTH_SCOPE
        extData.isEmpty().no {
            it.extData = extData
        }
        mWeChatReqAuthSuccessListener = onWeChatReqAuthSuccess
        mWeChatReqAuthErrorListener = onWeChatReqAuthError
    })

}

/**
 * 获取微信用户资料 通过[reqWeChatAuth]获得accessToken后调用
 * [accessToken] 通过[reqWeChatAuth]获得[WeChatSocialAccessTokenData.access_token]传入
 * [openId]  通过[reqWeChatAuth]获得[WeChatSocialAccessTokenData.openid]传入
 */
fun SocialHelper.getWeChatUserInfo(
    accessToken: String,
    openId: String = "",
    onGetUserInfoSuccess: (WeChatSocialUserInfoData) -> Unit,
    onGetUserInfoError: (String) -> Unit
) {
    SocialNetUtil.sendGet(String.format(WECHAT_USERINFO_URL, accessToken, openId), {
        it.toObject(WeChatSocialUserInfoData::class.java).let { resultUserInfo ->
            (resultUserInfo.errcode == 0).yes {
                onGetUserInfoSuccess(resultUserInfo)
            }.otherwise {
                onGetUserInfoError(resultUserInfo.errmsg.ifEmpty {
                    socialConfig.application.getString(
                        R.string.social_wechat_get_user_info_err,
                        resultUserInfo.errcode
                    )
                })
            }
        }
    }, {
        it.logE()
    })
}

/**
 * 分享文本到微信平台
 * [weChatShareType] 分享平台 [WeChatShareType.SCENE_SESSION]、[WeChatShareType.SCENE_TIMELINE]、[WeChatShareType.SCENE_FAVORITE] 分别为会话、朋友圈、收藏
 * [text] 文本内容
 * [description] 描述 可选
 * [onShareSuccess] 分享成功的回调 可选，不一定会回调，当用户分享到微信后选择停留在微信页面，不返回App时回调将丢失，官方就这样。看看后面人家给不给放出来这个回调，估计也是官方怕引流问题。
 * [onShareError] 分享失败的回调 可选，不一定会回调，包括用户取消也不会进，除非其他官方的未知错误。参考成功的回调
 */
fun SocialHelper.shareTextToWeChat(
    weChatShareType: WeChatShareType,
    text: String,
    description: String,
    onShareSuccess: (() -> Unit)? = null,
    onShareError: ((String) -> Unit)? = null
) {

    mWeChatReqShareSuccessListener = onShareSuccess
    mWeChatReqShareErrorListener = onShareError

    regWeChatSDK(this)

    mIWXAPI.isWXAppInstalled.no {
        if (onShareError != null) {
            onShareError(socialConfig.application.getString(R.string.social_not_install_wechat_app_tip))
        }
        return
    }

    mIWXAPI.sendReq(SendMessageToWX.Req().apply {
        transaction = buildTransaction("text")
        message = WXMediaMessage().also { mWXMediaMessage ->
            mWXMediaMessage.mediaObject = WXTextObject().also {
                it.text = text
            }
            mWXMediaMessage.description = description
        }
        scene = getShareScene(weChatShareType)
    })
}

/**
 * 分享图片到微信平台
 * [weChatShareType] 分享平台 [WeChatShareType.SCENE_SESSION]、[WeChatShareType.SCENE_TIMELINE]、[WeChatShareType.SCENE_FAVORITE] 分别为会话、朋友圈、收藏
 * [imageBitmap] 图片bitmap 内部会做 recycle
 * [onShareSuccess] 分享成功的回调 可选，不一定会回调，当用户分享到微信后选择停留在微信页面，不返回App时回调将丢失，官方就这样。看看后面人家给不给放出来这个回调，估计也是官方怕引流问题。
 * [onShareError] 分享失败的回调 可选，不一定会回调，包括用户取消也不会进，除非其他官方的未知错误。参考成功的回调
 */
fun SocialHelper.shareImageToWeChat(
    weChatShareType: WeChatShareType,
    imageBitmap: Bitmap,
    onShareSuccess: (() -> Unit)? = null,
    onShareError: ((String) -> Unit)? = null
) {

    mWeChatReqShareSuccessListener = onShareSuccess
    mWeChatReqShareErrorListener = onShareError

    regWeChatSDK(this)

    mIWXAPI.isWXAppInstalled.no {
        if (onShareError != null) {
            onShareError(socialConfig.application.getString(R.string.social_not_install_wechat_app_tip))
        }
        return
    }

    val thumbBitmap = Bitmap.createScaledBitmap(
        imageBitmap,
        150,
        150,
        true
    )

    mIWXAPI.sendReq(SendMessageToWX.Req().apply {

        transaction = buildTransaction("img")
        message = WXMediaMessage().also { mWXMediaMessage ->
            mWXMediaMessage.mediaObject = WXImageObject(imageBitmap)
            mWXMediaMessage.thumbData = bitmap2ByteArray(thumbBitmap)
        }

        scene = getShareScene(weChatShareType)
        if (!imageBitmap.isRecycled) imageBitmap.recycle()
        if (thumbBitmap != null && !thumbBitmap.isRecycled) thumbBitmap.recycle()

    })
}

/**
 * 分享音乐到微信平台
 * [weChatShareType] 分享平台 [WeChatShareType.SCENE_SESSION]、[WeChatShareType.SCENE_TIMELINE]、[WeChatShareType.SCENE_FAVORITE] 分别为会话、朋友圈、收藏
 * [musicUrl] 音乐url
 * [musicTitle] 音乐标题 可选
 * [musicDescription] 音乐描述 可选
 * [thumbBitmap] 音乐缩略图 可选  传入时内部会做 recycle
 * [onShareSuccess] 分享成功的回调 可选，不一定会回调，当用户分享到微信后选择停留在微信页面，不返回App时回调将丢失，官方就这样。看看后面人家给不给放出来这个回调，估计也是官方怕引流问题。
 * [onShareError] 分享失败的回调 可选，不一定会回调，包括用户取消也不会进，除非其他官方的未知错误。参考成功的回调
 */
fun SocialHelper.shareMusicToWeChat(
    weChatShareType: WeChatShareType,
    musicUrl: String,
    musicTitle: String,
    musicDescription: String? = null,
    thumbBitmap: Bitmap? = null,
    onShareSuccess: (() -> Unit)? = null,
    onShareError: ((String) -> Unit)? = null
) {

    mWeChatReqShareSuccessListener = onShareSuccess
    mWeChatReqShareErrorListener = onShareError

    regWeChatSDK(this)

    mIWXAPI.isWXAppInstalled.no {
        if (onShareError != null) {
            onShareError(socialConfig.application.getString(R.string.social_not_install_wechat_app_tip))
        }
        return
    }

    mIWXAPI.sendReq(SendMessageToWX.Req().apply {

        transaction = buildTransaction("music")
        message = WXMediaMessage().also { mWXMediaMessage ->
            mWXMediaMessage.mediaObject = WXMusicObject().also {
                it.musicUrl = musicUrl
            }

            mWXMediaMessage.title = musicTitle
            mWXMediaMessage.description = musicDescription

            (thumbBitmap != null).yes {
                mWXMediaMessage.thumbData = bitmap2ByteArray(
                    Bitmap.createScaledBitmap(
                        thumbBitmap!!,
                        150,
                        150,
                        true
                    )
                )
            }
        }

        scene = getShareScene(weChatShareType)
        if (thumbBitmap != null && !thumbBitmap.isRecycled) thumbBitmap.recycle()

    })
}


/**
 * 分享视频到微信平台
 * [weChatShareType] 分享平台 [WeChatShareType.SCENE_SESSION]、[WeChatShareType.SCENE_TIMELINE]、[WeChatShareType.SCENE_FAVORITE] 分别为会话、朋友圈、收藏
 * [videoUrl] 视频url
 * [videoTitle] 视频标题 可选
 * [videoDescription] 视频描述 可选
 * [thumbBitmap] 视频缩略图 可选  传入时内部会做 recycle
 * [onShareSuccess] 分享成功的回调 可选，不一定会回调，当用户分享到微信后选择停留在微信页面，不返回App时回调将丢失，官方就这样。看看后面人家给不给放出来这个回调，估计也是官方怕引流问题。
 * [onShareError] 分享失败的回调 可选，不一定会回调，包括用户取消也不会进，除非其他官方的未知错误。参考成功的回调
 */
fun SocialHelper.shareVideoToWeChat(
    weChatShareType: WeChatShareType,
    videoUrl: String,
    videoTitle: String? = null,
    videoDescription: String? = null,
    thumbBitmap: Bitmap? = null,
    onShareSuccess: (() -> Unit)? = null,
    onShareError: ((String) -> Unit)? = null
) {

    mWeChatReqShareSuccessListener = onShareSuccess
    mWeChatReqShareErrorListener = onShareError

    regWeChatSDK(this)

    mIWXAPI.isWXAppInstalled.no {
        if (onShareError != null) {
            onShareError(socialConfig.application.getString(R.string.social_not_install_wechat_app_tip))
        }
        return
    }

    mIWXAPI.sendReq(SendMessageToWX.Req().apply {

        transaction = buildTransaction("video")
        message = WXMediaMessage().also { mWXMediaMessage ->
            mWXMediaMessage.mediaObject = WXVideoObject().also {
                it.videoUrl = videoUrl
            }

            mWXMediaMessage.title = videoTitle
            mWXMediaMessage.description = videoDescription

            (thumbBitmap != null).yes {
                mWXMediaMessage.thumbData = bitmap2ByteArray(
                    Bitmap.createScaledBitmap(
                        thumbBitmap!!,
                        150,
                        150,
                        true
                    )
                )
            }
        }

        scene = getShareScene(weChatShareType)
        if (thumbBitmap != null && !thumbBitmap.isRecycled) thumbBitmap.recycle()

    })
}


/**
 * 分享网页到微信平台
 * [weChatShareType] 分享平台 [WeChatShareType.SCENE_SESSION]、[WeChatShareType.SCENE_TIMELINE]、[WeChatShareType.SCENE_FAVORITE] 分别为会话、朋友圈、收藏
 * [webpageUrl] 网页url
 * [webpageTitle] 网页标题 可选
 * [webpageDescription] 网页描述 可选
 * [thumbBitmap] 网页缩略图 可选  传入时内部会做 recycle
 * [onShareSuccess] 分享成功的回调 可选，不一定会回调，当用户分享到微信后选择停留在微信页面，不返回App时回调将丢失，官方就这样。看看后面人家给不给放出来这个回调，估计也是官方怕引流问题。
 * [onShareError] 分享失败的回调 可选，不一定会回调，包括用户取消也不会进，除非其他官方的未知错误。参考成功的回调
 */
fun SocialHelper.shareWebPageToWeChat(
    weChatShareType: WeChatShareType,
    webpageUrl: String,
    webpageTitle: String? = null,
    webpageDescription: String,
    thumbBitmap: Bitmap? = null,
    onShareSuccess: (() -> Unit)? = null,
    onShareError: ((String) -> Unit)? = null
) {

    mWeChatReqShareSuccessListener = onShareSuccess
    mWeChatReqShareErrorListener = onShareError

    regWeChatSDK(this)

    mIWXAPI.isWXAppInstalled.no {
        if (onShareError != null) {
            onShareError(socialConfig.application.getString(R.string.social_not_install_wechat_app_tip))
        }
        return
    }

    mIWXAPI.sendReq(SendMessageToWX.Req().apply {

        transaction = buildTransaction("webpage")
        message = WXMediaMessage().also { mWXMediaMessage ->
            mWXMediaMessage.mediaObject = WXWebpageObject().also {
                it.webpageUrl = webpageUrl
            }

            mWXMediaMessage.title = webpageTitle
            mWXMediaMessage.description = webpageDescription

            (thumbBitmap != null).yes {
                mWXMediaMessage.thumbData = bitmap2ByteArray(
                    Bitmap.createScaledBitmap(
                        thumbBitmap!!,
                        150,
                        150,
                        true
                    )
                )
            }
        }
        scene = getShareScene(weChatShareType)
        if (thumbBitmap != null && !thumbBitmap.isRecycled) thumbBitmap.recycle()
    })
}


/**
 * 分享微信小程序到微信平台
 * [weChatShareType] 分享平台 [WeChatShareType.SCENE_SESSION]、[WeChatShareType.SCENE_TIMELINE]、[WeChatShareType.SCENE_FAVORITE] 分别为会话、朋友圈、收藏
 * [webpageUrl] 网页链接
 * [miniprogramType] 小程序版本 0 1 2 分别代表 正式版 测试版 体验版 默认0 [com.tencent.mm.opensdk.modelmsg.WXMiniProgramObject.MINIPTOGRAM_TYPE_RELEASE]
 * [miniProgramRealId] 小程序的原始 id 获取方法：登录小程序管理后台-设置-基本设置-帐号信息
 * [miniprogramPath] 小程序的 path 小程序页面路径；对于小游戏，可以只传入 query 部分，来实现传参效果，如：传入 "?foo=bar"
 * [withShareTicket] 是否使用带 shareTicket 的分享 [https://developers.weixin.qq.com/doc/oplatform/Mobile_App/Share_and_Favorites/Android.html]
 * [webpageTitle] 小程序消息标题
 * [webpageDescription] 小程序消息描述
 * [thumbBitmap] 小程序消息缩略图  为空时不显示图片
 * [onShareSuccess] 分享成功的回调 可选，不一定会回调，当用户分享到微信后选择停留在微信页面，不返回App时回调将丢失，官方就这样。看看后面人家给不给放出来这个回调，估计也是官方怕引流问题。
 * [onShareError] 分享失败的回调 可选，不一定会回调，包括用户取消也不会进，除非其他官方的未知错误。参考成功的回调
 */
fun SocialHelper.shareMiniProgramToWeChat(
    weChatShareType: WeChatShareType,
    webpageUrl: String,
    miniprogramType: Int = 0,
    miniProgramRealId: String,
    miniprogramPath: String,
    withShareTicket: Boolean = false,
    webpageTitle: String? = null,
    webpageDescription: String? = null,
    thumbBitmap: Bitmap,
    onShareSuccess: (() -> Unit)? = null,
    onShareError: ((String) -> Unit)? = null
) {

    mWeChatReqShareSuccessListener = onShareSuccess
    mWeChatReqShareErrorListener = onShareError

    regWeChatSDK(this)

    mIWXAPI.isWXAppInstalled.no {
        if (onShareError != null) {
            onShareError(socialConfig.application.getString(R.string.social_not_install_wechat_app_tip))
        }
        return
    }

    mIWXAPI.sendReq(SendMessageToWX.Req().apply {

        transaction = buildTransaction("miniProgram")

        message = WXMediaMessage().also { mWXMediaMessage ->

            mWXMediaMessage.mediaObject = WXMiniProgramObject().also {
                it.webpageUrl = webpageUrl
                it.miniprogramType = miniprogramType// 正式版:0，测试版:1，体验版:2

                it.userName = miniProgramRealId //"gh_d43f693ca31f" // 小程序原始id

                it.path =
                    miniprogramPath // "/pages/media" //小程序页面路径；对于小游戏，可以只传入 query 部分，来实现传参效果，如：传入 "?foo=bar"

                it.withShareTicket = withShareTicket
            }

            mWXMediaMessage.title = webpageTitle
            mWXMediaMessage.description = webpageDescription
            mWXMediaMessage.thumbData = bitmap2ByteArray(
                Bitmap.createScaledBitmap(
                    thumbBitmap,
                    150,
                    150,
                    true
                )
            )
        }

        scene = getShareScene(weChatShareType)
        if (thumbBitmap.isRecycled) thumbBitmap.recycle()
    })
}


/**
 * 发起微信支付
 * [partnerId] 商户号 请填写商户号mchid对应的值。示例值：1900000109
 * [prepayid] 预支付交易会话ID 微信返回的支付交易会话ID，该值有效期为2小时。 示例值： WX1217752501201407033233368018
 * [sign] 签名
 */
fun SocialHelper.startWeChatPay(
    partnerId: String,
    prepayId: String,
    sign: String,
    onPaySuccess: () -> Unit,
    onPayError: (String) -> Unit
) {

    regWeChatSDK(this)

    mIWXAPI.sendReq(PayReq().also {
        it.appId = socialConfig.weChatAppId
        it.partnerId = partnerId
        it.prepayId = prepayId
        it.packageValue = "Sign=WXPay"
        it.nonceStr = getRandomWeChatString()
        it.timeStamp = "${System.currentTimeMillis() / 1000}"
        it.sign = sign
    })

}

/**
 * 生成随机数
 */
private fun getRandomWeChatString(): String {
    val s = arrayOf(
        "a",
        "b",
        "c",
        "d",
        "e",
        "f",
        "g",
        "h",
        "i",
        "j",
        "k",
        "l",
        "m",
        "n",
        "o",
        "p",
        "q",
        "r",
        "s",
        "t",
        "u",
        "v",
        "w",
        "x",
        "y",
        "z"
    )

    val ca = (System.currentTimeMillis() / 10).toString().toCharArray()

    val ac = (System.currentTimeMillis() / 10).toString().toCharArray().reversed()

    val randomStr = StringBuffer()

    for (c in ca) {
        randomStr.append(s[Random.nextInt(0, s.size - 1)]).append(c)
            .append(s[Random.nextInt(0, s.size - 1)])
    }
    for (c2 in ac) {
        randomStr.append(s[Random.nextInt(0, s.size - 1)]).append(c2)
            .append(s[Random.nextInt(0, s.size - 1)])
    }

    return (if (randomStr.toString().length > 30) randomStr.toString()
        .uppercase(Locale.getDefault())
        .substring(0, 30) else randomStr.toString().uppercase(Locale.getDefault()))

}

/**
 * 注册微信SDK
 */
private fun regWeChatSDK(mSocialHelper: SocialHelper) {
    if (!isInitWeChatApi) {
        mSocialHelper.socialConfig.apply {
            mIWXAPI = WXAPIFactory.createWXAPI(application, weChatAppId, false)
            mIWXAPI.registerApp(weChatAppId)
        }
    }
}

/**
 * bitmap转bytearray
 */
fun bitmap2ByteArray(bmp: Bitmap): ByteArray? {
    var i: Int
    var j: Int
    if (bmp.height > bmp.width) {
        i = bmp.width
        j = bmp.width
    } else {
        i = bmp.height
        j = bmp.height
    }
    val localBitmap = Bitmap.createBitmap(i, j, Bitmap.Config.RGB_565)
    val localCanvas = Canvas(localBitmap)
    while (true) {
        localCanvas.drawBitmap(bmp, Rect(0, 0, i, j), Rect(0, 0, i, j), null)
        val localByteArrayOutputStream = ByteArrayOutputStream()
        localBitmap.compress(
            Bitmap.CompressFormat.JPEG, 100,
            localByteArrayOutputStream
        )
        localBitmap.recycle()
        val arrayOfByte = localByteArrayOutputStream.toByteArray()
        try {
            localByteArrayOutputStream.close()
            return arrayOfByte
        } catch (e: Exception) {
            //F.out(e);
        }
        i = bmp.height
        j = bmp.height
    }
}