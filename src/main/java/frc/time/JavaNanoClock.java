package frc.time;

public class JavaNanoClock implements Clock {

    @Override
    public long currentTimeMillis() {
        return (long) (System.nanoTime() * 1e-6);
    }
}
