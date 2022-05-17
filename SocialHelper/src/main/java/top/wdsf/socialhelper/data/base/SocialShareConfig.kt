package top.wdsf.socialhelper.data.base

import top.wdsf.socialhelper.data.SocialShareMediaType
import top.wdsf.socialhelper.data.SocialShareToType

/**
 * 分享时的基类
 * [socialShareMediaType] 分享的资源类型
 * [socialShareMediaType] 分享到的通道类型 分享到哪里？
 */
abstract class SocialShareConfig(val socialShareMediaType:SocialShareMediaType,val socialShareToType:SocialShareToType) {

}