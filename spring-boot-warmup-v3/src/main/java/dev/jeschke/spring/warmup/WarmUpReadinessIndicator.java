package dev.jeschke.spring.warmup;

import static org.springframework.boot.availability.ReadinessState.ACCEPTING_TRAFFIC;
import static org.springframework.boot.availability.ReadinessState.REFUSING_TRAFFIC;

import org.springframework.boot.actuate.availability.ReadinessStateHealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.availability.ApplicationAvailability;
import org.springframework.boot.availability.AvailabilityState;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnClass(ReadinessStateHealthIndicator.class)
public class WarmUpReadinessIndicator extends ReadinessStateHealthIndicator {

    private final WarmUpRunner warmUpRunner;
    private final WarmUpSettingsFactory settingsFactory;

    public WarmUpReadinessIndicator(
            final ApplicationAvailability availability,
            final WarmUpRunner warmUpRunner,
            final WarmUpSettingsFactory settingsFactory) {
        super(availability);
        this.warmUpRunner = warmUpRunner;
        this.settingsFactory = settingsFactory;
    }

    @Override
    protected AvailabilityState getState(final ApplicationAvailability applicationAvailability) {
        final var settings = settingsFactory.getSettings();
        return !warmUpRunner.isWarmedUp() && settings.enableReadinessIndicator() ? REFUSING_TRAFFIC : ACCEPTING_TRAFFIC;
    }
}
