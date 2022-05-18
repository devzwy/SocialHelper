package io.github.socialhelper.wechat

import com.tencent.mm.opensdk.modelmsg.SendMessageToWX
import io.github.socialhelper.wechat.data.WeChatShareType

/**
 * 构建微信的Transaction
 */
fun buildTransaction(type: String): String {
    return "${type}${System.currentTimeMillis()}"
}

/**
 * 返回微信分享目标
 */
fun getShareScene(shareToType: WeChatShareType): Int {
    return if (shareToType == WeChatShareType.SCENE_TIMELINE)
        SendMessageToWX.Req.WXSceneTimeline
    else if (shareToType == WeChatShareType.SCENE_SESSION) SendMessageToWX.Req.WXSceneSession
    else if (shareToType == WeChatShareType.SCENE_FAVORITE) SendMessageToWX.Req.WXSceneFavorite
    else -1
}