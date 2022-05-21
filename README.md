# SocialHelper

[SocialHelper](https://github.com/devzwy/SocialHelper) 可以帮你快速完成国内以及国外很多平台的授权登录、分享功能。全部采用三方平台最新的Api实现；平台太多需要慢慢迭代上去，我会一直维护这个库。由于测试比较困难，每加入一个平台我都要去做测试，大家不要催。

由于该类型库的测试太麻烦，欢迎小伙伴们与我一起维护该库，**wdsf.top@gmail.com**与我联系，帮忙测试自己拥有的平台。我会在文末永久印下参与测试的小伙伴，愿岁月静好~

## 待办

- [x] 🎉 微信平台 **授权**、**获取用户资料**、**分享**[支持文本、图片、音乐、视频、网页、小程序]
- [ ] 🎉 支付宝平台 **授权**、**分享**[支持文本、图片、网页]
- [ ] 🏁 Facebook平台
- [ ] 💃🏻 Google
- [ ] 🚑 Line
- [ ] 📝 ...

[演示效果](https://download.wdsf.top/dev/video/show.mp4)  


## 如何使用

**注意**：__由于该类SDK二次封装的特殊性，Demo是无法直接运行看到结果的，具体请参考Api说明。或者下载打包好的Apk测试__

#### 1.引入依赖  [![Packaging status](https://img.shields.io/nexus/r/top.wdsf/SocialHelper?label=SocialHelper&nexusVersion=2&server=https%3A%2F%2Fs01.oss.sonatype.org)](https://github.com/devzwy/SocialHelper)

> 下面的版本号(1.0.0)换成上面图片中的最新版本(**去掉v**)
> 点击Sync Now

```
//必选
implementation("io.github:SocialHelper:TODO")
//可选
TODO
```

#### 2.初始化

> 别忘了在AndroidManifest.xml注册自己的Application~
> 依据各平台SDK的要求，您可能还需要对不同平台做单独配置。具体配置请[点我查看](https://github.com/devzwy/SocialHelper/blob/main/PlatformInfo.md)

```
class MyApplication:Application() {
    override fun onCreate() {
        super.onCreate()
         SocialHelper.init(SocialConfig.buildSocialConfig(this) {
            enableLog()
            enableWeChatPlatform("微信AppId", "微信secretKey")
            enableAlipayPlatform("支付宝AppId", "支付宝商户号", "支付宝应用私钥")
            ..
        })
    }
}
```

## Api列表

#### 微信 
- 获取微信accessToken
```
SocialHelper.reqWeChatAuth()
```
- 获取微信用户资料
```
SocialHelper.getWeChatUserInfo()
```

- 分享到微信
```
//分享文本 其他分享参考该Api
SocialHelper.shareTextToWeChat(
    weChatShareType: WeChatShareType, //WeChatShareType.SCENE_SESSION WeChatShareType.SCENE_TIMELINE WeChatShareType.SCENE_FAVORITE 
    text: String, //内容
    description: String, //描述
    onShareSuccess: (() -> Unit)? = null,
    onShareError: ((String) -> Unit)? = null
)
//分享图片
SocialHelper.shareImageToWeChat()

//分享音乐
SocialHelper.shareMusicToWeChat()

//分享视频
SocialHelper.shareVideoToWeChat()

//分享网页
SocialHelper.shareWebPageToWeChat()

//分享小程序
SocialHelper.shareMiniProgramToWeChat()

```

#### 支付宝
- 获取支付宝授权码
```
SocialHelper.reqAliPayAuth(this, true, {
                    appendLog(it)
                }, {
                    alipayResultSubData->
                    alipayResultSubData.auth_code
                    appendLog(it.toJsonStr())
                })
```

- 分享到支付宝
```
//分享文本 其他分享参考该Api
SocialHelper.shareTextToAlipay(
    text: String,//内容
    onShareSuccess: (() -> Unit)? = null,
    onShareError: ((String) -> Unit)? = null
)
//分享图片
SocialHelper.shareImageToAlipay()

//分享网页 会自动出现生活圈选项
SocialHelper.shareWebPageToAlipay()

```



