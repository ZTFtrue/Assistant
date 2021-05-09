package com.ztftrue.tool;

import android.graphics.drawable.Icon;
import android.service.quicksettings.Tile;

public class HeadsUpNotificationService extends android.service.quicksettings.TileService {

    public HeadsUpNotificationService() {
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    @Override
    public void onTileAdded() {
        super.onTileAdded();
        initTile();
    }

    @Override
    public void onStartListening() {
        super.onStartListening();
        initTile();
    }

    @Override
    public void onStopListening() {
        super.onStopListening();
    }

    @Override
    public void onTileRemoved() {
        super.onTileRemoved();
    }

    @Override
    public void onClick() {
        super.onClick();
        Tile tile = getQsTile();
        tile.setIcon(Icon.createWithResource(this,
                R.drawable.icon_notification_bel));
        if (getHeadsUpEnable()) {
            // 执行关闭
            tile.setState(Tile.STATE_INACTIVE);
            SystemUtils.startCommand("settings put global heads_up_notifications_enabled 0");
        } else {
            tile.setState(Tile.STATE_ACTIVE);
            SystemUtils.startCommand("settings put global heads_up_notifications_enabled 1");
        }
        tile.updateTile();
    }

    public boolean getHeadsUpEnable() {
        return SystemUtils.startCommand("settings get global heads_up_notifications_enabled").equals("1");
    }

    public void initTile() {
        Tile tile = getQsTile();
        tile.setIcon(Icon.createWithResource(this,
                R.drawable.icon_notification_bel));
        if (getHeadsUpEnable()) {
            tile.setState(Tile.STATE_INACTIVE);
            SystemUtils.startCommand("settings put global heads_up_notifications_enabled 0");
        } else {
            tile.setState(Tile.STATE_ACTIVE);
            SystemUtils.startCommand("settings put global heads_up_notifications_enabled 1");
        }
        tile.updateTile();
    }
}