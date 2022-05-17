# SocialHelper

[SocialHelper](https://markdown.lovejade.cn/?utm_source=markdown.lovejade.cn) 可以帮你快速完成国内以及国外很多平台的授权登录、分享功能。全部采用三方平台最新的Api实现；平台太多需要慢慢迭代上去，我会一直维护这个库。由于测试比较困难，每加入一个平台我都要去做测试，大家不要催。

由于该类型库的测试太麻烦，欢迎小伙伴们与我一起维护该库，点击[邮箱1](wdsf.top@gmail.com)/[邮箱2](jason.xa.china@gmail.com)/[邮箱3](dev_zwy@aliyun.com)与我联系，帮忙测试自己拥有的平台。我会在文末永久印下参与测试的小伙伴，愿岁月静好~

## 待办

- [x] 🎉 微信平台 **授权**、**获取用户资料**、**分享**[支持文本、图片、音乐、视频、网页、小程序]
- [ ] 🍀 QQ平台
- [ ] 🏁 Facebook平台
- [ ] 💃🏻 Google
- [ ] 🚑 Line
- [ ] 📝 ...

## 如何使用

**注意**：__Demo是无法直接运行看到结果的。因为平台有限制报名、签名、以及相关key的校验；__

#### 1.引入依赖  [![Packaging status](https://img.shields.io/nexus/r/top.wdsf/SocialHelper?label=SocialHelper&nexusVersion=2&server=https%3A%2F%2Fs01.oss.sonatype.org)](https://github.com/devzwy/SocialHelper)

> 下面的版本号(1.0.0)换成上面图片中的最新版本(**去掉v**)
> 点击Sync Now

```
implementation("top.wdsf:SocialHelper:1.0.0")
```

#### 2.初始化

> 别忘了在AndroidManifest.xml注册自己的Application~

```
class MyApplication:Application() {
    override fun onCreate() {
        super.onCreate()
        SocialHelper.init(SocialConfig.Build(context = this)
            .logEnable(enable = BuildConfig.DEBUG)
            .logTag(tag = "SocialHelper")
            .weChat(appId = "替换为你的微信appid",appSecretKey = "替换为你的微信appSecretKey")
            .otherPlatform()
            .build())
    }
}
```

#### 3.调用Api（后文有每个平台接入还需要完成的工作，别忘了）

```
//授权码获取 有配置appSecretKey时同步获取accessToken，否则仅获取authCode
SocialHelper.startAuth(
                platform = Platform.WECHAT,
                onSocialAuthListener = object : OnSocialAuthListener<WeChatAuth> {
                    /**
                     * 授权登陆错误回传
                     */
                    override fun onSocialAuthError(platform: Platform, errorMsg: String) {
                        logE("onSocialAuthError() -> platform:${platform.name},errorMsg:${errorMsg}")
                    }

                    /**
                     * 成功回调
                     */
                    override fun onSocialAuthSuccess(platform: Platform, data: WeChatAuth) {
                        logD("onSocialAuthSuccess() -> platform:${platform.name},data:${data.toJsonStr()}")
                    }
                }
            )
//获取用户资料

SocialHelper.getUserInfo(platform = platform,mSocialAuth = data,onSocialUserInfoListener = object :OnSocialUserInfoListener<WeChatUserInfoRespData>{
/**

* 失败回调
  */
  override fun onGetUserInfoError(platform: Platform, errorMsg: String) {
  logE("onGetUserInfoError() -> platform:${platform.name},errorMsg:${errorMsg}")
  }

/**

* 成功回调
  */
  override fun onGetUserInfoSuccess(
  platform: Platform,
  data: WeChatUserInfoRespData
  ) {
  logD("onSocialAuthSuccess() -> platform:${platform.name},data:${data.toJsonStr()}")
  }
  })
```

## 各平台集成你还需要完成的工作

### 1.微信集成

__授权流程：__ 调起客户端获取授权码(authCode) - 使用授权码获取AccessToken(**需要appSecretKey**)
__获取用户资料：__ 在成功获取到accessToken后即可获取用户资料(获得unicode)

> 这里建议前端获取授权码即可，其他工作交给后台完成，**本SDK不强制传入微信appSecretKey**，当用户在调用授权Api时如果没有配置appSecretKey时将返回authCode，不会去获取accessToken。

- 在同包名下创建wxapi包，并新建WXEntryActivity(名称不要变)继承自SocialWxEntryActivity

```
class WXEntryActivity:SocialWxEntryActivity() {
}
```

- 在AndroidManifest.xml中注册该Activity

```
<activity android:name=".wxapi.WXEntryActivity"
            android:exported="true"
            android:taskAffinity="这里写你的包名"
            android:launchMode="singleTask"
            />
```

- 最后记得App要签名后再运行，否则可能无法调起微信~
