package io.github.devzwy.socialhelper.line

import android.app.Activity
import android.content.Intent
import com.linecorp.linesdk.LineApiResponseCode
import com.linecorp.linesdk.api.LineApiClient
import com.linecorp.linesdk.api.LineApiClientBuilder
import com.linecorp.linesdk.auth.LineAuthenticationParams
import com.linecorp.linesdk.auth.LineLoginApi
import com.linecorp.linesdk.auth.LineLoginResult
import io.github.devzwy.socialhelper.SocialHelper
import io.github.devzwy.socialhelper.line.LineSocialConst.Companion.REQUEST_CODE_LINE_AUTH
import io.github.devzwy.socialhelper.utils.logE
import io.github.devzwy.socialhelper.utils.toJsonStr
import java.util.*

private lateinit var onLineReqAuthError: (String) -> Unit
private lateinit var onLineReqAuthSuccess: (LineLoginResult) -> Unit

/**
 * 在Line授权当前页面的Activity调用，调用处于[reqLineAuth]传入的activity保持一致 调用时请判断requestCode==[REQUEST_CODE_LINE_AUTH]
 */
fun SocialHelper.onLineAuthResult(intent: Intent?) {
    runCatching {
        val result = LineLoginApi.getLoginResultFromIntent(intent)
        when (result.responseCode) {
            LineApiResponseCode.SUCCESS -> {
                onLineReqAuthSuccess(result)
            }
            LineApiResponseCode.CANCEL -> {
                //取消了操作
                onLineReqAuthError(socialConfig.application.getString(com.linecorp.linesdk.R.string.common_cancel))
            }
            else -> {
                onLineReqAuthError(result.errorData.message ?: result.toJsonStr())
            }
        }

    }.onFailure {
        it.printStackTrace()
        onLineReqAuthError(
            socialConfig.application.getString(
                R.string.social_auth_fail_exception,
                it.message ?: ""
            )
        )
    }
}

/**
 * 发起Line授权 授权成功会在传入Activity对应的onActivityResult中回调，请务必在该回调中调用[onLineAuthResult]
 * [activity] Line SDK内部需要
 * [onError] 授权失败的回调
 * [onSuccess] 授权成功的回调 成功时会回传LineLoginResult
 */
fun SocialHelper.reqLineAuth(
    activity: Activity,
    onError: (String) -> Unit,
    onSuccess: (LineLoginResult) -> Unit
) {
    onLineReqAuthError = onError
    onLineReqAuthSuccess = onSuccess

    kotlin.runCatching {
        activity.startActivityForResult(
            LineLoginApi.getLoginIntent(
                activity,
                socialConfig.lineAppId,
                LineAuthenticationParams.Builder()
                    .scopes(Arrays.asList(com.linecorp.linesdk.Scope.PROFILE))
                    .build()
            ), REQUEST_CODE_LINE_AUTH
        )
    }.onFailure {
        it.localizedMessage?.logE()
        onError(it.message ?: "Line service not found")
    }
}