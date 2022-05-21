package io.github.socialhelper.demo

import android.app.Application
import io.github.devzwy.socialhelper.SocialConfig
import io.github.devzwy.socialhelper.SocialHelper

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        SocialHelper.init(SocialConfig.buildSocialConfig(this) {
            enableLog()
            enableWeChatPlatform("微信AppId", "微信secretKey")
            enableAlipayPlatform("支付宝AppId", "支付宝商户号", "支付宝应用私钥")
        })
    }

}