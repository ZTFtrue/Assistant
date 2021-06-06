package com.ztftrue.tool;

import android.graphics.drawable.Icon;
import android.provider.Settings;
import android.service.quicksettings.Tile;

public class BrightnessModeService extends android.service.quicksettings.TileService {

    public BrightnessModeService() {
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
        if (!Settings.System.canWrite(this)) {
            return;
        }
        Tile tile = getQsTile();
        tile.setIcon(Icon.createWithResource(this,
                R.drawable.ic_auto_brightness));
        if (getAutoEnable()) {
            tile.setState(Tile.STATE_INACTIVE);
            tile.updateTile();
            android.provider.Settings.System.putInt(getContentResolver(), android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE, android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
        } else {
            tile.setState(Tile.STATE_ACTIVE);
            tile.updateTile();
            android.provider.Settings.System.putInt(getContentResolver(), android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
        }
    }

    public boolean getAutoEnable() {
        int brightnessMode = 0;
        try {
            brightnessMode = android.provider.Settings.System.getInt(getContentResolver(), android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        return brightnessMode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
    }

    public void initTile() {
        Tile tile = getQsTile();
        tile.setIcon(Icon.createWithResource(this,
                R.drawable.ic_auto_brightness));
        if (getAutoEnable()) {
            tile.setState(Tile.STATE_ACTIVE);
        } else {
            tile.setState(Tile.STATE_INACTIVE);
        }
        tile.updateTile();
    }
}