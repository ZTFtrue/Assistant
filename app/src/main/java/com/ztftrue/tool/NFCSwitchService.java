package com.ztftrue.tool;

import android.content.Context;
import android.graphics.drawable.Icon;
import android.service.quicksettings.Tile;

public class NFCSwitchService extends android.service.quicksettings.TileService {

    public NFCSwitchService() {
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
                R.drawable.ic_nfc));
        if (getNFCEnable(this)) {
            // 执行关闭
            tile.setState(Tile.STATE_INACTIVE);
            SystemUtils.startCommand("svc nfc disable");
        } else {
            tile.setState(Tile.STATE_ACTIVE);
            SystemUtils.startCommand("svc nfc enable");
        }
        tile.updateTile();
    }

    public boolean getNFCEnable(Context context) {
        android.nfc.NfcAdapter mNfcAdapter = android.nfc.NfcAdapter.getDefaultAdapter(context);
        return mNfcAdapter.isEnabled();
    }

    public void initTile() {
        Tile tile = getQsTile();
        tile.setIcon(Icon.createWithResource(this,
                R.drawable.ic_nfc));
        if (getNFCEnable(this)) {
            tile.setState(Tile.STATE_INACTIVE);
            SystemUtils.startCommand("svc nfc disable");
        } else {
            tile.setState(Tile.STATE_ACTIVE);
            SystemUtils.startCommand("svc nfc enable");
        }
        tile.updateTile();
    }
}