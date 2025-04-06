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
    private final WarmUpSettingsFactory settingsFactory;
    private final AtomicBoolean done = new AtomicBoolean(false);

    @Async
    @EventListener
    public void onContextRefreshed(final ContextRefreshedEvent event) {
        final var settings = settingsFactory.getSettings();
        initializers.forEach(initializer -> initializer.warmUp(settings));
        done.set(true);
    }

    public boolean isWarmedUp() {
        return done.get();
    }
}
