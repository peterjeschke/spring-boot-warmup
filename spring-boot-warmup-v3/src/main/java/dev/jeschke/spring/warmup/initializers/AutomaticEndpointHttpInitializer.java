package dev.jeschke.spring.warmup.initializers;

import static dev.jeschke.spring.warmup.initializers.AutomaticEndpoint.AUTOMATIC_WARM_UP_ENDPOINT;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import dev.jeschke.spring.warmup.Endpoint;
import dev.jeschke.spring.warmup.internal.RepeatingWarmUpSettings;
import dev.jeschke.spring.warmup.internal.WarmUpSettings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@Slf4j
@Component
@RequiredArgsConstructor
public class AutomaticEndpointHttpInitializer implements WarmUpInitializer {

    public static final String CONTROLLER_BEAN_NAME = "warmUpAutomaticEndpoint";
    private final GenericApplicationContext context;
    private final RepeatingInvocation repeatingInvocation;
    private final HttpClient httpClient;

    @Override
    public void warmUp(final WarmUpSettings settings) {
        final var skipSingleCall = !settings.enableAutomaticMvcEndpoint();
        final var skipRepeatingCalls = settings.repeatingWarmUpSettings().stream()
                .map(RepeatingWarmUpSettings::settings)
                .noneMatch(WarmUpSettings::enableAutomaticMvcEndpoint);
        if (skipSingleCall && skipRepeatingCalls) {
            return;
        }

        context.registerBean(CONTROLLER_BEAN_NAME, AutomaticEndpoint.class);
        reloadControllers();

        try {
            if (!skipSingleCall) {
                httpClient.callEndpoint(buildInternalEndpoint(), settings);
            }
            for (final var repeatingWarmUpSetting : settings.repeatingWarmUpSettings()) {
                if (repeatingWarmUpSetting.settings().enableAutomaticMvcEndpoint()) {
                    repeatingInvocation.invokeRepeating(
                            repeatingWarmUpSetting.times(),
                            repeatingWarmUpSetting.interval(),
                            () -> httpClient.callEndpoint(buildInternalEndpoint(), repeatingWarmUpSetting.settings()));
                }
            }
        } catch (final InterruptedException e) {
            log.warn("Was interrupted. Will stop repeating calls", e);
            Thread.currentThread().interrupt();
        } finally {
            context.removeBeanDefinition(CONTROLLER_BEAN_NAME);
            reloadControllers();
        }
    }

    private Endpoint buildInternalEndpoint() {
        return new Endpoint(
                AUTOMATIC_WARM_UP_ENDPOINT, AutomaticEndpointRequestBody.createDefault(), APPLICATION_JSON_VALUE);
    }

    private void reloadControllers() {
        context.getBeansOfType(RequestMappingHandlerMapping.class).forEach((name, requestMappingHandlerMapping) -> {
            requestMappingHandlerMapping
                    .getHandlerMethods()
                    .keySet()
                    .forEach(requestMappingHandlerMapping::unregisterMapping);
            requestMappingHandlerMapping.afterPropertiesSet();
        });
    }
}
