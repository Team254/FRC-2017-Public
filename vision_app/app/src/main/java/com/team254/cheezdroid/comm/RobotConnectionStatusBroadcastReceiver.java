package com.team254.cheezdroid.comm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class RobotConnectionStatusBroadcastReceiver extends BroadcastReceiver {
    public static final String ACTION_ROBOT_CONNECTED = "action_robot_connected";
    public static final String ACTION_ROBOT_DISCONNECTED = "action_robot_disconnected";

    private RobotConnectionStateListener m_listener;

    public RobotConnectionStatusBroadcastReceiver(Context context, RobotConnectionStateListener listener) {
        this.m_listener = listener;

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_ROBOT_CONNECTED);
        intentFilter.addAction(ACTION_ROBOT_DISCONNECTED);
        context.registerReceiver(this, intentFilter);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ACTION_ROBOT_CONNECTED.equals(intent.getAction())) {
            m_listener.robotConnected();
        } else if (ACTION_ROBOT_DISCONNECTED.equals(intent.getAction())) {
            m_listener.robotDisconnected();
        }
    }
}
