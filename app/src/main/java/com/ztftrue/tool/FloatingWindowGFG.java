package com.ztftrue.tool;

import android.accessibilityservice.AccessibilityService;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Environment;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;
import androidx.lifecycle.MutableLiveData;

import java.io.File;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.core.SingleOnSubscribe;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class FloatingWindowGFG extends AccessibilityService implements LifecycleOwner {
    public static MutableLiveData<Boolean> isShowWindow = new MutableLiveData<>(false);
    final private LifecycleRegistry mLifecycleRegistry = new LifecycleRegistry(this);
    private Vibrator vibrator;

    public FloatingWindowGFG() {
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

    }

    @Override
    public void onInterrupt() {

    }

    boolean currentOrientationPortrait = true;

    @Override
    public void onCreate() {
        super.onCreate();
        mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        initGestureDetector();
        initObserve();
    }

    SensorEventListener m_sensorEventListener;
    Handler handler = new Handler();

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (floatView != null && layoutParamLandScape != null && windowManager != null && currentOrientationPortrait) {
                currentOrientationPortrait = false;
                currentLayoutParam = layoutParamLandScape;
                windowManager.updateViewLayout(floatView, currentLayoutParam);
            }
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            if (floatView != null && layoutParam != null && windowManager != null && !currentOrientationPortrait) {
                currentOrientationPortrait = true;
                currentLayoutParam = layoutParam;
                windowManager.updateViewLayout(floatView, currentLayoutParam);
            }
        }
    }

    /**
     * 打开关闭的订阅
     */
    private void initObserve() {
        handler.postDelayed(this::createFloatWindow, 1000);
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
        if (m_sensorEventListener != null) {
            SensorManager sm = (SensorManager) getSystemService(SENSOR_SERVICE);
            sm.unregisterListener(m_sensorEventListener);
        }
        mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY);
        windowManager.removeView(floatView);
        vibrator.cancel();
    }

    WindowManager windowManager;
    ViewGroup floatView;
    int viewWidth = 140;
    int viewHeight = 300;
    boolean isMoveButton = false;
    WindowManager.LayoutParams layoutParam;
    WindowManager.LayoutParams layoutParamLandScape;
    WindowManager.LayoutParams currentLayoutParam;

    @SuppressLint("ClickableViewAccessibility")
    public void createFloatWindow() {

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int width = metrics.widthPixels;
//        this.viewHeight = metrics.heightPixels / 5;
        windowManager = (WindowManager) FloatingWindowGFG.this.getSystemService(WINDOW_SERVICE);
        LayoutInflater inflater = (LayoutInflater) getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        floatView = (ViewGroup) inflater.inflate(R.layout.float_view_layout, null);
        layoutParam = new WindowManager.LayoutParams(
                viewWidth,
                viewHeight,
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSPARENT
        );
        layoutParamLandScape = new WindowManager.LayoutParams(
                80,
                width / 4,
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSPARENT
        );
        currentLayoutParam = layoutParam;
        layoutParamLandScape.gravity = Gravity.TOP | Gravity.START;
        layoutParamLandScape.x = 0;
        layoutParamLandScape.y = width / 3;
        layoutParam.gravity = Gravity.TOP | Gravity.START;
        layoutParam.x = 0;
        layoutParam.y = 1180;
        windowManager.getDefaultDisplay().getMetrics(metrics);

        floatView.setLayoutParams(new FrameLayout.LayoutParams(viewWidth, viewHeight));
        floatView.setOnTouchListener(new View.OnTouchListener() {
            int px = 0;
            int py = 0;

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    px = (int) event.getRawX();
                    py = (int) event.getRawY();
                }

                if (!isMoveButton) {
                    gestureDetector.onTouchEvent(event);
                } else {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_MOVE:
                            int nowX = (int) event.getRawX();
                            int nowY = (int) event.getRawY();
                            int movedX = nowX - px;
                            int movedY = nowY - py;
                            px = nowX;
                            py = nowY;
                            currentLayoutParam.x = currentLayoutParam.x + movedX;
                            currentLayoutParam.y = currentLayoutParam.y + movedY;

                            windowManager.updateViewLayout(floatView, currentLayoutParam);
                            break;
                        case MotionEvent.ACTION_UP:
                            if (currentLayoutParam.x > width / 2) {
                                currentLayoutParam.x = width - viewWidth;
                            } else {
                                currentLayoutParam.x = 0;
                            }
                            windowManager.updateViewLayout(v, currentLayoutParam);
                            isMoveButton = false;
                            break;
                    }
                }

                return true;
            }
        });
        windowManager.addView(floatView, currentLayoutParam);
    }

    GestureDetector gestureDetector;

    public void initGestureDetector() {
        gestureDetector = new GestureDetector(this, new GestureDetector.OnGestureListener() {
            @Override
            public boolean onDown(@NonNull MotionEvent e) {
                return false;
            }

            @Override
            public void onShowPress(@NonNull MotionEvent e) {
            }

            @Override
            public boolean onSingleTapUp(@NonNull MotionEvent e) {
                floatView.setVisibility(View.GONE);
                windowManager.updateViewLayout(floatView, currentLayoutParam);
                Single.create((SingleOnSubscribe<Boolean>) emitter -> {
                    SystemUtils.startCommand("input tap " + e.getRawX() + " " + e.getRawY());
                    emitter.onSuccess(true);
                }).subscribeOn(Schedulers.single()).observeOn(AndroidSchedulers.mainThread()).subscribe(new SingleObserver<>() {
                    @Override
                    public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {

                    }

                    @Override
                    public void onSuccess(@io.reactivex.rxjava3.annotations.NonNull Boolean aBoolean) {
                        handler.postDelayed(() -> {
                            floatView.setVisibility(View.VISIBLE);
                            windowManager.updateViewLayout(floatView, currentLayoutParam);
                        }, 300);
                    }

                    @Override
                    public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {

                    }
                });
                return false;
            }

            @Override
            public boolean onScroll(@NonNull MotionEvent e1, @NonNull MotionEvent e2, float distanceX, float distanceY) {
                return false;
            }

            @Override
            public void onLongPress(@NonNull MotionEvent e) {
                // Vibrate for 500 milliseconds
                VibrationEffect vibrationEffect = VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE);
                vibrator.vibrate(vibrationEffect);
                isMoveButton = true;
            }

            @Override
            public boolean onFling(@NonNull MotionEvent e1, @NonNull MotionEvent e2, float velocityX, float velocityY) {
                if (isMoveButton) {
                    return false;
                }
                if (Math.abs(e1.getX() - e2.getX()) > 100 && Math.abs(e1.getY() - e2.getY()) < 300) {
                    performGlobalAction(GLOBAL_ACTION_BACK);
                    // Back
//                    Completable.fromAction(() -> SystemUtils.startCommand("input keyevent 4"))
//                            .subscribeOn(Schedulers.single()).subscribe();
                } else if (Math.abs(e1.getX() - e2.getX()) < 200 && e2.getY() - e1.getY() > 300) {
                    //down
                    floatView.setVisibility(View.GONE);
                    String filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) +
                            File.separator + System.currentTimeMillis() + ".png";
                    Single.create((SingleOnSubscribe<Boolean>) emitter -> {
                        SystemUtils.startCommand("screencap -p " + filePath
                        );
//                        SystemUtils.startCommand("su -lp 2000 -c \"cmd notification post -S bigtext -t 'Screen Capture' 'Tag' 'Ok'\"");
                        emitter.onSuccess(true);
                    }).subscribeOn(Schedulers.single()).observeOn(AndroidSchedulers.mainThread()).subscribe(new SingleObserver<>() {
                        @Override
                        public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {

                        }

                        @Override
                        public void onSuccess(@io.reactivex.rxjava3.annotations.NonNull Boolean aBoolean) {
                            floatView.setVisibility(View.VISIBLE);
                            CreateNotification createNotification = new CreateNotification();
                            createNotification
                                    .createForegroundNotification(FloatingWindowGFG.this,
                                            "Screen Capture", filePath);
                            handler.postDelayed(() -> {
                                createNotification.cancelNotification(FloatingWindowGFG.this);
                            }, 1000);
                        }

                        @Override
                        public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {

                        }
                    });

                } else if (Math.abs(e1.getX() - e2.getX()) < 200 && e1.getY() - e2.getY() > 300) {
                    performGlobalAction(GLOBAL_ACTION_HOME);
                    Completable.fromAction(() -> SystemUtils.startCommand("input keyevent 3"))
                            .subscribeOn(Schedulers.single()).subscribe();
                }
                return true;
            }
        });
    }

    @NonNull
    @Override
    public Lifecycle getLifecycle() {
        return mLifecycleRegistry;
    }
}