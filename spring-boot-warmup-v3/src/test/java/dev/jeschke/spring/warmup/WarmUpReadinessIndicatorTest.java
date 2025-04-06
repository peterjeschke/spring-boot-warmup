package dev.jeschke.spring.warmup;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import static org.springframework.boot.availability.ReadinessState.ACCEPTING_TRAFFIC;
import static org.springframework.boot.availability.ReadinessState.REFUSING_TRAFFIC;

import java.util.stream.Stream;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.availability.ApplicationAvailability;
import org.springframework.boot.availability.AvailabilityState;

@ExtendWith(MockitoExtension.class)
class WarmUpReadinessIndicatorTest {

    private static final boolean IS_NOT_READY_YET = false;
    private static final boolean IS_READY = true;
    private static final boolean ENABLE_INDICATOR = true;
    private static final boolean DISABLE_INDICATOR = false;

    @Mock
    private WarmUpRunner warmUpRunner;

    @Mock
    private WarmUpSettingsFactory settingsFactory;

    @Mock
    private WarmUpSettings settings;

    @Mock
    private ApplicationAvailability availability;

    @InjectMocks
    private WarmUpReadinessIndicator indicator;

    @ParameterizedTest
    @MethodSource("getStateParams")
    void getState(boolean isUp, boolean enableReadinessIndicator, AvailabilityState expected) {
        when(settingsFactory.getSettings()).thenReturn(settings);
        lenient().when(warmUpRunner.isWarmedUp()).thenReturn(isUp);
        lenient().when(settings.enableReadinessIndicator()).thenReturn(enableReadinessIndicator);

        final var actual = indicator.getState(availability);

        assertThat(actual).isEqualTo(expected);
    }

    private static Stream<Arguments> getStateParams() {
        return Stream.of(
                arguments(IS_NOT_READY_YET, ENABLE_INDICATOR, REFUSING_TRAFFIC),
                arguments(IS_NOT_READY_YET, DISABLE_INDICATOR, ACCEPTING_TRAFFIC),
                arguments(IS_READY, ENABLE_INDICATOR, ACCEPTING_TRAFFIC),
                arguments(IS_READY, DISABLE_INDICATOR, ACCEPTING_TRAFFIC));
    }
}
