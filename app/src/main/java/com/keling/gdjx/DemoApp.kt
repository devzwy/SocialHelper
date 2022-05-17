package com.keling.gdjx

import android.app.Application
import top.wdsf.socialhelper.SocialHelper
import top.wdsf.socialhelper.data.SocialConfig

class DemoApp : Application() {

    override fun onCreate() {
        super.onCreate()
        SocialHelper.init(
            SocialConfig.Build(this)
                .weChat("12222","33333")
                .build()
        )
    }
}