package io.github.davepkennedy;

/**
 * Tells the time based on whatever it is told to tell
 */
public class ClockFixture implements Clock {
    private long time = 0;

    public void setTime(long time) {
        this.time = time;
    }

    @Override
    public long time() {
        return time;
    }
}
