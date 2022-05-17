package top.wdsf.socialhelper.listeners

import top.wdsf.socialhelper.data.Platform
import top.wdsf.socialhelper.data.base.SocialAuth
import top.wdsf.socialhelper.data.base.SocialUserInfo

/**
 * 用户资料获取的回调
 */
interface OnSocialUserInfoListener<T : SocialUserInfo> {

    /**
     * 成功回调
     */
    fun onGetUserInfoSuccess(platform: Platform, data: T)

    /**
     * 失败回调
     */
    fun onGetUserInfoError(platform: Platform, errorMsg: String)


}