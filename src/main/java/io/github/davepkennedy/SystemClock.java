package io.github.davepkennedy;

/**
 * Tells the time based on the OS clock.
 */
public class SystemClock implements Clock {
    @Override
    public long time() {
        return System.currentTimeMillis();
    }
}
