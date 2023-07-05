package com.ztftrue.tool;

import android.accessibilityservice.AccessibilityService;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.service.quicksettings.Tile;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;
import androidx.lifecycle.MutableLiveData;

import java.io.File;
import java.io.IOException;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.FlowableEmitter;
import io.reactivex.rxjava3.core.FlowableOnSubscribe;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleEmitter;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.core.SingleOnSubscribe;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.internal.operators.single.SingleToObservable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class FloatingWindowGFG extends AccessibilityService implements LifecycleOwner {
    public static MutableLiveData<Boolean> isShowWindow = new MutableLiveData<Boolean>(false);
    final private LifecycleRegistry mLifecycleRegistry = new LifecycleRegistry(this);

    public FloatingWindowGFG() {
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

    }

    @Override
    public void onInterrupt() {

    }


    @Override
    public void onCreate() {
        super.onCreate();
        mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE);
        initObserve();
    }

    Handler handler = new Handler();

    /**
     * 打开关闭的订阅
     */
    private void initObserve() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                createFloatWindow();
            }
        }, 1000);
        isShowWindow.observe(this, aBoolean -> {
            if (aBoolean) {
                if (floatView == null)
                    createFloatWindow();
            } else {
                windowManager.removeView(floatView);
            }
        });

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY);
        windowManager.removeView(floatView);
    }

    WindowManager windowManager;
    ViewGroup floatView;

    @SuppressLint("ClickableViewAccessibility")
    public void createFloatWindow() {
        int viewWidth = 150;
        int viewHeight = 150;
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;
        windowManager = (WindowManager) FloatingWindowGFG.this.getSystemService(WINDOW_SERVICE);
        LayoutInflater inflater = (LayoutInflater) getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        floatView = (ViewGroup) inflater.inflate(R.layout.float_view_layout, null);
        WindowManager.LayoutParams layoutParam = new WindowManager.LayoutParams(
                viewWidth,
                viewHeight,
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSPARENT
        );
        layoutParam.gravity = Gravity.START;
        layoutParam.x = 0;
        layoutParam.y = 0;
        windowManager.getDefaultDisplay().getMetrics(metrics);
        floatView.setLayoutParams(new FrameLayout.LayoutParams(viewWidth, viewHeight));
        floatView.setOnClickListener(v -> {
            v.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.trasparent_color, null));
            windowManager.updateViewLayout(v, layoutParam);
            Single.create((SingleOnSubscribe<Boolean>) emitter -> {
                SystemUtils.startCommand("screencap -p " +
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) +
                        File.separator + System.currentTimeMillis() + ".png");
                emitter.onSuccess(true);
            }).subscribeOn(Schedulers.single()).observeOn(AndroidSchedulers.mainThread()).subscribe(new SingleObserver<Boolean>() {
                @Override
                public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {

                }

                @Override
                public void onSuccess(@io.reactivex.rxjava3.annotations.NonNull Boolean aBoolean) {
                    v.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.radius_color, null));
                    Toast.makeText(v.getContext(), "s", Toast.LENGTH_SHORT).show();
                    windowManager.updateViewLayout(v, layoutParam);
                }

                @Override
                public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {

                }
            });
        });
        floatView.setOnTouchListener(new View.OnTouchListener() {

            int px;
            int py;
            long downTime;

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        px = (int) event.getRawX();
                        py = (int) event.getRawY();
                        downTime = System.currentTimeMillis();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        int nowX = (int) event.getRawX();
                        int nowY = (int) event.getRawY();
                        int movedX = nowX - px;
                        int movedY = nowY - py;
                        px = nowX;
                        py = nowY;
                        layoutParam.x = layoutParam.x + movedX;
                        layoutParam.y = layoutParam.y + movedY;
                        windowManager.updateViewLayout(floatView, layoutParam);
                        break;
                    case MotionEvent.ACTION_UP:
                        int nowXUp = (int) event.getRawX();
                        int nowYUp = (int) event.getRawX();
                        if (System.currentTimeMillis() - downTime < ViewConfiguration.getTapTimeout()) {
                            v.performClick();
                        } else {
                            if (width != 0) {
                                if (layoutParam.x > width / 2) {
                                    layoutParam.x = width - v.getWidth();
                                } else {
                                    layoutParam.x = 0;
                                }
                                windowManager.updateViewLayout(v, layoutParam);
                            }
                        }
                        break;
                }
                return true;
            }
        });
        windowManager.addView(floatView, layoutParam);
    }

    @NonNull
    @Override
    public Lifecycle getLifecycle() {
        return mLifecycleRegistry;
    }
}