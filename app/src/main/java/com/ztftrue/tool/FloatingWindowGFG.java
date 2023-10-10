package com.ztftrue.tool;

import android.accessibilityservice.AccessibilityService;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Environment;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.util.Log;
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
import androidx.core.content.res.ResourcesCompat;
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
    public static MutableLiveData<Boolean> isShowWindow = new MutableLiveData<Boolean>(false);
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


    @Override
    public void onCreate() {
        super.onCreate();
        mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        initGestureDetector();
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
        vibrator.cancel();
    }

    WindowManager windowManager;
    ViewGroup floatView;
    int viewWidth = 200;
    int viewHeight = 600;
    boolean isMoveButton = false;
    WindowManager.LayoutParams layoutParam;
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
        layoutParam.gravity = Gravity.TOP | Gravity.START;
        layoutParam.x = 0;
        layoutParam.y = 0;
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
                            layoutParam.x = layoutParam.x + movedX;
                            layoutParam.y = layoutParam.y + movedY;

                            windowManager.updateViewLayout(floatView, layoutParam);
                            break;
                        case MotionEvent.ACTION_UP:
                            if (layoutParam.x > width / 2) {
                                layoutParam.x = width - viewWidth;
                            } else {
                                layoutParam.x = 0;
                            }
                            windowManager.updateViewLayout(v, layoutParam);
                            isMoveButton = false;
                            break;
                    }
                }

                return true;
            }
        });
        windowManager.addView(floatView, layoutParam);
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
                Log.d("TAG", "onShowPress");
            }

            @Override
            public boolean onSingleTapUp(@NonNull MotionEvent e) {
                floatView.setVisibility(View.GONE);
                windowManager.updateViewLayout(floatView, layoutParam);
                Single.create((SingleOnSubscribe<Boolean>) emitter -> {
                    SystemUtils.startCommand("input tap "+e.getRawX()+" "+e.getRawY());
                    emitter.onSuccess(true);
                }).subscribeOn(Schedulers.single()).observeOn(AndroidSchedulers.mainThread()).subscribe(new SingleObserver<Boolean>() {
                    @Override
                    public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {

                    }

                    @Override
                    public void onSuccess(@io.reactivex.rxjava3.annotations.NonNull Boolean aBoolean) {
                        floatView.setVisibility(View.VISIBLE);
                        windowManager.updateViewLayout(floatView, layoutParam);
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
                Log.d("TAG", "Long press");
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
                    // Back
                    Completable.fromAction(() -> SystemUtils.startCommand("input keyevent 4"))
                            .subscribeOn(Schedulers.single()).subscribe();
                } else if (Math.abs(e1.getX() - e2.getX()) < 200 && e2.getY() - e1.getY() > 300) {
                    //down
                    floatView.setVisibility(View.GONE);
//                    floatView.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.trasparent_color, null));
//                    windowManager.updateViewLayout(floatView, layoutParam);
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
                            floatView.setVisibility(View.VISIBLE);
//                            floatView.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.radius_color, null));
//                            windowManager.updateViewLayout(floatView, layoutParam);
                        }

                        @Override
                        public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {

                        }
                    });

                } else if (Math.abs(e1.getX() - e2.getX()) < 200 && e1.getY() - e2.getY() > 300) {

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