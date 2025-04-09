package dev.jeschke.spring.warmup;

import static org.springframework.boot.availability.ReadinessState.ACCEPTING_TRAFFIC;
import static org.springframework.boot.availability.ReadinessState.REFUSING_TRAFFIC;

import dev.jeschke.spring.warmup.initializers.WarmUpInitializer;
import java.util.List;
import org.springframework.boot.actuate.availability.ReadinessStateHealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.availability.ApplicationAvailability;
import org.springframework.boot.availability.AvailabilityState;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnClass(ReadinessStateHealthIndicator.class)
public class WarmUpReadinessIndicator extends ReadinessStateHealthIndicator {

    private final WarmUpRunner warmUpRunner;
    private final WarmUpFactory factory;
    private final List<WarmUpInitializer> initializers;

    public WarmUpReadinessIndicator(
            final ApplicationAvailability availability,
            final WarmUpRunner warmUpRunner,
            final WarmUpFactory factory,
            final List<WarmUpInitializer> initializers) {
        super(availability);
        this.warmUpRunner = warmUpRunner;
        this.factory = factory;
        this.initializers = initializers;
    }

    @Override
    protected AvailabilityState getState(final ApplicationAvailability applicationAvailability) {
        final var settings = factory.getSettings(initializers);
        return !warmUpRunner.isWarmedUp() && settings.enableReadinessIndicator() ? REFUSING_TRAFFIC : ACCEPTING_TRAFFIC;
    }
}
