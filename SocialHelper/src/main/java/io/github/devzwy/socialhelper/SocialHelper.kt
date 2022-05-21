package io.github.devzwy.socialhelper

object SocialHelper {

    /*配置文件*/
    lateinit var socialConfig: SocialConfig

    /**
     * 初始化工具 切记传入application
     */
    fun init(socialConfig: SocialConfig) {
        SocialHelper.socialConfig = socialConfig
    }


}