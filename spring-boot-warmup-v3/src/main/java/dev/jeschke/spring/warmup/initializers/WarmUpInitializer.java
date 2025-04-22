package dev.jeschke.spring.warmup.initializers;

import dev.jeschke.spring.warmup.WarmUpBuilder;
import dev.jeschke.spring.warmup.internal.WarmUpSettings;

public interface WarmUpInitializer {

    default WarmUpBuilder configure(final WarmUpBuilder builder) {
        return builder;
    }

    void warmUp(WarmUpSettings configuration);
}
