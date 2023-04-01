package com.ztftrue.tool;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.IOException;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkAccessibilityPermission(this, false);
    }

    /**
     * 跳转到设置页面申请打开无障碍辅助功能
     */
    private void accessibilityToSettingPage(Context context) {
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    private void checkAccessibilityPermission(Activity context, boolean auto) {
        if (isMyServiceRunning()) {
            FloatingWindowGFG.isShowWindow.postValue(true);
        } else if (auto) {
            accessibilityToSettingPage(context);
        }
    }

    private boolean isMyServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            // If this service is found as a running, it will return true or else false.
            if (FloatingWindowGFG.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }


    public void onClick(View view) {
        if (view.getId() == R.id.sudo) {
            checkSudo();
        } else if (view.getId() == R.id.setting) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
            startActivity(intent);
        } else if (view.getId() == R.id.wifi_cell) {
            executeCommand("settings put secure sysui_qs_tiles \"$(settings get secure sysui_qs_tiles),wifi,cell\"");
        } else if (view.getId() == R.id.float_window) {
            checkAccessibilityPermission(MainActivity.this,true);
//            FloatingWindowGFG.isShowWindow.postValue(true);
        } else if (view.getId() == R.id.close_screen_orientation) {
//            https://source.android.com/docs/core/display/rotate-suggestions?hl=zh-cn
            executeCommand("settings put secure show_rotation_suggestions");
        }
    }

    public void executeCommand(String command) {
        Observable.create((ObservableOnSubscribe<Boolean>) emitter -> {
            SystemUtils.startCommand(command);
            emitter.onComplete();
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

    public void checkSudo() {
        Observable.create((ObservableOnSubscribe<Boolean>) emitter -> {
            if (!emitter.isDisposed()) {
                String a = SystemUtils.startCommand("ls");
                emitter.onNext(!TextUtils.isEmpty(a));
                emitter.onComplete();
            }
        }).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<Boolean>() {

            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onNext(@NonNull Boolean o) {
                if (o) {
                    Toast.makeText(MainActivity.this, "Get sudo granted", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(MainActivity.this, "No sudo granted", Toast.LENGTH_LONG).show();

                }
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