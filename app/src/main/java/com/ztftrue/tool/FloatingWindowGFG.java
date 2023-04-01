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
import androidx.lifecycle.Observer;

import java.io.File;
import java.io.IOException;

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
Handler handler= new Handler();
    /**
     * 打开关闭的订阅
     */
    private void initObserve() {
        handler .postDelayed(new Runnable() {
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
        int viewWidth = 120;
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;
        windowManager = (WindowManager) FloatingWindowGFG.this.getSystemService(WINDOW_SERVICE);
        LayoutInflater inflater = (LayoutInflater) getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        floatView = (ViewGroup) inflater.inflate(R.layout.float_view_layout, null);
        WindowManager.LayoutParams layoutParam = new WindowManager.LayoutParams(
                viewWidth,
                140,
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSPARENT
        );
        layoutParam.gravity = Gravity.START;
        layoutParam.x = 0;
        layoutParam.y = 0;
        windowManager.getDefaultDisplay().getMetrics(metrics);
        floatView.setLayoutParams(new FrameLayout.LayoutParams(viewWidth, 250));
        floatView.setOnClickListener(v -> {
            windowManager.removeView(v);
            try {
                SystemUtils.startCommand("screencap -p " +
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) +
                        File.separator + System.currentTimeMillis() + ".png");
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        windowManager.addView(v, layoutParam);
                    }
                },800);
            } catch (InterruptedException | IOException e) {
                throw new RuntimeException(e);
            }
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