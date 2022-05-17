package wdsf.top.socialhelper.data.base

import wdsf.top.socialhelper.data.Platform
import wdsf.top.socialhelper.data.TokenFrom

/**
 * 登陆授权回传数据的基类
 */
abstract class SocialAuth {
    abstract fun from(): TokenFrom
}