package wdsf.top.socialhelper.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import wdsf.top.socialhelper.data.wxapi_resp.WeChatAccessTokenResultData

object SpUtils {

    private lateinit var mSp: SharedPreferences

    fun init(context: Context) {
        mSp = context.getSharedPreferences("social_helper", Context.MODE_PRIVATE)
    }

    /**
     * 写入accessToken
     */
    fun putAccessToken(weChatAccessTokenResultData: WeChatAccessTokenResultData) {
        mSp.edit().putString("wx_access_token", Gson().toJson(weChatAccessTokenResultData)).apply()
    }

    /**
     * 获取最后一次获取到的accessToken
     */
    fun getAccessToken(): WeChatAccessTokenResultData? {
        mSp.getString("wx_access_token", null)?.let {
            return Gson().fromJson(it, WeChatAccessTokenResultData::class.java)
        }
        return null
    }


    /**
     * 清空存储的accessToken
     */
    fun clearAccessToken(){
        mSp.edit().remove("wx_access_token").apply()
    }
}