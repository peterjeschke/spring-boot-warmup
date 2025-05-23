package dev.jeschke.spring.warmup.initializers;

import java.time.Duration;
import org.springframework.stereotype.Component;

@Component
public class RepeatingInvocation {
    public void invokeRepeating(final int times, final Duration interval, final Runnable runnable)
            throws InterruptedException {
        for (var i = 0; i < times; i++) {
            runnable.run();
            Thread.sleep(interval.toMillis());
        }
    }
}
