package com.team254.lib.util.drivers;

import edu.wpi.first.wpilibj.I2C;
import edu.wpi.first.wpilibj.I2C.Port;
import edu.wpi.first.wpilibj.Timer;

import java.util.TimerTask;

/**
 * Driver for a Lidar Lite sensor
 */
public class LidarLiteSensor {
    private I2C mI2C;
    private byte[] mDistance;
    private java.util.Timer mUpdater;
    private boolean mHasSignal;

    private final static int LIDAR_ADDR = 0x62;
    private final static int LIDAR_CONFIG_REGISTER = 0x00;
    private final static int LIDAR_DISTANCE_REGISTER = 0x8f;

    public LidarLiteSensor(Port port) {
        mI2C = new I2C(port, LIDAR_ADDR);
        mDistance = new byte[2];
        mUpdater = new java.util.Timer();
        mHasSignal = false;
    }

    /**
     * @return Distance in meters
     */
    public double getDistance() {
        int distCm = (int) Integer.toUnsignedLong(mDistance[0] << 8) + Byte.toUnsignedInt(mDistance[1]);
        return distCm / 100.0;
    }

    /**
     * @return true iff the sensor successfully provided data last loop
     */
    public boolean hasSignal() {
        return mHasSignal;
    }

    /**
     * Start 10Hz polling
     */
    public void start() {
        start(100);
    }

    /**
     * Start polling for period in milliseconds
     */
    public void start(int period) {
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                update();
            }
        };
        mUpdater.scheduleAtFixedRate(task, 0, period);
    }

    public void stop() {
        mUpdater.cancel();
        mUpdater = new java.util.Timer();
    }

    private void update() {
        if (mI2C.write(LIDAR_CONFIG_REGISTER, 0x04)) {
            // the write failed to ack
            mHasSignal = false;
            return;
        }
        Timer.delay(0.04); // Delay for measurement to be taken
        if (!mI2C.read(LIDAR_DISTANCE_REGISTER, 2, mDistance)) {
            // the read failed
            mHasSignal = false;
            return;
        }
        mHasSignal = true;
        Timer.delay(0.005); // Delay to prevent over polling
    }
}
