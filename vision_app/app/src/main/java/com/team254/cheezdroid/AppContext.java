package com.team254.cheezdroid;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.PowerManager;

import com.team254.cheezdroid.comm.RobotConnection;

import java.util.Arrays;

public class AppContext extends Application {

    private AppContext instance;
    private PowerManager.WakeLock wakeLock;
    private OnScreenOffReceiver onScreenOffReceiver;


    // This class is mainly here so we can get references to the application context in places where
    // it is otherwise extremely hairy to do so. USE SPARINGLY.
    private static AppContext app;

    private RobotConnection rc;

    public AppContext() {
        super();
        app = this;
    }


    public static Context getDefaultContext() {
        return app.getApplicationContext();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        rc = new RobotConnection(getDefaultContext());
        rc.start();
        registerKioskModeScreenOffReceiver();
    }

    public static RobotConnection getRobotConnection() {
        return app.rc;
    }

    private void registerKioskModeScreenOffReceiver() {
        // register screen off receiver
        final IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        onScreenOffReceiver = new OnScreenOffReceiver();
        registerReceiver(onScreenOffReceiver, filter);
    }

    public PowerManager.WakeLock getWakeLock() {
        if(wakeLock == null) {
            // lazy loading: first call, create wakeLock via PowerManager.
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "wakeup");
        }
        return wakeLock;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        rc.stop();
    }
}