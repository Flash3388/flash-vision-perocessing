package frc.time.sync;

import frc.time.Clock;

public class NtpClock implements Clock {

    private final Clock mMeasureClock;
    private long mOffset;

    public NtpClock(Clock measureClock) {
        mMeasureClock = measureClock;
        mOffset = 0;
    }

    @Override
    public long currentTimeMillis() {
        return mMeasureClock.currentTimeMillis() + mOffset;
    }

    public void updateOffset(long offset) {
        mOffset += offset;
    }
}
