package top.wdsf.socialhelper.listeners

import top.wdsf.socialhelper.data.Platform
import top.wdsf.socialhelper.data.base.SocialAuth

/**
 * 授权的回调
 */
interface OnSocialAuthListener<T : SocialAuth> {

    /**
     * 成功回调
     */
    fun onSocialAuthSuccess(platform: Platform, data: T)

    /**
     * 授权登陆错误回传
     */
    fun onSocialAuthError(platform: Platform, errorMsg: String)

    /**
     * 取消操作回传
     */
    fun onSocialAuthCancel(platform: Platform){

    }

}