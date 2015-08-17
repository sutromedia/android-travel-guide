package com.sutromedia.android.lib.util;


public class Timer {
    private long                                mStart = 0;

    public Timer() {
	reset();
    }

    public long getElapsed() {
        long elapsedTimeMillis = System.currentTimeMillis()-mStart;
	return elapsedTimeMillis;
    }

    public long getElapsedReset() {
	long elapsed = getElapsed();
	reset();
	return elapsed;
    }

    private void reset() {
        mStart = System.currentTimeMillis();
    }
}