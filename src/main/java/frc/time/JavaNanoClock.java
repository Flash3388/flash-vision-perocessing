package frc.time;

public class JavaNanoClock implements Clock {

    private final long mStartTime;

    public JavaNanoClock() {
        mStartTime = System.nanoTime();
    }

    @Override
    public long currentTimeMillis() {
        return (long) ((System.nanoTime() - mStartTime) * 1e-6);
    }
}
