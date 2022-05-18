package io.github.socialhelper.demo

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import io.github.social.SocialHelper
import io.github.social.utils.logE
import io.github.social.utils.toJsonStr
import io.github.socialhelper.wechat.*
import io.github.socialhelper.wechat.data.WeChatShareType

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<View>(R.id.tvvvv).setOnClickListener { v ->
//            SocialHelper.aaa()
//            SocialHelper.reqWeChatAuth(onWeChatReqAuthSuccess = {
//                Toast.makeText(this,it.toJsonStr(),Toast.LENGTH_LONG).show()
//            }, onWeChatReqAuthError = {
//                Toast.makeText(this,it,Toast.LENGTH_LONG).show()
//            })

            SocialHelper.startWeChatPay("1365026102","wx271754067562114fedb5565b1827663000","D94996F23D4542B277F35756122DEF7C")

//            SocialHelper.getUserInfo("56_qoH8DaSoiTeRKWA-KHd6ExkKKK3j9N5v5ykfvJ22D3OG97hUFOzY_Xr-VHWS4SVErlh3Ox3Mp6p8sAieRQfhmgzN-OBslqxG6PKFhyvWZAQ","oHyiq6WWtG8-xgJDWWYMvShFRNyU",{
//                Toast.makeText(this,it.toJsonStr(), Toast.LENGTH_LONG).show()
//            },{
//                Toast.makeText(this,it,Toast.LENGTH_LONG).show()
//            })

//            val bitmap = BitmapFactory.decodeResource(resources, R.raw.test)
//
//            SocialHelper.shareMiniProgramToWeChat(WeChatShareType.SCENE_FAVORITE,"123",0,"123","",false,null,null,bitmap,{
//                Toast.makeText(this,"分享成功",Toast.LENGTH_LONG).show()
//            },{
//                Toast.makeText(this,it,Toast.LENGTH_LONG).show()
//            })

        }

    }
}