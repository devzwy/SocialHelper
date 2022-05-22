package io.github.devzwy.socialhelper.google

import android.content.Intent
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import io.github.devzwy.socialhelper.SocialConfig
import io.github.devzwy.socialhelper.SocialHelper
import io.github.devzwy.socialhelper.google.GoogleSocialConst.Companion.REQUEST_CODE_GOOGLE_AUTH
import io.github.devzwy.socialhelper.utils.*


lateinit var onGoogleReqAuthError: (String) -> Unit
lateinit var onGoogleReqAuthSuccess: (GoogleSignInAccount) -> Unit

/**
 * 在Google授权当前页面的Activity调用，调用处于[reqGoogleAuth]传入的activity保持一致 调用时请判断requestCode==[REQUEST_CODE_GOOGLE_AUTH]
 */
fun SocialHelper.onGoogleAuthResult(intent: Intent?) {
    runCatching {
        val task = GoogleSignIn.getSignedInAccountFromIntent(intent)
        task.getResult(ApiException::class.java).let {
            (it.serverAuthCode.isNullOrEmpty()).no {
                //授权成功
                onGoogleReqAuthSuccess(it)
            }.otherwise {
                onGoogleReqAuthError(socialConfig.application.getString(R.string.social_auth_fail))
            }
        }
    }.onFailure {
        it.printStackTrace()
        onGoogleReqAuthError(
            socialConfig.application.getString(
                R.string.social_auth_fail_exception,
                it.message ?: ""
            )
        )
    }
}

/**
 * 发起Google授权 授权成功会在传入Activity对应的onActivityResult中回调，请务必在该回调中调用[onGoogleAuthResult]
 * 常见错误码：10 肯能为clientId配置错了或者签名校验没过，或者包名校验没过，或者去Google后台重新生成json配置  12500:json文件是不是忘了啊小老弟
 * [activity] Google SDK内部需要
 * [onError] 授权失败的回调
 * [onSuccess] 授权成功的回调 成功时会回传serverAuthCode
 */
fun SocialHelper.reqGoogleAuth(
    activity: FragmentActivity,
    onError: (String) -> Unit,
    onSuccess: (GoogleSignInAccount) -> Unit
) {

    onGoogleReqAuthError = onError
    onGoogleReqAuthSuccess = onSuccess

    kotlin.runCatching {
        val lastAccountInfo = GoogleSignIn.getLastSignedInAccount(this.socialConfig.application)
        (lastAccountInfo!=null).yes {
            "从缓存直接返回accountInfo".logD()
            onGoogleReqAuthSuccess(lastAccountInfo!!)
        }.otherwise {
            checkGoogleClientInit(this.socialConfig, activity).let { mGoogleSiginClient ->
                (mGoogleSiginClient != null).yes {
                    activity.startActivityForResult(
                        mGoogleSiginClient!!.signInIntent,
                        REQUEST_CODE_GOOGLE_AUTH
                    )
                }.otherwise {
                    "mGoogleSiginClient = null".logE()
                    onGoogleReqAuthError("init error")
                }
            }
        }
    }.onFailure {
        it.localizedMessage?.logE()
        onGoogleReqAuthError(it.message ?: "Google service not found")
    }
}

/**
 * 退出google登陆
 */
fun SocialHelper.signOut(activity:FragmentActivity,onSuccess: () -> Unit,onError: (String) -> Unit){
    kotlin.runCatching {
            checkGoogleClientInit(this.socialConfig, activity).let { mGoogleSiginClient ->
                (mGoogleSiginClient != null).yes {
                    mGoogleSiginClient!!.signOut().addOnCanceledListener {
                        onError("cancel")
                    }.addOnCompleteListener {
                        //作废token
                        mGoogleSiginClient.revokeAccess()
                        onSuccess()
                    }

                }.otherwise {
                    "mGoogleSiginClient = null".logE()
                    onError("mGoogleSiginClient was null")
                }
            }
    }.onFailure {
        it.localizedMessage?.logE()
        onError(it.message ?:"login out fail")
    }
}

/**
 * 初始化google服务
 */
@Throws(Exception::class)
private fun checkGoogleClientInit(
    socialConfig: SocialConfig,
    activity: FragmentActivity
): GoogleSignInClient? {
    return try {
        GoogleSignIn.getClient(
            activity, GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(Scope(Scopes.DRIVE_APPFOLDER))
                .requestServerAuthCode(socialConfig.googleClientId)
                .build()
        )
    } catch (e: Exception) {
        null
    }
}
