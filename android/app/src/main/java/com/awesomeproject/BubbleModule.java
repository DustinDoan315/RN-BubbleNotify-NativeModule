
package com.awesomeproject;


import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Promise;
import com.facebook.react.ReactActivity;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.bridge.ReactContextBaseJavaModule;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.graphics.drawable.Icon;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import android.content.Context;
import androidx.core.content.ContextCompat;
import androidx.core.app.NotificationManagerCompat;
import android.app.NotificationChannel;
import androidx.core.app.Person;
import androidx.core.graphics.drawable.IconCompat;
import android.view.GestureDetector;
import android.graphics.Point;
import android.view.Display;
import android.widget.TextView;
import android.util.Log;
import android.app.Activity;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.animation.ObjectAnimator;
import android.graphics.Color;

public class BubbleModule extends ReactContextBaseJavaModule {
    private WindowManager mWindowManager;
    private View mChatHeadView;
    private boolean isListen = false;
    private String type = "store";
    private int count = 0;
    private Promise promise;
    boolean isShowNotify = false;
    Context context;

    public BubbleModule(ReactApplicationContext reactContext) {
        super(reactContext);
        context = reactContext;
    }

    @Override
    public String getName() {
        return "BubbleModule";
    }

    private void updateBooleanVariable(boolean newValue) {
        isListen = newValue;
    }

    private boolean hasOverlayPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        return Settings.canDrawOverlays(getReactApplicationContext());
    }

    private static final int OVERLAY_PERMISSION_REQUEST_CODE = 100;
    private boolean isOverlayPermissionRequested = false;

    private boolean checkDrawOverlayPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }

        if (!Settings.canDrawOverlays(getReactApplicationContext())) {
            if (!isOverlayPermissionRequested) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                intent.setData(Uri.parse("package:" + getReactApplicationContext().getPackageName()));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                Activity currentActivity = getCurrentActivity();
                if (currentActivity != null) {
                    currentActivity.startActivityForResult(intent, OVERLAY_PERMISSION_REQUEST_CODE);
                    isOverlayPermissionRequested = true;
                }
            }

            return false;
        }

        return true;
    }

    @ReactMethod
    public void onCreate() {
        updateBooleanVariable(true);
        mWindowManager = (WindowManager) getReactApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = mWindowManager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int deviceWidth = size.x;
        int deviceHeight = size.y;

        if (mChatHeadView == null) {
            mChatHeadView = LayoutInflater.from(getReactApplicationContext()).inflate(R.layout.layout_chat_head, null);
        }

        if (!checkDrawOverlayPermission()) {
            return;
        }

        WritableMap eventData = Arguments.createMap();
        eventData.putString("showBubble", "show Bubble successfully");
        getReactApplicationContext()
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit("isShow", eventData);

        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                        : WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.TOP | Gravity.LEFT;

        params.x = 0;
        params.y = (int) (deviceHeight / 2.5);

        final ImageView chatHeadImage = mChatHeadView.findViewById(R.id.chat_head_profile_iv);

        final GestureDetector gestureDetector = new GestureDetector(context,
                new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onDown(MotionEvent e) {
                        return true;
                    }

                    private ImageView chatHeadProfileImageView;
                    private float initialAlpha = 1f;

                    // private void decreaseOpacity() {
                    // if (chatHeadProfileImageView != null) {
                    // ObjectAnimator alphaAnimator =
                    // ObjectAnimator.ofFloat(chatHeadProfileImageView, "alpha",
                    // initialAlpha, 0.5f);
                    // alphaAnimator.setDuration(500);
                    // alphaAnimator.start();
                    // }
                    // }

                    @Override
                    public boolean onSingleTapUp(MotionEvent e) {
                        Intent intent = new Intent(getReactApplicationContext(), BubbleActivity.class);
                        intent.putExtra("myData", "Hello from Android Native Module");
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        PendingIntent pendingIntent = PendingIntent.getActivity(getReactApplicationContext(), 0,
                                intent, PendingIntent.FLAG_IMMUTABLE);

                        try {
                            pendingIntent.send();
                        } catch (PendingIntent.CanceledException ex) {
                            ex.printStackTrace();
                        }
                        WritableMap eventData = Arguments.createMap();
                        eventData.putString("myData", type);
                        getReactApplicationContext()
                                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                                .emit("NOTIFY", eventData);

                        return true;
                    }

                    @Override
                    public void onLongPress(MotionEvent e) {

                    }

                    @Override
                    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                        return true;
                    }
                });
        chatHeadImage.setOnTouchListener(new View.OnTouchListener() {
            private int lastAction;
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;

                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        lastAction = event.getAction();
                        return gestureDetector.onTouchEvent(event);

                    case MotionEvent.ACTION_UP:

                        int centerWidth = deviceWidth / 2;

                        if (params.x < centerWidth) {
                            while (params.x > 0) {
                                params.x -= 10;
                                mWindowManager.updateViewLayout(mChatHeadView, params);
                            }
                        } else {
                            while (params.x < deviceWidth) {
                                params.x += 10;
                                mWindowManager.updateViewLayout(mChatHeadView, params);
                            }
                        }

                        if (lastAction == MotionEvent.ACTION_DOWN) {
                            Intent intent = new Intent(getReactApplicationContext(), BubbleActivity.class);
                            intent.putExtra("myData", "Hello from Android Native Module");
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            PendingIntent pendingIntent = PendingIntent.getActivity(getReactApplicationContext(), 0,
                                    intent, PendingIntent.FLAG_IMMUTABLE);

                            try {
                                pendingIntent.send();
                            } catch (PendingIntent.CanceledException e) {
                                e.printStackTrace();
                            }

                            WritableMap eventData = Arguments.createMap();
                            eventData.putString("myData", "Hello from Android Native Module");
                            getReactApplicationContext()
                                    .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                                    .emit("NOTIFY", eventData);
                        }

                        lastAction = event.getAction();
                        return gestureDetector.onTouchEvent(event);
                    case MotionEvent.ACTION_MOVE:

                        int xDiff = (int) (event.getRawX() - initialTouchX);
                        int yDiff = (int) (event.getRawY() - initialTouchY);

                        params.x = initialX + xDiff;
                        params.y = initialY + yDiff;

                        if (params.x < 0) {
                            params.x = 0;
                        }
                        if (params.y < 0) {
                            params.y = 0;
                        }

                        mWindowManager.updateViewLayout(mChatHeadView, params);
                        lastAction = event.getAction();
                        return true;
                    default:
                        return false;
                }
            }
        });

        if (hasOverlayPermission()) {
            mWindowManager.addView(mChatHeadView, params);
        } else {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getReactApplicationContext().getPackageName()));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getReactApplicationContext().startActivity(intent);
        }
    }

    @ReactMethod
    public void onReceiveNotify(String notify, String typeMess) {
        Log.e("Bubble Notify", notify);
        Activity currentActivity = getCurrentActivity();
        if (currentActivity == null) {
            Log.e("BubbleModule", "Current activity is null");
            return;
        }

        WindowManager windowManager = (WindowManager) currentActivity.getSystemService(Context.WINDOW_SERVICE);

        if (mChatHeadView == null) {
            mChatHeadView = LayoutInflater.from(currentActivity).inflate(R.layout.layout_chat_head, null);

            WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);
        }

        final TextView textView = mChatHeadView.findViewById(R.id.content_notify_view_id);
        final TextView textCount = mChatHeadView.findViewById(R.id.text_view_id);

        if (textView != null) {
            Log.e("BubbleModule", notify);
            type = typeMess;
            if (notify != null && !notify.equals("0")) {
                textView.setVisibility(View.VISIBLE);
                textView.setText(notify);
                count += 1;
                if (count != 0) {
                    textCount.setVisibility(View.VISIBLE);
                    textCount.setText(String.valueOf(count));
                } else {
                    textCount.setVisibility(View.GONE);
                }

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        textView.setVisibility(View.GONE);
                    }
                }, 15000);
            } else {
                textView.setVisibility(View.GONE);
                textCount.setVisibility(View.GONE);
            }
        } else {
            Log.e("BubbleModule", "TextView is null");
        }
    }

    @ReactMethod
    public void resetCounter() {
        count = 0;
        onReceiveNotify("0", "0");
    }

    @ReactMethod
    public void getIsListen() {
        WritableMap eventData = Arguments.createMap();
        eventData.putString("isListen", Boolean.toString(isListen));
        eventData.putString("typeMess", type);
        getReactApplicationContext()
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit("LISTEN", eventData);
    }

    @ReactMethod
    public void hideChatHead() {
        if (mChatHeadView != null && mChatHeadView.getParent() != null) {
            updateBooleanVariable(false);
            type = "store";
            count = 0;
            mWindowManager.removeView(mChatHeadView);
        }
    }
}