package io.github.davepkennedy;

import org.junit.Before;
import org.junit.Test;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Created by dave on 29/08/2014.
 */
public class BucketedCounterTest {

    private final ClockFixture clock = new ClockFixture();
    private BucketedCounter counter;

    private BucketedCounter newCounter() {
        return new BucketedCounter(TimeUnit.SECONDS, 1, 60, clock);
    }
    @Before
    public void setup() {
        this.counter = newCounter();
    }

    @Test
    public void itAccumulates() {
        counter.increment();
        assertThat(counter.count(), is(1L));
    }

    @Test
    public void itExpires() {
        TestExpiryHandler expiryHandler = new TestExpiryHandler();
        clock.setTime(0);
        counter.increment();
        counter.expiryHandler(expiryHandler);
        clock.setTime(61000);
        counter.increment();

        assertTrue(expiryHandler.wasCalled());
        assertThat(expiryHandler.expiredCount(), is(1L));
    }

    private class TestExpiryHandler implements ExpiryHandler {

        private long expiredCount;
        private long expiredTimestamp;
        private boolean called;

        @Override
        public void call(long timestamp, long count) {
            called = true;
            expiredCount = count;
            expiredTimestamp = timestamp;
        }

        public boolean wasCalled() {return called;}
        public long expiredCount() {return expiredCount;}
        public long getExpiredTimestamp() {return expiredTimestamp;}
    }
}
