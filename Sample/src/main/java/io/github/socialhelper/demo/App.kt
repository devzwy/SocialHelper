package io.github.socialhelper.demo

import android.app.Application
import io.github.social.SocialConfig
import io.github.social.SocialHelper

class App:Application() {
    override fun onCreate() {
        super.onCreate()
        SocialHelper.init(SocialConfig.buildSocialConfig(this){
            enableLog()
            enableWeChatPlatform("微信AppId","微信secretKey")
        })
    }

}