package dev.jeschke.spring.warmup;

import dev.jeschke.spring.warmup.builder.WarmUpBuilderImpl;
import dev.jeschke.spring.warmup.initializers.WarmUpInitializer;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WarmUpRunner {

    private final List<WarmUpInitializer> initializers;
    private final AtomicBoolean done = new AtomicBoolean(false);

    @Async
    @EventListener
    public void onContextRefreshed(final ContextRefreshedEvent event) {
        var builder = configureCustomizers(event.getApplicationContext());
        for (final var initializer : initializers) {
            builder = initializer.loadComponents(builder);
        }
        final var configuration = builder.build();
        initializers.forEach(initializer -> initializer.warmUp(configuration));
        done.set(true);
    }

    private WarmUpBuilder configureCustomizers(final ApplicationContext applicationContext) {
        WarmUpBuilder result = new WarmUpBuilderImpl();
        for (final var customizer :
                applicationContext.getBeansOfType(WarmUpCustomizer.class).values()) {
            result = customizer.apply(result);
        }
        return result;
    }

    public boolean isWarmedUp() {
        return done.get();
    }
}
