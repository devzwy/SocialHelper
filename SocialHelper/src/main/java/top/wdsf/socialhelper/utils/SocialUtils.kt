package top.wdsf.socialhelper.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.os.Build
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX
import top.wdsf.socialhelper.data.SocialShareToType
import java.io.ByteArrayOutputStream

object SocialUtils {

    /**
     * 构建微信的Transaction
     */
    fun buildTransaction(type: String): String {
        return "${type}${System.currentTimeMillis()}"
    }

    /**
     * 返回微信分享目标
     */
    fun getShareScene(shareToType: SocialShareToType): Int {
        return if (shareToType == SocialShareToType.SCENE_TIMELINE)
            SendMessageToWX.Req.WXSceneTimeline
        else if (shareToType == SocialShareToType.SCENE_SESSION) SendMessageToWX.Req.WXSceneSession
        else if (shareToType == SocialShareToType.SCENE_FAVORITE) SendMessageToWX.Req.WXSceneFavorite
        else -1
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
}