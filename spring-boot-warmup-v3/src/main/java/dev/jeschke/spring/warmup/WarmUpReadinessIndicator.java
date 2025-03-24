package dev.jeschke.spring.warmup;

import org.springframework.boot.actuate.availability.ReadinessStateHealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.availability.ApplicationAvailability;
import org.springframework.boot.availability.AvailabilityState;
import org.springframework.stereotype.Component;

import static org.springframework.boot.availability.ReadinessState.ACCEPTING_TRAFFIC;
import static org.springframework.boot.availability.ReadinessState.REFUSING_TRAFFIC;

@Component
@ConditionalOnClass(ReadinessStateHealthIndicator.class)
public class WarmUpReadinessIndicator extends ReadinessStateHealthIndicator {

    private final WarmUpRunner warmUpRunner;

    public WarmUpReadinessIndicator(final ApplicationAvailability availability, final WarmUpRunner warmUpRunner) {
        super( availability );
        this.warmUpRunner = warmUpRunner;
    }

    @Override
    protected AvailabilityState getState(final ApplicationAvailability applicationAvailability) {
        return warmUpRunner.isWarmedUp() ? ACCEPTING_TRAFFIC : REFUSING_TRAFFIC;
    }
}
