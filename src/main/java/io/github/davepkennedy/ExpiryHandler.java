package io.github.davepkennedy;

/**
 * Created by dave on 29/08/2014.
 */
public interface ExpiryHandler {
    void call (long timestamp, long count);
}
