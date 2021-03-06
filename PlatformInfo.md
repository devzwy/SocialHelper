# 各平台还需要完成的工作
> 网络权限请自行添加

## 微信
- 项目跟包下创建**wxapi**包，并在该包下创建**WXEntryActivity**继承自**WeChatSocialEntryActivity**  
> 假如我的包名为：io.github.socialhelper.demo,则该文件创建目录如下
```
package io.github.socialhelper.demo.wxapi

import io.github.devzwy.socialhelper.wechat.WeChatSocialEntryActivity

class WXEntryActivity : WeChatSocialEntryActivity() {
}
```
- 在**AndroidManifest.xml**文件中注册该页面
```
 <activity android:name=".wxapi.WXEntryActivity"
            android:launchMode="singleTask"
            android:taskAffinity="把这里换成你的包名"
            android:exported="true"/>
```

## 支付宝
- 项目跟包下创建**apshare**包，并在该包下创建**ShareEntryActivity**继承自**AlipaySocialEntryActivity**
> 假如我的包名为：io.github.socialhelper.demo,则该文件创建目录如下
```
package io.github.socialhelper.demo.apshare

import io.github.devzwy.socialhelper.alipay.AlipaySocialEntryActivity

class ShareEntryActivity: AlipaySocialEntryActivity() {
}
```
- 在**AndroidManifest.xml**文件中注册该页面
```
 <activity android:name=".apshare.ShareEntryActivity"
            android:exported="true"/>
```

## Google
- src目录同级防止在google平台生成的**google-services.json**
  ![google_platform](https://download.wdsf.top/dev/image/google_platform.png)  

- 调用授权**Activity**的**onActivityResult**中增加如下代码
```
override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //加入这一行
        if (requestCode == REQUEST_CODE_GOOGLE_AUTH) SocialHelper.onGoogleAuthResult(data)
    }
```

## Line
- 在**AndroidManifest.xml**下的**application标签加入如下代码**
``` 
  <application
        //加入这两行
         xmlns:tools="http://schemas.android.com/tools"
        tools:replace="android:allowBackup"
        >
```

- 调用授权**Activity**的**onActivityResult**中增加如下代码
```
override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //加入这一行
        if (requestCode == REQUEST_CODE_LINE_AUTH) SocialHelper.onLineAuthResult(data)
    }
```