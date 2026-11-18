# 定位测试器 / LocationLab

一个纯 Android 原生的定位诊断工具，面向 Android 设备的位置权限、GPS/网络 Provider 和当前位置读取测试。

## 当前版本

- Android 8.0+（API 26+）
- Java 17
- Android Gradle Plugin 8.7.3
- compileSdk 35
- 无第三方 SDK
- 不需要 Root
- 不需要网络权限

## 功能

- 检查定位权限。
- 检查 GPS / 网络定位 Provider 是否开启。
- 读取 Android 系统最近一次位置。
- 持续监听位置变化。
- 显示纬度、经度和定位精度。
- 一键复制坐标。
- 一键打开系统定位设置。
- GitHub Actions 自动构建 Debug APK。

## GitHub 上直接构建 APK

进入仓库：

https://github.com/qyz20010102-arch/dingwei

然后：

1. 打开 **Actions**。
2. 选择 **Build Android APK**。
3. 点击 **Run workflow**。
4. 等待构建完成。
5. 打开对应的 workflow run。
6. 在 **Artifacts** 中下载 `LocationLab-debug-apk`。
7. 解压后得到 `app-debug.apk`。

`main` 分支每次更新也会自动触发构建。

## Android Studio

使用 Android Studio 打开仓库根目录，安装：

- Android SDK Platform 35
- JDK 17

然后执行 `assembleDebug` 即可生成：

`app/build/outputs/apk/debug/app-debug.apk`

## 关于模拟定位

Android 的开发者选项提供了官方的模拟位置测试机制。第三方应用是否接受模拟位置由其自身实现决定。

本项目**不**：

- 修改微信或微信小程序；
- 隐藏 Android 的模拟位置状态；
- 绕过第三方应用的定位检测；
- 自动完成学校签到、打卡或考勤；
- 伪造考勤结果。

因此它适合作为自己的 Android / 微信小程序定位功能测试工具，而不是用于绕过学校签到规则。

## 荣耀 Magic8

MagicOS 的系统菜单名称和入口可能随版本更新。首次使用时重点检查：

**设置 → 位置信息 / 定位服务**

确保系统定位已经开启，并按系统提示授予本应用定位权限。

如果你是在开发自己的微信小程序，可以使用本工具显示的实际系统坐标与小程序读取到的坐标进行对照测试。
