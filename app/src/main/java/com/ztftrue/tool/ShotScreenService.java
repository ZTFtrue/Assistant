package com.ztftrue.tool;

import android.content.Context;
import android.graphics.drawable.Icon;
import android.os.Environment;
import android.service.quicksettings.Tile;

import java.io.File;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class ShotScreenService extends android.service.quicksettings.TileService {

    public ShotScreenService() {
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
    public void onClick() {
        super.onClick();
        Tile tile = getQsTile();
        dealHeadsUp(tile, this);
    }

    public void dealHeadsUp(Tile tile, Context context) {

        Observable.create((ObservableOnSubscribe<Boolean>) emitter -> {
            if (!emitter.isDisposed()) {
                File file= Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                if(!file.exists()){
                    file.mkdirs();
                }
                SystemUtils.startCommand("service call statusbar 2&&sleep 0.5s&&screencap -p " +
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) +
                        File.separator + System.currentTimeMillis() + ".png&&su -lp 2000 -c \"cmd notification post -S bigtext -t 'Screen' 'Tag' 'Ok'\"");
                emitter.onComplete();
            }
        }).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<Boolean>() {

            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onNext(@NonNull Boolean o) {
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


    public void initTile() {
        Tile tile = getQsTile();
        tile.setIcon(Icon.createWithResource(this,
                R.drawable.cut_screen));
        tile.setState(Tile.STATE_ACTIVE);
        tile.updateTile();
    }
}