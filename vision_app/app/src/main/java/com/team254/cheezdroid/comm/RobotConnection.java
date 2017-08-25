package com.team254.cheezdroid.comm;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.team254.cheezdroid.RobotEventBroadcastReceiver;
import com.team254.cheezdroid.comm.messages.HeartbeatMessage;
import com.team254.cheezdroid.comm.messages.OffWireMessage;
import com.team254.cheezdroid.comm.messages.VisionMessage;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

public class RobotConnection {
    public static final int K_ROBOT_PORT = 8254;
    public static final String K_ROBOT_PROXY_HOST = "localhost";
    public static final int K_CONNECTOR_SLEEP_MS = 100;
    public static final int K_THRESHOLD_HEARTBEAT = 800;
    public static final int K_SEND_HEARTBEAT_PERIOD = 100;

    private int m_port;
    private String m_host;
    private Context m_context;
    private boolean m_running = true;
    private boolean m_connected = false;
    volatile private Socket m_socket;
    private Thread m_connect_thread, m_read_thread, m_write_thread;

    private long m_last_heartbeat_sent_at = System.currentTimeMillis();
    private long m_last_heartbeat_rcvd_at = 0;

    private ArrayBlockingQueue<VisionMessage> mToSend = new ArrayBlockingQueue<VisionMessage>(30);

    protected class WriteThread implements Runnable {

        @Override
        public void run() {
            while (m_running) {
                VisionMessage nextToSend = null;
                try {
                    nextToSend = mToSend.poll(250, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    Log.e("WriteThead", "Couldn't poll queue");
                }
                if (nextToSend == null) {
                    continue;
                }
                sendToWire(nextToSend);
            }
        }
    }

    protected class ReadThread implements Runnable {

        public void handleMessage(VisionMessage message) {
            if ("heartbeat".equals(message.getType())) {
                m_last_heartbeat_rcvd_at = System.currentTimeMillis();
            }
            if ("shot".equals(message.getType())) {
                broadcastShotTaken();
            }
            if ("camera_mode".equals(message.getType())) {
                if ("vision".equals(message.getMessage())) {
                    broadcastWantVisionMode();
                } else if ("intake".equals(message.getMessage())) {
                    broadcastWantIntakeMode();
                }
            }

            Log.w("Connection" , message.getType() + " " + message.getMessage());
        }

        @Override
        public void run() {
            while (m_running) {
                if (m_socket != null || m_connected) {
                    BufferedReader reader;
                    try {
                        InputStream is = m_socket.getInputStream();
                        reader = new BufferedReader(new InputStreamReader(is));
                    } catch (IOException e) {
                        Log.e("ReadThread", "Could not get input stream");
                        continue;
                    } catch (NullPointerException npe) {
                        Log.e("ReadThread", "socket was null");
                        continue;
                    }
                    String jsonMessage = null;
                    try {
                        jsonMessage = reader.readLine();
                    } catch (IOException e) {
                    }
                    if (jsonMessage != null) {
                        OffWireMessage parsedMessage = new OffWireMessage(jsonMessage);
                        if (parsedMessage.isValid()) {
                            handleMessage(parsedMessage);
                        }
                    }
                } else {
                    try {
                        Thread.sleep(100, 0);
                    } catch (InterruptedException e) {
                    }
                }
            }
        }
    }

    protected class ConnectionMonitor implements Runnable {

        @Override
        public void run() {
            while (m_running) {
                try {
                    if (m_socket == null || !m_socket.isConnected() && !m_connected) {
                        tryConnect();
                        Thread.sleep(250, 0);
                    }

                    long now = System.currentTimeMillis();

                    if (now - m_last_heartbeat_sent_at > K_SEND_HEARTBEAT_PERIOD) {
                        send(HeartbeatMessage.getInstance());
                        m_last_heartbeat_sent_at = now;
                    }

                    if (Math.abs(m_last_heartbeat_rcvd_at - m_last_heartbeat_sent_at) > K_THRESHOLD_HEARTBEAT && m_connected) {
                        m_connected = false;
                        broadcastRobotDisconnected();
                        broadcastWantVisionMode();
                    }
                    if (Math.abs(m_last_heartbeat_rcvd_at - m_last_heartbeat_sent_at) < K_THRESHOLD_HEARTBEAT && !m_connected) {
                        m_connected = true;
                        broadcastRobotConnected();
                    }

                    Thread.sleep(K_CONNECTOR_SLEEP_MS, 0);
                } catch (InterruptedException e) {
                }
            }

        }
    }

    public RobotConnection(Context context, String host, int port) {
        m_context = context;
        m_host = host;
        m_port = port;
    }

    public RobotConnection(Context context) {
        this(context, K_ROBOT_PROXY_HOST, K_ROBOT_PORT);
    }

    synchronized private void tryConnect() {
        if (m_socket == null) {
            try {
                m_socket = new Socket(m_host, m_port);
                m_socket.setSoTimeout(100);
            } catch (IOException e) {
                Log.w("RobotConnector", "Could not connect");
                m_socket = null;
            }
        }
    }

    synchronized public void stop() {
        m_running = false;
        if (m_connect_thread != null && m_connect_thread.isAlive()) {
            try {
                m_connect_thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (m_write_thread != null && m_write_thread.isAlive()) {
            try {
                m_write_thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (m_read_thread != null && m_read_thread.isAlive()) {
            try {
                m_read_thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    synchronized public void start() {
        m_running = true;

        if (m_write_thread == null || !m_write_thread.isAlive()) {
            m_write_thread = new Thread(new WriteThread());
            m_write_thread.start();
        }

        if (m_read_thread == null || !m_read_thread.isAlive()) {
            m_read_thread = new Thread(new ReadThread());
            m_read_thread.start();
        }

        if (m_connect_thread == null || !m_connect_thread.isAlive()) {
            m_connect_thread = new Thread(new ConnectionMonitor());
            m_connect_thread.start();
        }
    }


    synchronized public void restart() {
        stop();
        start();
    }

    synchronized public boolean isConnected() {
        return m_socket != null && m_socket.isConnected() && m_connected;
    }

    private synchronized boolean sendToWire(VisionMessage message) {
        String toSend = message.toJson() + "\n";
        if (m_socket != null && m_socket.isConnected()) {
            try {
                OutputStream os = m_socket.getOutputStream();
                os.write(toSend.getBytes());
                return true;
            } catch (IOException e) {
                Log.w("RobotConnection", "Could not send data to socket, try to reconnect");
                m_socket = null;
            }
        }
        return false;
    }

    public synchronized boolean send(VisionMessage message) {
        return mToSend.offer(message);
    }

    public void broadcastRobotConnected() {
        Intent i = new Intent(RobotConnectionStatusBroadcastReceiver.ACTION_ROBOT_CONNECTED);
        m_context.sendBroadcast(i);
    }

    public void broadcastShotTaken() {
        Intent i = new Intent(RobotEventBroadcastReceiver.ACTION_SHOT_TAKEN);
        m_context.sendBroadcast(i);
    }

    public void broadcastWantVisionMode() {
        Intent i = new Intent(RobotEventBroadcastReceiver.ACTION_WANT_VISION);
        m_context.sendBroadcast(i);
    }

    public void broadcastWantIntakeMode() {
        Intent i = new Intent(RobotEventBroadcastReceiver.ACTION_WANT_INTAKE);
        m_context.sendBroadcast(i);
    }

    public void broadcastRobotDisconnected() {
        Intent i = new Intent(RobotConnectionStatusBroadcastReceiver.ACTION_ROBOT_DISCONNECTED);
        m_context.sendBroadcast(i);
    }
}
