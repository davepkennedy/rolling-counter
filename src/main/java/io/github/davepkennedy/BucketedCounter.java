package io.github.davepkennedy;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

/**
 * Counts things in a bucket of time, expiring buckets that are older than some limit.
 */
public class BucketedCounter implements ExpiringCounter {
    private final Clock clock;
    private final TimeUnit bucketUnit;
    private final long bucketSize;
    private final long maxAge;
    private final ConcurrentMap<Long,Long> buckets = new ConcurrentHashMap<>();
    private List<ExpiryHandler> handlers = new ArrayList<>();

    /**
     * Construct a new instance.
     * @param bucketUnit The unit of time this bucket measures with.
     * @param bucketSize How much time each bucket covers.
     * @param maxAge How old buckets are allowed to be.
     * @param clock A clock which provides the time.
     */
    public BucketedCounter (
            TimeUnit bucketUnit,
            long bucketSize,
            long maxAge,
            Clock clock
    ) {
        this.bucketUnit = bucketUnit;
        this.bucketSize = bucketSize;
        this.maxAge = maxAge;
        this.clock = clock;
    }

    private long currentTimeUnit() {
        return bucketUnit.convert(clock.time(), TimeUnit.MILLISECONDS);
    }

    private long currentBucket() {
        return currentTimeUnit() / bucketSize;
    }

    private long lastBucket() {
        return (currentTimeUnit() - maxAge) / bucketSize;
    }

    /**
     * Removes all the buckets which are older than the maximum.
     */
    private void expire() {
        List<Long> expired = new ArrayList<>();
        long lastBucket = lastBucket();
        for (long bucket : buckets.keySet()) {
            if (bucket < lastBucket) {
                expireEvent (bucket, buckets.get(bucket));
                expired.add(bucket);
            }
        }
        for (long bucket : expired) {
            buckets.remove(bucket);
        }
    }

    /**
     * Invokes all the listeners for the expiry events
     * @param bucket The bucket being expired
     * @param count What the count is for this bucket
     */
    private void expireEvent(long bucket, long count) {
        long timestamp = TimeUnit.MILLISECONDS.convert(bucket * bucketSize, this.bucketUnit);
        //System.out.printf("%s expiring %d at %d %n", this, count, timestamp);
        for (ExpiryHandler handler : handlers) {
            handler.call(timestamp, count);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void increment() {
        increment(1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void increment(long count) {
        long bucket = currentBucket();
        buckets.putIfAbsent(bucket, 0L);
        buckets.put(bucket, buckets.get(bucket) + count);
        expire();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long count() {
        long lastBucket = lastBucket();
        long count = 0;
        for (long bucket : buckets.keySet()) {
            if (bucket >= lastBucket) {
                count += buckets.get(bucket);
            }
        }
        System.out.printf("%s has %d in %d buckets %n", this, count, buckets.size());
        return count;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void expiryHandler(ExpiryHandler handler) {
        this.handlers.add(handler);
    }

    @Override
    public String toString() {
        return String.format("BucketedCounter(Unit: %s Size: %d Age: %d)", bucketUnit, bucketSize, maxAge);
    }
}
