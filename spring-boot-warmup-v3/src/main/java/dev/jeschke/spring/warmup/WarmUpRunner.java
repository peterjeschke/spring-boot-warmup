package dev.jeschke.spring.warmup;

import dev.jeschke.spring.warmup.initializers.WarmUpInitializer;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WarmUpRunner {

    private final List<WarmUpInitializer> initializers;
    private final WarmUpFactory factory;
    private final AtomicBoolean done = new AtomicBoolean(false);

    @Async
    @EventListener
    public void onContextRefreshed(final ContextRefreshedEvent ignoredEvent) {
        final var settings = factory.getSettings(initializers);
        initializers.forEach(initializer -> initializer.warmUp(settings));
        done.set(true);
    }

    public boolean isWarmedUp() {
        return done.get();
    }
}
