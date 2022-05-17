package top.wdsf.socialhelper.data.base

import top.wdsf.socialhelper.data.Platform
import top.wdsf.socialhelper.data.TokenFrom

/**
 * 登陆授权回传数据的基类
 */
abstract class SocialAuth {
    abstract fun from(): TokenFrom
}