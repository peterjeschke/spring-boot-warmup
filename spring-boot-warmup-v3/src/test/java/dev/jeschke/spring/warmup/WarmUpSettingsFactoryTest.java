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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

@ExtendWith(MockitoExtension.class)
class WarmUpSettingsFactoryTest {

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

    private WarmUpSettingsFactory factory;

    @BeforeEach
    void setUp() {
        factory = new WarmUpSettingsFactory(context, List.of(initializer1, initializer2));
        when(context.getBeansOfType(WarmUpCustomizer.class))
                .thenReturn(new TreeMap<>(Map.of("customizer1", customizer1, "customizer2", customizer2)));
        when(customizer1.apply(any())).thenAnswer(returnsFirstArg());
        when(customizer2.apply(any())).thenAnswer(returnsFirstArg());
        when(initializer1.configure(any())).thenAnswer(returnsFirstArg());
        when(initializer2.configure(any())).thenAnswer(returnsFirstArg());
    }

    @Test
    void getSettings() {
        final var settings = factory.getSettings();

        final var inOrder = inOrder(customizer1, customizer2, initializer1, initializer2);
        inOrder.verify(customizer1).apply(builder.capture());
        inOrder.verify(customizer2).apply(builder.capture());
        inOrder.verify(initializer1).configure(builder.capture());
        inOrder.verify(initializer2).configure(builder.capture());

        assertThat(builder.getAllValues())
                .allSatisfy(value -> assertThat(builder.getAllValues()).allMatch(value::equals));
        assertThat(settings).isNotNull();
    }

    @Test
    void getSettings_onlyInitializesOnce() {
        final var settings1 = factory.getSettings();
        final var settings2 = factory.getSettings();

        final var inOrder = inOrder(customizer1, customizer2, initializer1, initializer2);
        inOrder.verify(customizer1).apply(builder.capture());
        inOrder.verify(customizer2).apply(builder.capture());
        inOrder.verify(initializer1).configure(builder.capture());
        inOrder.verify(initializer2).configure(builder.capture());

        assertThat(builder.getAllValues())
                .allSatisfy(value -> assertThat(builder.getAllValues()).allMatch(value::equals));
        assertThat(settings1).isSameAs(settings2);
    }
}
