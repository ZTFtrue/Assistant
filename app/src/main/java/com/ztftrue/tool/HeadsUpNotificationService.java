package com.ztftrue.tool;

import android.graphics.drawable.Icon;
import android.service.quicksettings.Tile;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;


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
        Tile tile = getQsTile();
        tile.setIcon(Icon.createWithResource(this,
                R.drawable.icon_notification_bel));
        dealHeadsUp(tile, true);
    }

    @Override
    public void onStartListening() {
        super.onStartListening();
        Tile tile = getQsTile();
        tile.setIcon(Icon.createWithResource(this,
                R.drawable.icon_notification_bel));
        dealHeadsUp(tile, true);
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
        dealHeadsUp(tile, false);
    }

    public void dealHeadsUp(Tile tile, boolean init) {
        Observable.create((ObservableOnSubscribe<Boolean>) emitter -> {
            if (!emitter.isDisposed()) {
                boolean o = SystemUtils.startCommand("settings get global heads_up_notifications_enabled").equals("1");
                if(init){
                    emitter.onNext(o);
                    emitter.onComplete();
                    return;
                }
                if (o) {
                    SystemUtils.startCommand("settings put global heads_up_notifications_enabled 0");
                } else {
                    SystemUtils.startCommand("settings put global heads_up_notifications_enabled 1");
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
                } else {
                    tile.setState(Tile.STATE_INACTIVE);
                }
                tile.updateTile();
            }

            @Override
            public void onError(@NonNull Throwable e) {
                e.printStackTrace();
            }

            @Override
            public void onComplete() {

            }
        });
    }


}