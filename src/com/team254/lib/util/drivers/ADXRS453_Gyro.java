/*----------------------------------------------------------------------------*/
/* Copyright (c) FIRST 2015-2016. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/* MODIFIED BY TEAM 254                                                       */
/*----------------------------------------------------------------------------*/

package com.team254.lib.util.drivers;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.GyroBase;
import edu.wpi.first.wpilibj.PIDSource;
import edu.wpi.first.wpilibj.SPI;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.interfaces.Gyro;
import edu.wpi.first.wpilibj.livewindow.LiveWindow;
import edu.wpi.first.wpilibj.livewindow.LiveWindowSendable;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Use a rate gyro to return the robots heading relative to a starting position. The Gyro class tracks the robots
 * heading based on the starting position. As the robot rotates the new heading is computed by integrating the rate of
 * rotation returned by the sensor. When the class is instantiated, it does a short calibration routine where it samples
 * the gyro while at rest to determine the default offset. This is subtracted from each sample to determine the heading.
 *
 * This class is for the digital ADXRS453 gyro sensor that connects via SPI. A datasheet can be found here:
 * http://www.analog.com/media/en/technical-documentation/data-sheets/ADXRS453. pdf
 */
public class ADXRS453_Gyro extends GyroBase implements Gyro, PIDSource, LiveWindowSendable {
    public static final double kCalibrationSampleTime = 5.0;

    private static final double kSamplePeriod = 0.001;
    private static final double kDegreePerSecondPerLSB = -0.0125;

    private static final int kPIDRegister = 0x0C;

    private SPI m_spi;

    private boolean m_is_calibrating;
    private double m_last_center;

    /**
     * Constructor. Uses the onboard CS0.
     */
    public ADXRS453_Gyro() {
        this(SPI.Port.kOnboardCS0);
    }

    /**
     * Constructor.
     *
     * @param port
     *            (the SPI port that the gyro is connected to)
     */
    public ADXRS453_Gyro(SPI.Port port) {
        m_spi = new SPI(port);
        m_spi.setClockRate(3000000);
        m_spi.setMSBFirst();
        m_spi.setSampleDataOnRising();
        m_spi.setClockActiveHigh();
        m_spi.setChipSelectActiveLow();

        /** Validate the part ID */
        if ((readRegister(kPIDRegister) & 0xff00) != 0x5200) {
            m_spi.free();
            m_spi = null;
            DriverStation.reportError("Could not find ADXRS453 gyro on SPI port " + port.value, false);
            return;
        }

        m_spi.initAccumulator(kSamplePeriod, 0x20000000, 4, 0x0c00000E, 0x04000000, 10, 16, true, true);

        calibrate();

        LiveWindow.addSensor("ADXRS453_Gyro", port.value, this);
    }

    /**
     * This is a blocking calibration call. There are also non-blocking options available in this class!
     * 
     * {@inheritDoc}
     */
    @Override
    public synchronized void calibrate() {
        Timer.delay(0.1);
        startCalibrate();
        Timer.delay(kCalibrationSampleTime);
        endCalibrate();
    }

    public synchronized void startCalibrate() {
        if (m_spi == null)
            return;

        if (!m_is_calibrating) {
            m_is_calibrating = true;
            m_spi.setAccumulatorCenter(0);
            m_spi.resetAccumulator();
        }
    }

    public synchronized void endCalibrate() {
        if (m_is_calibrating) {
            m_is_calibrating = false;
            m_last_center = m_spi.getAccumulatorAverage();
            m_spi.setAccumulatorCenter((int) Math.round(m_last_center));
            m_spi.resetAccumulator();
        }
    }

    public synchronized void cancelCalibrate() {
        if (m_is_calibrating) {
            m_is_calibrating = false;
            m_spi.setAccumulatorCenter((int) Math.round(m_last_center));
            m_spi.resetAccumulator();
        }
    }

    public synchronized double getCenter() {
        return m_last_center;
    }

    private boolean calcParity(int v) {
        boolean parity = false;
        while (v != 0) {
            parity = !parity;
            v = v & (v - 1);
        }
        return parity;
    }

    private int readRegister(int reg) {
        int cmdhi = 0x8000 | (reg << 1);
        boolean parity = calcParity(cmdhi);

        ByteBuffer buf = ByteBuffer.allocateDirect(4);
        buf.order(ByteOrder.BIG_ENDIAN);
        buf.put(0, (byte) (cmdhi >> 8));
        buf.put(1, (byte) (cmdhi & 0xff));
        buf.put(2, (byte) 0);
        buf.put(3, (byte) (parity ? 0 : 1));

        m_spi.write(buf, 4);
        m_spi.read(false, buf, 4);

        if ((buf.get(0) & 0xe0) == 0) {
            return 0;
        }
        return (buf.getInt(0) >> 5) & 0xffff;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void reset() {
        if (m_is_calibrating) {
            cancelCalibrate();
        }
        m_spi.resetAccumulator();
    }

    /**
     * Delete (free) the spi port used for the gyro and stop accumulating.
     */
    @Override
    public void free() {
        if (m_spi != null) {
            m_spi.free();
            m_spi = null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized double getAngle() {
        if (m_spi == null)
            return 0.0;
        if (m_is_calibrating) {
            return 0.0;
        }
        return m_spi.getAccumulatorValue() * kDegreePerSecondPerLSB * kSamplePeriod;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized double getRate() {
        if (m_spi == null)
            return 0.0;
        if (m_is_calibrating) {
            return 0.0;
        }
        return m_spi.getAccumulatorLastValue() * kDegreePerSecondPerLSB;
    }
}
