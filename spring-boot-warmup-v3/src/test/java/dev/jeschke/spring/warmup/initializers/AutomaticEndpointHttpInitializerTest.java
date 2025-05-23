package dev.jeschke.spring.warmup.initializers;

import static dev.jeschke.spring.warmup.initializers.AutomaticEndpoint.AUTOMATIC_WARM_UP_ENDPOINT;
import static dev.jeschke.spring.warmup.initializers.AutomaticEndpointHttpInitializer.CONTROLLER_BEAN_NAME;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.AdditionalAnswers.answerVoid;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import dev.jeschke.spring.warmup.Endpoint;
import dev.jeschke.spring.warmup.internal.RepeatingWarmUpSettings;
import dev.jeschke.spring.warmup.internal.WarmUpSettings;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@ExtendWith(MockitoExtension.class)
class AutomaticEndpointHttpInitializerTest {

    private static final int REPEATING_TIMES = 5;
    private static final Duration REPEATING_INTERVAL = Duration.ofMillis(1000);
    private static final int REPEATING_TIMES_2 = 10;
    private static final Duration REPEATING_INTERVAL_2 = Duration.ofMillis(2000);

    @Mock(answer = RETURNS_DEEP_STUBS)
    private ServletWebServerApplicationContext context;

    @Mock
    private RepeatingInvocation repeatingInvocation;

    @Mock
    private HttpClient httpClient;

    @Mock
    private RequestMappingHandlerMapping handlerMapping;

    @Mock
    private RequestMappingInfo requestMappingInfo;

    @InjectMocks
    private AutomaticEndpointHttpInitializer initializer;

    @BeforeEach
    void setUp() {
        lenient()
                .when(context.getBeansOfType(RequestMappingHandlerMapping.class))
                .thenReturn(Map.of("name", handlerMapping));
        lenient()
                .when(handlerMapping.getHandlerMethods())
                .thenReturn(Map.of(requestMappingInfo, mock(HandlerMethod.class)));
    }

    @Test
    void warmUp_allDisabled() {
        final var settings = createSettings(false, List.of());

        initializer.warmUp(settings);

        verifyNoInteractions(context, httpClient, handlerMapping);
    }

    @Test
    void warmUp_singleEndpoint() {
        final var settings = createSettings(true, List.of());

        initializer.warmUp(settings);

        final var inOrder = inOrder(context, httpClient, handlerMapping);
        inOrder.verify(context).registerBean(CONTROLLER_BEAN_NAME, AutomaticEndpoint.class);
        inOrder.verify(handlerMapping).unregisterMapping(requestMappingInfo);
        inOrder.verify(handlerMapping).afterPropertiesSet();
        inOrder.verify(httpClient).callEndpoint(createEndpoint(), settings);
        inOrder.verify(context).removeBeanDefinition(CONTROLLER_BEAN_NAME);
        inOrder.verify(handlerMapping).unregisterMapping(requestMappingInfo);
        inOrder.verify(handlerMapping).afterPropertiesSet();
    }

    @Test
    void warmUp_repeatingEndpoint() throws InterruptedException {
        final var settings = createSettings(
                false,
                List.of(new RepeatingWarmUpSettings(
                        REPEATING_TIMES, REPEATING_INTERVAL, createSettings(true, List.of()))));

        initializer.warmUp(settings);

        final var inOrder = inOrder(context, repeatingInvocation, handlerMapping);
        inOrder.verify(context).registerBean(CONTROLLER_BEAN_NAME, AutomaticEndpoint.class);
        inOrder.verify(handlerMapping).unregisterMapping(requestMappingInfo);
        inOrder.verify(handlerMapping).afterPropertiesSet();
        inOrder.verify(repeatingInvocation).invokeRepeating(eq(REPEATING_TIMES), eq(REPEATING_INTERVAL), any());
        inOrder.verify(context).removeBeanDefinition(CONTROLLER_BEAN_NAME);
        inOrder.verify(handlerMapping).unregisterMapping(requestMappingInfo);
        inOrder.verify(handlerMapping).afterPropertiesSet();
        verifyNoInteractions(httpClient);
    }

    @Test
    void warmUp_repeatingEndpoint_butDisabled() throws InterruptedException {
        final var settings = createSettings(
                false,
                List.of(
                        new RepeatingWarmUpSettings(
                                REPEATING_TIMES, REPEATING_INTERVAL, createSettings(false, List.of())),
                        new RepeatingWarmUpSettings(
                                REPEATING_TIMES_2, REPEATING_INTERVAL_2, createSettings(true, List.of()))));

        initializer.warmUp(settings);

        verify(repeatingInvocation, never()).invokeRepeating(eq(REPEATING_TIMES), eq(REPEATING_INTERVAL), any());
        verify(repeatingInvocation).invokeRepeating(eq(REPEATING_TIMES_2), eq(REPEATING_INTERVAL_2), any());
    }

    @Test
    void warmUp_repeatingEndpoint_invokesHttpClient() throws InterruptedException {
        final var repeatedSetting = createSettings(true, List.of());
        final var settings = createSettings(
                false, List.of(new RepeatingWarmUpSettings(REPEATING_TIMES, REPEATING_INTERVAL, repeatedSetting)));
        doAnswer(answerVoid((final Integer times, final Duration interval, final Runnable runnable) -> runnable.run()))
                .when(repeatingInvocation)
                .invokeRepeating(anyInt(), any(), any());

        initializer.warmUp(settings);

        verify(httpClient).callEndpoint(createEndpoint(), repeatedSetting);
    }

    @Test
    void warmUp_deregistersOnException() {
        final var settings = createSettings(true, List.of());
        final var exception = new RuntimeException();
        doThrow(exception).when(httpClient).callEndpoint(any(), any());

        assertThatThrownBy(() -> initializer.warmUp(settings)).isSameAs(exception);

        final var inOrder = inOrder(context, httpClient, handlerMapping);
        inOrder.verify(context).registerBean(CONTROLLER_BEAN_NAME, AutomaticEndpoint.class);
        inOrder.verify(handlerMapping).unregisterMapping(requestMappingInfo);
        inOrder.verify(handlerMapping).afterPropertiesSet();
        inOrder.verify(httpClient).callEndpoint(createEndpoint(), settings);
        inOrder.verify(context).removeBeanDefinition(CONTROLLER_BEAN_NAME);
        inOrder.verify(handlerMapping).unregisterMapping(requestMappingInfo);
        inOrder.verify(handlerMapping).afterPropertiesSet();
    }

    private Endpoint createEndpoint() {
        return new Endpoint(
                AUTOMATIC_WARM_UP_ENDPOINT, AutomaticEndpointRequestBody.createDefault(), APPLICATION_JSON_VALUE);
    }

    private WarmUpSettings createSettings(
            final boolean enableAutomaticMvcEndpoint, final List<RepeatingWarmUpSettings> repeatingWarmUpSettings) {
        return new WarmUpSettings(
                List.of(), enableAutomaticMvcEndpoint, false, "", null, "", false, repeatingWarmUpSettings);
    }
}
