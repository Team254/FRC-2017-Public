package com.team254.lib.util;

import java.util.LinkedList;

/**
 * Implements a simple circular buffer.
 */
public class CircularBuffer {
    int mWindowSize;
    LinkedList<Double> mSamples;
    double mSum;

    public CircularBuffer(int window_size) {
        mWindowSize = window_size;
        mSamples = new LinkedList<Double>();
        mSum = 0.0;
    }

    public void clear() {
        mSamples.clear();
        mSum = 0.0;
    }

    public double getAverage() {
        if (mSamples.isEmpty())
            return 0.0;
        return mSum / mSamples.size();
    }

    public void recomputeAverage() {
        // Reset any accumulation drift.
        mSum = 0.0;
        if (mSamples.isEmpty())
            return;
        for (Double val : mSamples) {
            mSum += val;
        }
        mSum /= mWindowSize;
    }

    public void addValue(double val) {
        mSamples.addLast(val);
        mSum += val;
        if (mSamples.size() > mWindowSize) {
            mSum -= mSamples.removeFirst();
        }
    }

    public int getNumValues() {
        return mSamples.size();
    }

    public boolean isFull() {
        return mWindowSize == mSamples.size();
    }
}
