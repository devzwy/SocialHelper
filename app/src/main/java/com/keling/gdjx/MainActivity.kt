package com.keling.gdjx

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.MotionEvent
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import com.whygraphics.multilineradiogroup.MultiLineRadioGroup
import top.wdsf.socialhelper.SocialHelper
import top.wdsf.socialhelper.data.Platform
import top.wdsf.socialhelper.data.SocialShareMediaType
import top.wdsf.socialhelper.data.SocialShareToType
import top.wdsf.socialhelper.data.base.SocialAuth
import top.wdsf.socialhelper.data.base.SocialShareConfig
import top.wdsf.socialhelper.data.base.SocialUserInfo
import top.wdsf.socialhelper.data.shareconfig.SocialWeChatShareConfig
import top.wdsf.socialhelper.data.wxapi_resp.WeChatAuth
import top.wdsf.socialhelper.listeners.OnSocialAuthListener
import top.wdsf.socialhelper.listeners.OnSocialShareListener
import top.wdsf.socialhelper.listeners.OnSocialUserInfoListener
import top.wdsf.socialhelper.utils.toJsonStr

class MainActivity : AppCompatActivity(), OnSocialAuthListener<SocialAuth>,
    OnSocialUserInfoListener<SocialUserInfo>, OnSocialShareListener {

    lateinit var platforms: Array<String>

    lateinit var contentTypes: Array<String>

    lateinit var shareToTypes: Array<String>

    //记录日志的txt
    lateinit var logTextView: AppCompatEditText

    //平台
    lateinit var radioGroupPlatform: MultiLineRadioGroup

    //分享内容的类型
    lateinit var radioGroupContentType: MultiLineRadioGroup

    //目标通道类型
    lateinit var radioGroupToType: MultiLineRadioGroup

    //获取授权按钮
    lateinit var btGetAuth: Button

    //获取用户资料按钮
    lateinit var btGetUserInfo: Button

    //分享按钮
    lateinit var btShare: Button

    //用户授权标识 为空标识未获取到
    var mSocialAuth: SocialAuth? = null

    //默认的平台
    var platform: Platform = Platform.WECHAT

    //默认分享到的类型
    var shareToType = SocialShareToType.SCENE_SESSION

    //默认分享内容的类型
    var shareMediaType = SocialShareMediaType.TEXT

    val testTitle = "标题栏目"
    val testDec = "描述文本"
    val testMp3Url = "https://download.top.wdsf/dev/music/test.mp3"
    val testMp4Url = "https://download.top.wdsf/dev/video/test.mp4"
    val testWWWUrl = "http://top.wdsf"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        platforms = resources.getStringArray(R.array.radio_buttons_platform)
        shareToTypes = resources.getStringArray(R.array.radio_buttons_channel)
        contentTypes = resources.getStringArray(R.array.radio_buttons_type)

        initView()

        initRadioGroupListener()

        initClickListener()

        initLogView()

    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initLogView() {
        logTextView.movementMethod = ScrollingMovementMethod.getInstance()
        logTextView.setOnTouchListener { v, event ->

            if (event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_MOVE) {
                v.parent.requestDisallowInterceptTouchEvent(true)
            } else if (event.action == MotionEvent.ACTION_UP) {
                v.parent.requestDisallowInterceptTouchEvent(false)
            }
            return@setOnTouchListener false
        }
    }

    private fun initClickListener() {

        //获取授权按钮
        btGetAuth.setOnClickListener {
            //注意这里的回调传入的泛型如果全部用 SocialAuth 时，需要在回调里根据不同平台强转成不同的授权回调实体 详细参考Api说明
            //当然最好的状态时接口直接带上泛型，比如微信可以这么调用:
            /*
            SocialHelper.startAuth(platform = Platform.WECHAT,onSocialAuthListener = object :OnSocialAuthListener<WeChatAuth>{
                override fun onSocialAuthSuccess(platform: Platform, data: WeChatAuth) {
                }

                override fun onSocialAuthError(platform: Platform, errorMsg: String) {
                }
            })
             */
            SocialHelper.startAuth(platform = platform, onSocialAuthListener = this)
        }

        //获取用户资料按钮
        btGetUserInfo.setOnClickListener {
            mSocialAuth?.let {
                //注意这里的回调传入的泛型如果全部用 SocialUserInfo 时，需要在回调里根据不同平台强转成不同的授权回调实体 详细参考Api说明
                //当然最好的状态时接口直接带上泛型，比如微信可以这么调用:
                /*
                SocialHelper.getUserInfo(platform = platform,mSocialAuth =it,object :OnSocialUserInfoListener<WeChatUserInfoRespData>{
                    override fun onGetUserInfoSuccess(
                        platform: Platform,
                        data: WeChatUserInfoRespData
                    ) {

                    }

                    override fun onGetUserInfoError(platform: Platform, errorMsg: String) {

                    }
                })
                 */
                SocialHelper.getUserInfo(platform = platform, mSocialAuth = it, this)
            }
        }


        //分享按钮
        btShare.setOnClickListener {

            val bitmap = BitmapFactory.decodeResource(resources, R.raw.test)

            val shareConfig: SocialShareConfig? = when (platform) {
                Platform.WECHAT -> {
                    when (shareMediaType) {
                        SocialShareMediaType.TEXT -> SocialWeChatShareConfig.buildTextShareConfig(
                            shareToType,
                            testTitle,
                            testDec
                        )
                        SocialShareMediaType.IMAGE -> SocialWeChatShareConfig.buildImageShareConfig(
                            shareToType,
                            bitmap
                        )
                        SocialShareMediaType.MUSIC -> SocialWeChatShareConfig.buildMusicShareConfig(
                            shareToType,
                            testMp3Url,
                            testTitle,
                            testDec,
                            bitmap
                        )
                        SocialShareMediaType.VIDEO -> SocialWeChatShareConfig.buildVideoShareConfig(
                            shareToType,
                            testMp4Url,
                            testTitle,
                            testDec,
                            bitmap
                        )
                        SocialShareMediaType.WEBPAGE -> SocialWeChatShareConfig.buildWebPageShareConfig(
                            shareToType,
                            testWWWUrl,
                            testTitle,
                            testDec,
                            bitmap
                        )
                        SocialShareMediaType.WECHAT_MINI_PROGRAM-> SocialWeChatShareConfig.buildMiniProgramShareConfig(
                            shareToType,
                            "http://www.qq.com",
                            0,
                            "gh_d43f693ca31f",
                            "/12121",
                            false,
                            "111",
                            "2222",
                            bitmap
                        )
                        else -> null
                    }
                }
                else -> null
            }

            shareConfig?.let {
                SocialHelper.share(
                    platform = platform,
                    shareConfig = it,
                    onSocialShareListener = this
                )
            }
        }


    }

    private fun initRadioGroupListener() {

        //平台选择回调
        radioGroupPlatform.setOnCheckedChangeListener(MultiLineRadioGroup.OnCheckedChangeListener { group, button ->

            when (button.text.toString()) {
                platforms[0] -> {
                    //微信
                    platform = Platform.WECHAT
                }
                platforms[1] -> {
                    //支付宝
                    platform = Platform.ALIPAY
                }
                platforms[2] -> {
                    //QQ
                    platform = Platform.QQ
                }
                platforms[3] -> {
                    //Line
                    platform = Platform.LINE
                }
                platforms[4] -> {
                    //Google
                    platform = Platform.GOOGLE
                }
                platforms[5] -> {
                    //Facebook
                    platform = Platform.FACEBOOK
                }
            }

            refreshUI()

        })

        //分享类型选择回调
        radioGroupContentType.setOnCheckedChangeListener(MultiLineRadioGroup.OnCheckedChangeListener { group, button ->

            when (button.text) {
                contentTypes[0] -> {
                    shareMediaType = SocialShareMediaType.TEXT
                }

                contentTypes[1] -> {
                    shareMediaType = SocialShareMediaType.IMAGE
                }

                contentTypes[2] -> {
                    shareMediaType = SocialShareMediaType.MUSIC
                }

                contentTypes[3] -> {
                    shareMediaType = SocialShareMediaType.VIDEO
                }

                contentTypes[4] -> {
                    shareMediaType = SocialShareMediaType.WEBPAGE
                }

                contentTypes[5] -> {
                    shareMediaType = SocialShareMediaType.WECHAT_MINI_PROGRAM
                }
            }

            refreshUI()
        })

        //分享目标平台切换
        radioGroupToType.setOnCheckedChangeListener(MultiLineRadioGroup.OnCheckedChangeListener { group, button ->

            when (button.text) {
                shareToTypes[0] -> {
                    //会话
                    shareToType = SocialShareToType.SCENE_SESSION
                }

                contentTypes[1] -> {
                    //朋友圈
                    shareToType = SocialShareToType.SCENE_TIMELINE
                }

                contentTypes[2] -> {
                    //收藏
                    shareToType = SocialShareToType.SCENE_FAVORITE
                }
                else -> {
                }
            }

            refreshUI()
        })
    }

    @SuppressLint("SetTextI18n")
    fun refreshUI() {
    }

    private fun initView() {
        logTextView = findViewById(R.id.logTextView)
        radioGroupPlatform = findViewById(R.id.radioGroupPlatform)
        radioGroupContentType = findViewById(R.id.radioGroupContentType)
        radioGroupToType = findViewById(R.id.radioGroupToType)
        btGetAuth = findViewById(R.id.btGetAuth)
        btGetUserInfo = findViewById(R.id.btGetUserInfo)
        btShare = findViewById(R.id.btShare)
    }


    private fun toast(msg: String) {
        Toast.makeText(application, msg, Toast.LENGTH_SHORT).show()
    }

    /**
     *  授权成功回调
     */
    override fun onSocialAuthSuccess(platform: Platform, data: SocialAuth) {
        appendLog("授权回传(onSuccess)[${platform.name}]：${data.toJsonStr()}")

        mSocialAuth = data

        if (platform == Platform.WECHAT) {
            if ((data as WeChatAuth).weChatAccessTokenResultData == null) {
                mSocialAuth = null
                toast("未配置微信平台的appSecretKey时只能获取授权码，无法获取AccessToken！")
                return
            }
        }

        btGetUserInfo.isEnabled = true
    }

    /**
     * 授权失败回调
     */
    override fun onSocialAuthError(platform: Platform, errorMsg: String) {
        appendLog("授权回传(onError)[${platform.name}]：${errorMsg}")
        toast(errorMsg)
    }

    /**
     * 授权取消回调
     */
    override fun onSocialAuthCancel(platform: Platform) {
        appendLog("授权回传(onCancel)[${platform.name}]：无情的取消了！狗东西！")
    }

    /**
     * 获取用户资料回传
     */
    override fun onGetUserInfoSuccess(platform: Platform, data: SocialUserInfo) {
        appendLog("用户资料(onSuccess)[${platform.name}]：${data.toJsonStr()}")
    }

    /**
     * 获取用户资料失败回调
     */
    override fun onGetUserInfoError(platform: Platform, errorMsg: String) {
        appendLog("用户资料(onError)[${platform.name}]：${errorMsg}")
        toast(errorMsg)
    }


    private fun appendLog(content: String) {
        if (!logTextView.text.isNullOrEmpty()) {
            logTextView.append("\n\n")
        }
        logTextView.append(content)
        logTextView.setSelection(logTextView.text?.length ?: 0)
    }

    /**
     * 分享成功
     * [platform] 成功的通道
     */
    override fun onShareSuccess(platform: Platform) {
        appendLog("分享成功(onShareSuccess)[${platform.name}]")
    }

    /**
     * 分享失败
     * [platform] 失败的通道
     * [errorMsg] 失败提示
     */
    override fun onShareError(platform: Platform, errorMsg: String) {
        appendLog("分享失败(onShareError)[${platform.name}]：${errorMsg}")
        toast(errorMsg)
    }


}