package dev.jeschke.spring.warmup.initializers;

import static dev.jeschke.spring.warmup.initializers.AutomaticEndpoint.AUTOMATIC_WARM_UP_ENDPOINT;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import dev.jeschke.spring.warmup.WarmUpFactory;
import dev.jeschke.spring.warmup.WarmUpSettings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@Slf4j
@Component
@RequiredArgsConstructor
public class AutomaticEndpointHttpInitializer implements WarmUpInitializer {

    public static final String CONTROLLER_BEAN_NAME = "warmUpAutomaticEndpoint";
    private final ServletWebServerApplicationContext context;
    private final WarmUpFactory warmUpFactory;

    @Override
    public void warmUp(final WarmUpSettings settings) {
        if (!settings.enableAutomaticMvcEndpoint()) {
            return;
        }

        context.registerBean(CONTROLLER_BEAN_NAME, AutomaticEndpoint.class);
        reloadControllers();

        callAutomaticEndpoint(settings);

        context.removeBeanDefinition(CONTROLLER_BEAN_NAME);
        reloadControllers();
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

    private void callAutomaticEndpoint(final WarmUpSettings settings) {
        log.info("Calling automatic endpoint");
        final var port = context.getWebServer().getPort();
        final var url = "%s://localhost:%s/%s".formatted(settings.protocol(), port, AUTOMATIC_WARM_UP_ENDPOINT);
        final var response = warmUpFactory
                .getRestClient(settings.httpClient())
                .post()
                .uri(url)
                .accept(APPLICATION_JSON)
                .contentType(APPLICATION_JSON)
                .body(AutomaticEndpointRequestBody.createDefault())
                .retrieve()
                .toBodilessEntity();
        if (!response.getStatusCode().is2xxSuccessful()) {
            log.warn("Warming up automatic endpoint was not successful. Status code: {}", response.getStatusCode());
        }
    }
}
