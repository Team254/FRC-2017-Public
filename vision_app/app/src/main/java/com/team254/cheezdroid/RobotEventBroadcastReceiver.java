package com.team254.cheezdroid;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;


public class RobotEventBroadcastReceiver extends BroadcastReceiver {
    public static final String ACTION_SHOT_TAKEN = "ACTION_SHOT_TAKEN";
    public static final String ACTION_WANT_VISION = "ACTION_WANT_VISION";
    public static final String ACTION_WANT_INTAKE = "ACTION_WANT_INTAKE";


    private RobotEventListener m_listener;

    public RobotEventBroadcastReceiver(Context context, RobotEventListener listener) {
        this.m_listener = listener;

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_SHOT_TAKEN);
        intentFilter.addAction(ACTION_WANT_VISION);
        intentFilter.addAction(ACTION_WANT_INTAKE);
        context.registerReceiver(this, intentFilter);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ACTION_SHOT_TAKEN.equals(intent.getAction())) {
            m_listener.shotTaken();
        }
        if (ACTION_WANT_VISION.equals(intent.getAction())) {
            m_listener.wantsVisionMode();
        }
        if (ACTION_WANT_INTAKE.equals(intent.getAction())) {
            m_listener.wantsIntakeMode();
        }
    }
}
