package frc.time;

public class JavaNanoClock implements Clock {

    @Override
    public long currentTimeMillis() {
        return System.nanoTime();
    }
}
