package io.github.socialhelper.demo

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.linecorp.linesdk.auth.LineLoginResult
import com.whygraphics.multilineradiogroup.MultiLineRadioGroup
import io.github.devzwy.socialhelper.SocialHelper
import io.github.devzwy.socialhelper.alipay.reqAliPayAuth
import io.github.devzwy.socialhelper.alipay.shareImageToAlipay
import io.github.devzwy.socialhelper.alipay.shareTextToAlipay
import io.github.devzwy.socialhelper.alipay.shareWebPageToAlipay
import io.github.devzwy.socialhelper.google.GoogleSocialConst.Companion.REQUEST_CODE_GOOGLE_AUTH
import io.github.devzwy.socialhelper.google.onGoogleAuthResult
import io.github.devzwy.socialhelper.google.reqGoogleAuth
import io.github.devzwy.socialhelper.line.LineSocialConst.Companion.REQUEST_CODE_LINE_AUTH
import io.github.devzwy.socialhelper.line.onLineAuthResult
import io.github.devzwy.socialhelper.line.reqLineAuth
import io.github.devzwy.socialhelper.utils.toJsonStr
import io.github.devzwy.socialhelper.wechat.*
import io.github.devzwy.socialhelper.wechat.data.WeChatShareType
import io.github.devzwy.socialhelper.wechat.data.WeChatSocialReqAuthRespData

class MainActivity : AppCompatActivity() {

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

    lateinit var tv2: TextView

    lateinit var tv1:TextView

    //获取授权按钮
    lateinit var btGetAuth: Button

    //获取用户资料按钮
    lateinit var btGetUserInfo: Button

    //分享按钮
    lateinit var btShare: Button

    //默认的平台 0微信 1支付宝
    var platform = 0

    //默认分享到的类型
    var shareToType = 0

    //默认分享内容的类型
    var shareMediaType = 0

    val testTitle = "分享的测试标题"
    val testDec = "分享的测试描述内容！"
    val testMp3Url = "https://download.wdsf.top/dev/music/test.mp3"
    val testMp4Url = "https://download.wdsf.top/dev/video/test.mp4"
    val testImageUrl = "https://download.wdsf.top/dev/image/test.webp"
    val testWWWUrl = "http://wdsf.top"

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_GOOGLE_AUTH) SocialHelper.onGoogleAuthResult(data)
        if (requestCode == REQUEST_CODE_LINE_AUTH) SocialHelper.onLineAuthResult(data)
    }

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

    var weChatAuthData: WeChatSocialReqAuthRespData? = null

    var mGoogleSignInAccount: GoogleSignInAccount? = null
    var mLineLoginResult: LineLoginResult? = null

    private fun initClickListener() {

        //获取授权按钮
        btGetAuth.setOnClickListener {

            if (platform == 0) {
                //发起微信授权
                SocialHelper.reqWeChatAuth(onWeChatReqAuthError = {
                    appendLog(it)
                }, onWeChatReqAuthSuccess = {
                    appendLog(it.toJsonStr())
                    weChatAuthData = it
                    if (it.socialAccessTokenData != null) {
                        btGetUserInfo.isEnabled = true
                    }
                })
            }

            if (platform == 1) {
                //发起支付宝授权
                SocialHelper.reqAliPayAuth(this, true, {
                    appendLog(it)
                }, {
                    appendLog(it.toJsonStr())
                })
            }

            if (platform == 2) {
                //发起Google授权
                SocialHelper.reqGoogleAuth(this, {
                    appendLog(it)
                }, {
                    this.mGoogleSignInAccount = it
                    appendLog(it.toJsonStr())
                    btGetUserInfo.isEnabled = true
                })
            }

            if (platform == 3) {
                //发起Line授权
                SocialHelper.reqLineAuth(this, {
                    appendLog(it)
                }, {
                    this.mLineLoginResult = it
                    appendLog(it.toJsonStr())
                    btGetUserInfo.isEnabled = true
                })
            }

        }

        //获取用户资料按钮
        btGetUserInfo.setOnClickListener {

            if (platform == 0) {
                weChatAuthData?.socialAccessTokenData?.let {
                    SocialHelper.getWeChatUserInfo(it.access_token, it.openid, {
                        appendLog(it.toJsonStr())
                    }, {
                        appendLog(it)
                    })
                }
            }

            if (platform == 2) {
                //Google
                this.mGoogleSignInAccount?.let {
                    //演示 从对象取出对应用户资料
                    appendLog("personName:${it.displayName}")
                    appendLog("personGivenName:${it.givenName}")
                    appendLog("personFamilyName:${it.familyName}")
                    appendLog("personEmail:${it.email}")
                    appendLog("personId:${it.id}")
                    appendLog("personPhoto:${it.photoUrl}")
                }
            }

            if (platform == 3) {
                //Line
                this.mLineLoginResult?.lineProfile?.let {
                    //演示 从对象取出对应用户资料
                    appendLog("displayName:${it.displayName}")
                    appendLog("userId:${it.userId}")
                    appendLog("pictureUrl:${it.pictureUrl}")
                }
            }

        }

        //分享按钮
        btShare.setOnClickListener {

            val bitmap = BitmapFactory.decodeResource(resources, R.raw.test)

            when (platform) {
                0 -> {
                    when (shareMediaType) {
                        0 -> {

                            SocialHelper.shareTextToWeChat(if (shareToType == 0) WeChatShareType.SCENE_SESSION else if (shareToType == 1) WeChatShareType.SCENE_TIMELINE else WeChatShareType.SCENE_FAVORITE,
                                testTitle,
                                testDec,
                                {
                                    appendLog("分享成功")
                                },
                                {
                                    appendLog(it)
                                })

                        }

                        1 -> {
                            //图片
                            SocialHelper.shareImageToWeChat(if (shareToType == 0) WeChatShareType.SCENE_SESSION else if (shareToType == 1) WeChatShareType.SCENE_TIMELINE else WeChatShareType.SCENE_FAVORITE,
                                bitmap,
                                {
                                    appendLog("分享成功")
                                },
                                {
                                    appendLog(it)
                                })
                        }

                        2 -> {
                            //音乐
                            SocialHelper.shareMusicToWeChat(if (shareToType == 0) WeChatShareType.SCENE_SESSION else if (shareToType == 1) WeChatShareType.SCENE_TIMELINE else WeChatShareType.SCENE_FAVORITE,
                                testMp3Url,
                                testTitle,
                                testDec,
                                bitmap,
                                {
                                    appendLog("分享成功")
                                },
                                {
                                    appendLog(it)
                                })
                        }


                        3 -> {
                            //视频
                            SocialHelper.shareVideoToWeChat(if (shareToType == 0) WeChatShareType.SCENE_SESSION else if (shareToType == 1) WeChatShareType.SCENE_TIMELINE else WeChatShareType.SCENE_FAVORITE,
                                testMp4Url,
                                testTitle,
                                testDec,
                                bitmap,
                                {
                                    appendLog("分享成功")
                                },
                                {
                                    appendLog(it)
                                })
                        }

                        4 -> {
                            //网页
                            SocialHelper.shareWebPageToWeChat(if (shareToType == 0) WeChatShareType.SCENE_SESSION else if (shareToType == 1) WeChatShareType.SCENE_TIMELINE else WeChatShareType.SCENE_FAVORITE,
                                testWWWUrl,
                                testTitle,
                                testDec,
                                bitmap,
                                {
                                    appendLog("分享成功")
                                },
                                {
                                    appendLog(it)
                                })
                        }

                        5 -> {
                            //小程序
                            SocialHelper.shareMiniProgramToWeChat(if (shareToType == 0) WeChatShareType.SCENE_SESSION else if (shareToType == 1) WeChatShareType.SCENE_TIMELINE else WeChatShareType.SCENE_FAVORITE,
                                "http://www.wdsf.top",
                                0,
                                "gh_d43f693ca31f",
                                "/12121",
                                false,
                                "111",
                                "2222",
                                bitmap,
                                {
                                    appendLog("分享成功")
                                },
                                {
                                    appendLog(it)
                                })
                        }
                    }
                }

                1 -> {

                    when (shareMediaType) {
                        0 -> {
                            SocialHelper.shareTextToAlipay(testTitle, {
                                appendLog("分享到支付宝成功")
                            }, {
                                appendLog(it)
                            })
                        }
                        1 -> {
                            SocialHelper.shareImageToAlipay(testImageUrl, {
                                appendLog("分享到支付宝成功")
                            }, {
                                appendLog(it)
                            })
                        }

                        2 -> {
                            SocialHelper.shareWebPageToAlipay(
                                testWWWUrl,
                                testTitle,
                                testDec,
                                testImageUrl,
                                {
                                    appendLog("分享到支付宝成功")
                                },
                                {
                                    appendLog(it)
                                })
                        }

                        else -> {}
                    }
                }
            }

        }

    }

    private fun initRadioGroupListener() {

        //平台选择回调
        radioGroupPlatform.setOnCheckedChangeListener(MultiLineRadioGroup.OnCheckedChangeListener { group, button ->

            btGetUserInfo.isEnabled = false

            when (button.text.toString()) {
                platforms[0] -> {
                    //微信
                    platform = 0

                    contentTypes = resources.getStringArray(R.array.radio_buttons_type)

                    radioGroupContentType.removeAllButtons()
                    contentTypes.forEach {
                        radioGroupContentType.addButtons(it)
                    }

                    radioGroupContentType.check(contentTypes[0])

                    tv2.visibility = View.VISIBLE
                    tv1.visibility = View.VISIBLE
                    btShare.visibility = View.VISIBLE
                    radioGroupContentType.visibility = View.VISIBLE
                    radioGroupToType.visibility = View.VISIBLE
                    btGetUserInfo.visibility = View.VISIBLE

                }

                platforms[1] -> {
                    //支付宝
                    platform = 1

                    contentTypes = resources.getStringArray(R.array.radio_buttons_type_alipay)

                    radioGroupContentType.removeAllButtons()
                    contentTypes.forEach {
                        radioGroupContentType.addButtons(it)
                    }

                    radioGroupContentType.check(contentTypes[0])

                    radioGroupToType.visibility = View.GONE
                    tv2.visibility = View.GONE
                    tv1.visibility = View.VISIBLE
                    btShare.visibility = View.VISIBLE
                    radioGroupContentType.visibility = View.VISIBLE
                    btGetUserInfo.visibility = View.INVISIBLE
                }

                platforms[2] -> {
                    //Google
                    platform = 2

                    contentTypes = resources.getStringArray(R.array.radio_buttons_type_alipay)

                    radioGroupContentType.removeAllButtons()
                    contentTypes.forEach {
                        radioGroupContentType.addButtons(it)
                    }

                    radioGroupContentType.check(contentTypes[0])

                    radioGroupToType.visibility = View.GONE
                    tv2.visibility = View.GONE
                    tv1.visibility = View.GONE
                    btShare.visibility = View.GONE
                    radioGroupContentType.visibility = View.GONE
                    btGetUserInfo.visibility = View.VISIBLE
                }
                platforms[3] -> {
                    //Google
                    platform = 3

                    contentTypes = resources.getStringArray(R.array.radio_buttons_type_alipay)

                    radioGroupContentType.removeAllButtons()
                    contentTypes.forEach {
                        radioGroupContentType.addButtons(it)
                    }

                    radioGroupContentType.check(contentTypes[0])

                    radioGroupToType.visibility = View.GONE
                    tv2.visibility = View.GONE
                    tv1.visibility = View.GONE
                    btShare.visibility = View.GONE
                    radioGroupContentType.visibility = View.GONE
                    btGetUserInfo.visibility = View.VISIBLE
                }
            }

            refreshUI()

        })

        //分享类型选择回调
        radioGroupContentType.setOnCheckedChangeListener(MultiLineRadioGroup.OnCheckedChangeListener { group, button ->

            when (button.text) {
                contentTypes[0] -> {
                    shareMediaType = 0
                }

                contentTypes[1] -> {
                    shareMediaType = 1
                }

                contentTypes[2] -> {
                    shareMediaType = 2
                }

                contentTypes[3] -> {
                    shareMediaType = 3
                }

                contentTypes[4] -> {
                    shareMediaType = 4
                }

                contentTypes[5] -> {
                    shareMediaType = 5
                }
            }

            refreshUI()
        })

        //分享目标平台切换
        radioGroupToType.setOnCheckedChangeListener(MultiLineRadioGroup.OnCheckedChangeListener { group, button ->

            when (button.text) {
                shareToTypes[0] -> {
                    //会话
                    shareToType = 0
                }

                shareToTypes[1] -> {
                    //朋友圈
                    shareToType = 1
                }

                shareToTypes[2] -> {
                    //收藏
                    shareToType = 2
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
        tv2 = findViewById(R.id.tv2)
        tv1 = findViewById(R.id.tv1)
        btGetAuth = findViewById(R.id.btGetAuth)
        btGetUserInfo = findViewById(R.id.btGetUserInfo)
        btShare = findViewById(R.id.btShare)
    }


    private fun toast(msg: String) {
        Toast.makeText(application, msg, Toast.LENGTH_SHORT).show()
    }


    private fun appendLog(content: String) {
        if (!logTextView.text.isNullOrEmpty()) {
            logTextView.append("\n\n")
        }
        logTextView.append(content)
        logTextView.setSelection(logTextView.text?.length ?: 0)
    }

}