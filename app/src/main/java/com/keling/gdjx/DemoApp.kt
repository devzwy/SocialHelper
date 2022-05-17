package com.keling.gdjx

import android.app.Application
import wdsf.top.socialhelper.SocialHelper
import wdsf.top.socialhelper.data.SocialConfig

class DemoApp : Application() {

    override fun onCreate() {
        super.onCreate()
        SocialHelper.init(
            SocialConfig.Build(this)
                .weChat("wx123","123456")
                .build()
        )
    }
}