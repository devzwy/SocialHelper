package top.wdsf.socialhelper.listeners

import top.wdsf.socialhelper.data.Platform

/**
 * 分享结果回调
 */
interface OnSocialShareListener {

    /**
     * 分享成功
     * [platform] 成功的通道
     */
    fun onShareSuccess(platform: Platform)

    /**
     * 分享失败
     * [platform] 失败的通道
     * [errorMsg] 失败提示
     */
    fun onShareError(platform: Platform, errorMsg: String)
}