package dev.jeschke.spring.warmup.initializers;

import static dev.jeschke.spring.warmup.initializers.InternalEndpoint.INTERNAL_WARM_UP_ENDPOINT;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import dev.jeschke.spring.warmup.WarmUpSettings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.bind.Name;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@Slf4j
@Component
@RequiredArgsConstructor
public class InternalEndpointHttpInitializer implements WarmUpInitializer {

    private final ServletWebServerApplicationContext context;

    @Name("warmUpRestClient")
    private final RestClient restClient;

    @Override
    public void warmUp(final WarmUpSettings settings) {
        if (!settings.useInternalEndpoint()) {
            return;
        }

        context.registerBean("warmUpInternalEndpoint", InternalEndpoint.class);
        reloadControllers();

        callInternalEndpoint();

        context.removeBeanDefinition("warmUpInternalEndpoint");
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

    private void callInternalEndpoint() {
        final var port = context.getWebServer().getPort();
        final var url = "http://localhost:%s/%s".formatted(port, INTERNAL_WARM_UP_ENDPOINT);
        final var response = restClient
                .post()
                .uri(url)
                .accept(APPLICATION_JSON)
                .contentType(APPLICATION_JSON)
                .body(InternalEndpointPayload.createDefault())
                .retrieve()
                .toBodilessEntity();
        if (!response.getStatusCode().is2xxSuccessful()) {
            log.warn("Warming up internal endpoint was not successful. Status code: {}", response.getStatusCode());
        }
    }
}
