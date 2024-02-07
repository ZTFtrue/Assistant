# Assistant

Assistant

![1](./ScreenShoot/1.png )
![NFC](./ScreenShoot/2.png )

1. Adaptive brightness, need Modify system settings permission(search in settings`Modify system settings`)
2. Enable or disable heads up (bubbles), need root.

------------------

1. 自动调节亮度, 需要修改系统设置权限(设置里可以搜索到`修改系统设置`)
2. 浮动通知, NFC 开关, 需要root权限

## Command

浮动通知

0 disable , 1 enable

```shell
adb shell settings put global heads_up_notifications_enabled 0
# settings put system screen_brightness_mode 1
# settings get system screen_brightness 10
```

Android12 快捷栏 显示 wifi 和 蜂窝按钮，只能在android12 使用

```shell
adb shell settings put secure sysui_qs_tiles "$(settings get secure sysui_qs_tiles),wifi,cell"
```

关闭横屏时候的旋转提示

```shell
adb shell settings put secure show_rotation_suggestions
```

关闭剪贴板提示

```shell
adb shell device_config put systemui clipboard_overlay_enabled false
```

NFC控制

```shell
adb shell svc nfc disable
```


## TODO

整合命令