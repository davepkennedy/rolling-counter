package io.github.davepkennedy;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class RollingCounterTest {

    private final ClockFixture clock = new ClockFixture();
    private RollingCounter counter;

    @Before
    public void setup() {
        this.counter = new RollingCounter(clock,
                new RollingCounter.Config(TimeUnit.SECONDS, 1, 60),
                new RollingCounter.Config(TimeUnit.MINUTES, 1, 4),
                new RollingCounter.Config(TimeUnit.MINUTES, 5, 15),
                new RollingCounter.Config(TimeUnit.MINUTES, 10, 40)
        );
    }

    @Test
    public void itRollsUpTheHour() {
        long millis = TimeUnit.MILLISECONDS.convert(1, TimeUnit.HOURS);
        for (int i = 0; i < millis; i++) {
            clock.setTime(i);
            counter.increment();
        }
        assertThat(counter.count(), is(millis));
    }
}
