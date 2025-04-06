package dev.jeschke.spring.warmup;

import dev.jeschke.spring.warmup.builder.WarmUpBuilderImpl;
import dev.jeschke.spring.warmup.initializers.WarmUpInitializer;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WarmUpSettingsFactory {
    private final ApplicationContext context;
    private final List<WarmUpInitializer> initializers;
    private final AtomicReference<WarmUpSettings> cachedSettings = new AtomicReference<>();

    public WarmUpSettings getSettings() {
        return cachedSettings.updateAndGet(settings -> {
            if (settings == null) {
                return buildSettings();
            }
            return settings;
        });
    }

    private WarmUpSettings buildSettings() {
        var builder = configureCustomizers(context);
        for (final var initializer : initializers) {
            builder = initializer.configure(builder);
        }
        return builder.build();
    }

    private WarmUpBuilder configureCustomizers(final ApplicationContext applicationContext) {
        WarmUpBuilder result = new WarmUpBuilderImpl();
        for (final var customizer :
                applicationContext.getBeansOfType(WarmUpCustomizer.class).values()) {
            result = customizer.apply(result);
        }
        return result;
    }
}
