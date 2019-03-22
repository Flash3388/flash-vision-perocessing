package frc.time.sync;

import frc.time.Clock;

public class NtpClock implements Clock {

    private final Clock mMeasureClock;
    private long mLastMeasureTime;
    private long mLastTime;

    public NtpClock(Clock measureClock) {
        mMeasureClock = measureClock;

        setTime(0);
    }

    @Override
    public long currentTimeMillis() {
        long currentMeasureTime = mMeasureClock.currentTimeMillis();
        long offset = currentMeasureTime - mLastMeasureTime;
        mLastMeasureTime = offset;

        return mLastTime + offset;
    }

    public void setTime(long time) {
        mLastTime = time;
        mLastMeasureTime = mMeasureClock.currentTimeMillis();
    }

    public void updateOffset(long offset) {
        mLastTime += offset;
        mLastMeasureTime = mMeasureClock.currentTimeMillis();
    }
}
