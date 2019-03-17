package com.ssyanhuo.screenrotate;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;


import java.util.Timer;
import java.util.TimerTask;

public class BackendService extends Service {


    @Override

    public void onCreate() {

        super.onCreate();
        Log.v("RotateControl", "Service Started.");
        //发送通知，这里是安卓O及以上的方法
        //TODO 适配安卓O以下的通知发送
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("SERVICE_NOTIFICATION", "SERVICE_NOTIFICATION", NotificationManager.IMPORTANCE_UNSPECIFIED);
            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            nm.createNotificationChannel(channel);
            Notification.Builder mBuilder = new Notification.Builder(this, "SERVICE_NOTIFICATION");
            mBuilder.setContentTitle(getString(R.string.notification_title))
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentText(getString(R.string.notification_text))
                    .setTicker(getString(R.string.notification_title))
                    .setWhen(System.currentTimeMillis());
            Notification backEndNotification = mBuilder.build();
            startForeground(1, backEndNotification);
        } else {
            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            Notification.Builder mBuilder = new Notification.Builder(this);
            mBuilder.setContentTitle("test")
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentText("123")
                    .setTicker("123")
                    .setWhen(System.currentTimeMillis())
                    .setDefaults(Notification.DEFAULT_ALL);
            Notification backEndNotification = mBuilder.build();
            nm.notify(1, backEndNotification);
        }
        //showFloatingWindow();
        final FloatingWindow floatingwindow = new FloatingWindow();
        final OrientationEventListener rotateListener = new OrientationEventListener(this) {
            @Override
            public void onOrientationChanged(int orientation) {
                if ((((orientation >= 0) && (orientation < 15)) || (orientation > 345) )&& floatingwindow.nowRotate() != 0){//竖屏-0
                    switch (floatingwindow.mode){
                        case 0:
                            floatingwindow.mode = 1;
                            floatingwindow.nowRotate = 0;
                            Timer timerShowbutton = new Timer();
                            TimerTask timerShowbuttonTask = new TimerTask() {
                                @Override
                                public void run() {
                                    floatingwindow.mode = 2;
                                }
                            };
                            timerShowbutton.schedule(timerShowbuttonTask,1000);
                        case 1:
                            if(floatingwindow.nowRotate != 0){
                                floatingwindow.mode = 0;
                            }
                        case 2:
                            if(!floatingwindow.viable){
                                floatingwindow.showFloatingWindow(0);
                            }
                    }
                    if(floatingwindow.rotate != 0){
                        floatingwindow.hideFloatingWindow();
                    }
                    floatingwindow.rotate = 0;
                    Log.i("ScreenRotate", "竖屏");
                }else if((orientation > 255 && orientation < 285) && floatingwindow.nowRotate() != 1){//横屏-1
                    if(floatingwindow.rotate != 1){
                        floatingwindow.hideFloatingWindow();
                    }
                    floatingwindow.rotate = 1;
                    Log.i("ScreenRotate", "横屏");
                    if(!floatingwindow.viable){
                        floatingwindow.showFloatingWindow(1);
                    }
                }else if((orientation > 165 && orientation < 195) && floatingwindow.nowRotate() != 2){//反向竖屏-2
                    if(floatingwindow.rotate != 2){
                        floatingwindow.hideFloatingWindow();
                    }
                    floatingwindow.rotate = 2;
                    Log.i("ScreenRotate", "反向竖屏");
                    if(!floatingwindow.viable){
                        floatingwindow.showFloatingWindow(2);
                    }
                }else if((orientation > 75 && orientation < 105) && floatingwindow.nowRotate() != 3){//反向横屏-3
                    if(floatingwindow.rotate != 3){
                        floatingwindow.hideFloatingWindow();
                    }
                    floatingwindow.rotate = 3;
                    Log.i("ScreenRotate", "反向横屏");
                    if(!floatingwindow.viable){
                        floatingwindow.showFloatingWindow(3);
                    }
                }else {
                    floatingwindow.hideFloatingWindow();
                }

            }
        };
        rotateListener.enable();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.v("RotateControl", "Service Destroyed.");
    }
    public class FloatingWindow {
        final SharedPreferences userSettings= getSharedPreferences("com.ssyanhuo.screenrotate_preferences", 0);
        final Button button = new Button(getApplicationContext());
        final SharedPreferences.Editor editor = userSettings.edit();
        final WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        final WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        int rotate = 0;
        boolean viable = false;
        int nowRotate = 0;
        int mode = 0; //0-无状态；1-检测到方向改变但不一定稳定,如果不稳定将变为0，持续一秒变为2；2-检测到稳定的方向改变,再次改变方向或点击button变为0
        @SuppressLint("ClickableViewAccessibility")
        private void showFloatingWindow(final int pos){
            viable = true;
            mode = 0;
            // 新建悬浮窗控件
            button.setText("Floating Window");
            // 设置LayoutParam
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            } else {
                layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
            }
            layoutParams.format = PixelFormat.RGBA_8888;
            layoutParams.width = 500;
            layoutParams.height = 100;
            layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            layoutParams.x = userSettings.getInt("Floating_Window_X", 0);
            layoutParams.y = userSettings.getInt("Floating_Window_Y", 0);
            windowManager.addView(button, layoutParams);
            Log.i("ScreenRotate","P:"+String.valueOf(pos));
            Timer timer = new Timer();
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    hideFloatingWindow();
                }
            };
            timer.schedule(task,5000);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Settings.System.putInt(getContentResolver(),"user_rotation", pos);
                    hideFloatingWindow();
                }
            });
            button.setOnTouchListener(new View.OnTouchListener() {
                int x;
                int y;
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            x = (int) event.getRawX();
                            y = (int) event.getRawY();
                            break;
                        case MotionEvent.ACTION_UP:
                            Log.i("ssyanhuo","moved");
                            int nowX = (int) event.getRawX();
                            int nowY = (int) event.getRawY();
                            int movedX = nowX - x;
                            int movedY = nowY - y;
                            x = nowX;
                            y = nowY;
                            int newx = layoutParams.x + movedX;
                            int newy = layoutParams.y + movedY;
                            editor.putInt("Floating_Window_X",newx);
                            editor.putInt("Floating_Window_Y",newy);
                            editor.commit();
                            break;
                        case MotionEvent.ACTION_MOVE:
                            nowX = (int) event.getRawX();
                            nowY = (int) event.getRawY();
                            movedX = nowX - x;
                            movedY = nowY - y;
                            x = nowX;
                            y = nowY;
                            layoutParams.x = layoutParams.x + movedX;
                            layoutParams.y = layoutParams.y + movedY;
                            windowManager.updateViewLayout(button, layoutParams);
                            break;

                    }
                    return false;
                }
            });

        }
        private void hideFloatingWindow(){
            viable = false;
            mode = 0;
            try{
                windowManager.removeView(button);
            }catch (Exception e){
                Log.i("ScreenRotate","Floating Button not exists.");
            }
        }
        private int nowRotate(){
            return windowManager.getDefaultDisplay().getRotation();
        }
    }



}