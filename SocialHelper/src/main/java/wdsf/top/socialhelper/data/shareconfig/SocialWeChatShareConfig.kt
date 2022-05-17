package wdsf.top.socialhelper.data.shareconfig

import android.graphics.Bitmap
import com.tencent.mm.opensdk.modelmsg.*
import wdsf.top.socialhelper.data.SocialShareMediaType
import wdsf.top.socialhelper.data.SocialShareToType
import wdsf.top.socialhelper.data.base.SocialShareConfig
import wdsf.top.socialhelper.utils.SocialUtils


/**
 * 分享到微信时使用的实体
 */
class SocialWeChatShareConfig private constructor(
    socialShareMediaType: SocialShareMediaType,
    socialShareToType: SocialShareToType,
    val wxRequest: SendMessageToWX.Req
) : SocialShareConfig(socialShareMediaType, socialShareToType) {

    companion object {

        /**
         * 分享微信文本
         * [shareToType] 分享目标 朋友圈、会话、收藏
         * [text]文本内容
         * [description] 描述 默认空字符串
         */
        fun buildTextShareConfig(
            shareToType: SocialShareToType,
            text: String,
            description: String = ""
        ): SocialWeChatShareConfig {
            return SocialWeChatShareConfig(
                SocialShareMediaType.TEXT,
                shareToType,
                SendMessageToWX.Req().apply {
                    transaction = SocialUtils.buildTransaction("text")
                    message = WXMediaMessage().also { mWXMediaMessage ->
                        mWXMediaMessage.mediaObject = WXTextObject().also {
                            it.text = text
                        }
                        mWXMediaMessage.description = description
                    }
                    scene = SocialUtils.getShareScene(shareToType)
                })
        }


        /**
         * 分享图片
         * [shareToType] 分享目标 朋友圈、会话、收藏
         * [imageBitmap] 图片 使用后会自动做recycle
         * [thumbBitmap] 缩略图 使用后会自动做recycle 为空时默认内部自动创建一个150*150的缩略图
         */
        fun buildImageShareConfig(
            shareToType: SocialShareToType,
            imageBitmap: Bitmap,
            thumbBitmap: Bitmap? = null
        ): SocialWeChatShareConfig {
            return SocialWeChatShareConfig(
                SocialShareMediaType.IMAGE,
                shareToType,
                SendMessageToWX.Req().apply {
                    transaction = SocialUtils.buildTransaction("img")
                    message = WXMediaMessage().also { mWXMediaMessage ->
                        mWXMediaMessage.mediaObject = WXImageObject(imageBitmap)
                        mWXMediaMessage.thumbData = SocialUtils.bitmap2ByteArray(
                            thumbBitmap ?: Bitmap.createScaledBitmap(
                                imageBitmap,
                                150,
                                150,
                                true
                            )
                        )

                    }
                    scene = SocialUtils.getShareScene(shareToType)
                    if (!imageBitmap.isRecycled) imageBitmap.recycle()
                    if (thumbBitmap != null && !thumbBitmap.isRecycled) thumbBitmap.recycle()
                })
        }

        /**
         * 分享音乐
         * [shareToType] 分享目标 朋友圈、会话、收藏
         * [musicUrl] 音乐url
         * [musicTitle] 音乐标题
         * [musicDescription] 音乐描述
         * [thumbBitmap] 音乐缩略图 为空时不显示图片
         */
        fun buildMusicShareConfig(
            shareToType: SocialShareToType,
            musicUrl: String,
            musicTitle: String = "",
            musicDescription: String = "",
            thumbBitmap: Bitmap? = null
        ): SocialWeChatShareConfig {

            return SocialWeChatShareConfig(
                SocialShareMediaType.MUSIC,
                shareToType,
                SendMessageToWX.Req().apply {
                    transaction = SocialUtils.buildTransaction("music")
                    message = WXMediaMessage().also { mWXMediaMessage ->
                        mWXMediaMessage.mediaObject = WXMusicObject().also {
                            it.musicUrl = musicUrl
                        }

                        mWXMediaMessage.title = musicTitle
                        mWXMediaMessage.description = musicDescription

                        thumbBitmap?.let {
                            mWXMediaMessage.thumbData = SocialUtils.bitmap2ByteArray(
                                Bitmap.createScaledBitmap(
                                    it,
                                    150,
                                    150,
                                    true
                                )
                            )
                        }
                    }

                    scene = SocialUtils.getShareScene(shareToType)
                    if (thumbBitmap != null && !thumbBitmap.isRecycled) thumbBitmap.recycle()
                })
        }

        /**
         * 分享视频
         * [shareToType] 分享目标 朋友圈、会话、收藏
         * [videoUrl] 视频url
         * [videoTitle] 视频标题
         * [videoDescription] 视频描述
         * [thumbBitmap] 视频缩略图  为空时不显示图片
         */
        fun buildVideoShareConfig(
            shareToType: SocialShareToType,
            videoUrl: String,
            videoTitle: String = "",
            videoDescription: String = "",
            thumbBitmap: Bitmap? = null
        ): SocialWeChatShareConfig {

            return SocialWeChatShareConfig(
                SocialShareMediaType.VIDEO,
                shareToType,
                SendMessageToWX.Req().apply {
                    transaction = SocialUtils.buildTransaction("video")
                    message = WXMediaMessage().also { mWXMediaMessage ->
                        mWXMediaMessage.mediaObject = WXVideoObject().also {
                            it.videoUrl = videoUrl
                        }

                        mWXMediaMessage.title = videoTitle
                        mWXMediaMessage.description = videoDescription

                        thumbBitmap?.let {
                            mWXMediaMessage.thumbData = SocialUtils.bitmap2ByteArray(
                                Bitmap.createScaledBitmap(
                                    it,
                                    150,
                                    150,
                                    true
                                )
                            )
                        }
                    }

                    scene = SocialUtils.getShareScene(shareToType)
                    if (thumbBitmap != null && !thumbBitmap.isRecycled) thumbBitmap.recycle()
                })
        }


        /**
         * 分享网页
         * [shareToType] 分享目标 朋友圈、会话、收藏
         * [webpageUrl] 网页url
         * [webpageTitle] 网页标题
         * [webpageDescription] 网页描述
         * [thumbBitmap] 网页缩略图  为空时不显示图片
         */
        fun buildWebPageShareConfig(
            shareToType: SocialShareToType,
            webpageUrl: String,
            webpageTitle: String = "",
            webpageDescription: String = "",
            thumbBitmap: Bitmap? = null
        ): SocialWeChatShareConfig {

            return SocialWeChatShareConfig(
                SocialShareMediaType.WEBPAGE,
                shareToType,
                SendMessageToWX.Req().apply {
                    transaction = SocialUtils.buildTransaction("webpage")
                    message = WXMediaMessage().also { mWXMediaMessage ->
                        mWXMediaMessage.mediaObject = WXWebpageObject().also {
                            it.webpageUrl = webpageUrl
                        }

                        mWXMediaMessage.title = webpageTitle
                        mWXMediaMessage.description = webpageDescription

                        thumbBitmap?.let {
                            mWXMediaMessage.thumbData = SocialUtils.bitmap2ByteArray(
                                Bitmap.createScaledBitmap(
                                    it,
                                    150,
                                    150,
                                    true
                                )
                            )
                        }
                    }

                    scene = SocialUtils.getShareScene(shareToType)
                    if (thumbBitmap != null && !thumbBitmap.isRecycled) thumbBitmap.recycle()
                })
        }


        /**
         * 分享小程序
         * [shareToType] 分享目标 朋友圈、会话、收藏 这里预留了朋友圈和收藏 但是小程序目前只支持分享到会话，指定其他方式可能报错
         * [webpageUrl] 网页链接
         * [miniprogramType] 小程序版本 0 1 2 分别代表 正式版 测试版 体验版 默认0 [com.tencent.mm.opensdk.modelmsg.WXMiniProgramObject.MINIPTOGRAM_TYPE_RELEASE]
         * [miniProgramRealId] 小程序的原始 id 获取方法：登录小程序管理后台-设置-基本设置-帐号信息
         * [miniprogramPath] 小程序的 path 小程序页面路径；对于小游戏，可以只传入 query 部分，来实现传参效果，如：传入 "?foo=bar"
         * [withShareTicket] 是否使用带 shareTicket 的分享 [https://developers.weixin.qq.com/doc/oplatform/Mobile_App/Share_and_Favorites/Android.html]
         * [webpageTitle] 小程序消息标题
         * [webpageDescription] 小程序消息描述
         * [thumbBitmap] 小程序消息缩略图  为空时不显示图片
         */
        fun buildMiniProgramShareConfig(
            shareToType: SocialShareToType,
            webpageUrl: String,
            miniprogramType: Int = 0,
            miniProgramRealId: String,
            miniprogramPath: String,
            withShareTicket: Boolean = false,
            webpageTitle: String = "",
            webpageDescription: String = "",
            thumbBitmap: Bitmap
        ): SocialWeChatShareConfig {

            return SocialWeChatShareConfig(
                SocialShareMediaType.WECHAT_MINI_PROGRAM,
                shareToType,
                SendMessageToWX.Req().apply {
                    transaction = SocialUtils.buildTransaction("miniProgram")
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
                        thumbBitmap?.let {
                            mWXMediaMessage.thumbData = SocialUtils.bitmap2ByteArray(
                                Bitmap.createScaledBitmap(
                                    it,
                                    150,
                                    150,
                                    true
                                )
                            )
                        }
                    }

                    scene = SocialUtils.getShareScene(shareToType)
                    if (thumbBitmap != null && !thumbBitmap.isRecycled) thumbBitmap.recycle()
                })
        }
    }
}