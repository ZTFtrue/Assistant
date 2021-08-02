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

0 disable , 1 enable

```bash
settings put global heads_up_notifications_enabled 0
# settings put system screen_brightness_mode 1
# settings get system screen_brightness 10
```
