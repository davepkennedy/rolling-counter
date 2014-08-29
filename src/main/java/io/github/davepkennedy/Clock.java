package io.github.davepkennedy;

/**
 * A clock which tells the time
 */
public interface Clock {
    /**
     * @return The current time in milliseconds
     */
    long time();
}
