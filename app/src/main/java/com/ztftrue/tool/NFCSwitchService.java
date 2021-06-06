package com.ztftrue.tool;

import android.content.Context;
import android.graphics.drawable.Icon;
import android.service.quicksettings.Tile;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

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
        dealHeadsUp(tile, this);
    }

    public void dealHeadsUp(Tile tile, Context context) {
        tile.setState(Tile.STATE_UNAVAILABLE);
        tile.updateTile();
        Observable.create((ObservableOnSubscribe<Boolean>) emitter -> {
            if (!emitter.isDisposed()) {
                boolean o = getNFCEnable(context);
                emitter.onNext(o);
                if (o) {
                    SystemUtils.startCommand("svc nfc disable");
                } else {
                    SystemUtils.startCommand("svc nfc enable");
                }
                emitter.onNext(!o);
                emitter.onComplete();
            }
        }).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<Boolean>() {

            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onNext(@NonNull Boolean o) {
                if (o) {
                    tile.setState(Tile.STATE_ACTIVE);
                    // TODO
                    tile.setLabel("Turning off");
                } else {
                    tile.setState(Tile.STATE_INACTIVE);
                    tile.setLabel("Turning on");
                }
                tile.updateTile();
            }

            @Override
            public void onError(@NonNull Throwable e) {
                e.printStackTrace();
            }

            @Override
            public void onComplete() {
                tile.setLabel("NFC");
                tile.updateTile();
            }
        });
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
            tile.setState(Tile.STATE_ACTIVE);
        } else {
            tile.setState(Tile.STATE_INACTIVE);
        }
        tile.updateTile();
    }
}