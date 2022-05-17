package wdsf.top.socialhelper.listeners

import wdsf.top.socialhelper.data.Platform
import wdsf.top.socialhelper.data.base.SocialAuth
import wdsf.top.socialhelper.data.base.SocialUserInfo

/**
 * 用户资料获取的回调
 */
interface OnSocialUserInfoListener<T : SocialUserInfo> {

    /**
     * 成功回调
     */
    fun onGetUserInfoSuccess(platform: Platform, data: T)

    /**
     * 授权登陆错误回传
     */
    fun onGetUserInfoError(platform: Platform, errorMsg: String)


}