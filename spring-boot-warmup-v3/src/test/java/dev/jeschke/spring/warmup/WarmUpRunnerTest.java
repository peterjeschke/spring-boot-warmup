package dev.jeschke.spring.warmup;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

import dev.jeschke.spring.warmup.initializers.WarmUpInitializer;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;

@ExtendWith(MockitoExtension.class)
class WarmUpRunnerTest {

    @Mock
    private ContextRefreshedEvent event;

    @Mock
    private ApplicationContext context;

    @Mock
    private WarmUpInitializer initializer1;

    @Mock
    private WarmUpInitializer initializer2;

    @Mock
    private WarmUpCustomizer customizer1;

    @Mock
    private WarmUpCustomizer customizer2;

    @Captor
    private ArgumentCaptor<WarmUpBuilder> builder;

    @Captor
    private ArgumentCaptor<WarmUpSettings> settings;

    private WarmUpRunner warmUpRunner;

    @BeforeEach
    void init() {
        warmUpRunner = new WarmUpRunner(List.of(initializer1, initializer2));
        when(event.getApplicationContext()).thenReturn(context);
        when(context.getBeansOfType(WarmUpCustomizer.class))
                .thenReturn(new TreeMap<>(Map.of("customizer1", customizer1, "customizer2", customizer2)));
        when(customizer1.apply(any())).thenAnswer(returnsFirstArg());
        when(customizer2.apply(any())).thenAnswer(returnsFirstArg());
        when(initializer1.configure(any())).thenAnswer(returnsFirstArg());
        when(initializer2.configure(any())).thenAnswer(returnsFirstArg());
    }

    @Test
    void onContextRefreshed() {
        warmUpRunner.onContextRefreshed(event);

        InOrder inOrder = inOrder(customizer1, customizer2, initializer1, initializer2);
        inOrder.verify(customizer1).apply(builder.capture());
        inOrder.verify(customizer2).apply(builder.capture());
        inOrder.verify(initializer1).configure(builder.capture());
        inOrder.verify(initializer2).configure(builder.capture());
        inOrder.verify(initializer1).warmUp(settings.capture());
        inOrder.verify(initializer2).warmUp(settings.capture());

        assertThat(builder.getAllValues())
                .allSatisfy(value -> assertThat(builder.getAllValues()).allMatch(value::equals));
        assertThat(settings.getAllValues())
                .allSatisfy(value -> assertThat(settings.getAllValues()).allMatch(value::equals));
        assertThat(builder.getValue().build()).isEqualTo(settings.getValue());
    }

    @Test
    void isWarmedUp() {
        assertThat(warmUpRunner.isWarmedUp()).isFalse();

        warmUpRunner.onContextRefreshed(event);

        assertThat(warmUpRunner.isWarmedUp()).isTrue();
    }
}
