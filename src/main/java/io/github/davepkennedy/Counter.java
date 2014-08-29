package io.github.davepkennedy;

/**
 * Created by dave on 28/08/2014.
 */
public interface Counter {
    void increment();
    void increment(long count);
    long count();
}
