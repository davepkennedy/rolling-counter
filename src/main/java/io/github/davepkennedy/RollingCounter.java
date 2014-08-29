package io.github.davepkennedy;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * A simple counter which just keeps adding up.
 * It delegate bucketing the many other BucketedCounters.
 * Each bucketed counter is wired up so when one starts to expire counts, they will be added on to the next counter.
 * The first counter should be a high fidelity counter which has a short lifetime.
 * Subsequent counters should have decreasing fidelity and increasing age.
 */
public class RollingCounter implements Counter {
    private List<ExpiringCounter> counters = new ArrayList<>();

    /**
     * Initialize a new instance
     * @param clock A clock for telling the time
     * @param configs A set of many different bucket configurations
     */
    public RollingCounter (Clock clock, Config ... configs) {
        for (Config config : configs) {
            counters.add(new BucketedCounter(config.timeUnit, config.bucketSize, config.maxAge, clock));
        }
        /*
        Wires each counter up to the next one.
        Expiring counts from the last counter are quietly dropped
         */
        for (int i = 1; i < counters.size(); i++) {
            ExpiringCounter source = counters.get(i-1);
            ExpiringCounter dest = counters.get(i);
            source.expiryHandler(new RolloverExpiryHandler(dest));
        }
    }

    /**
     * Responds to the expiring counter event by adding the counts onto the target counter
     */
    private class RolloverExpiryHandler implements ExpiryHandler {
        private final Counter target;

        public RolloverExpiryHandler (Counter target) {
            this.target = target;
        }
        @Override
        public void call(long timestamp, long count) {
            target.increment(count);
        }
    }

    @Override
    public void increment() {
        increment(1);
    }

    @Override
    public void increment(long count) {
        if (counters.size() > 1) {
            counters.get(0).increment(count);
        }
    }

    @Override
    public long count() {
        long count = 0;
        for (Counter counter : counters) {
            count += counter.count();
        }
        return count;
    }

    public static class Config {
        private final TimeUnit timeUnit;
        private final long bucketSize;
        private final long maxAge;

        public Config (TimeUnit timeUnit, long bucketSize, long maxAge) {
            this.timeUnit = timeUnit;
            this.bucketSize = bucketSize;
            this.maxAge = maxAge;
        }
    }
}
