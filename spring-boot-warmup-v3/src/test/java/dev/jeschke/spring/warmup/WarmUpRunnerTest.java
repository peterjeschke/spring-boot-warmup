package dev.jeschke.spring.warmup;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

import dev.jeschke.spring.warmup.initializers.WarmUpInitializer;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.event.ContextRefreshedEvent;

@ExtendWith(MockitoExtension.class)
class WarmUpRunnerTest {

    @Mock
    private ContextRefreshedEvent event;

    @Mock
    private WarmUpInitializer initializer1;

    @Mock
    private WarmUpInitializer initializer2;

    @Mock
    private WarmUpSettingsFactory factory;

    @Mock
    private WarmUpSettings settings;

    private WarmUpRunner warmUpRunner;

    @BeforeEach
    void init() {
        warmUpRunner = new WarmUpRunner(List.of(initializer1, initializer2), factory);
        when(factory.getSettings()).thenReturn(settings);
    }

    @Test
    void onContextRefreshed() {
        warmUpRunner.onContextRefreshed(event);

        final var inOrder = inOrder(initializer1, initializer2);
        inOrder.verify(initializer1).warmUp(settings);
        inOrder.verify(initializer2).warmUp(settings);
    }

    @Test
    void isWarmedUp() {
        assertThat(warmUpRunner.isWarmedUp()).isFalse();

        warmUpRunner.onContextRefreshed(event);

        assertThat(warmUpRunner.isWarmedUp()).isTrue();
    }
}
