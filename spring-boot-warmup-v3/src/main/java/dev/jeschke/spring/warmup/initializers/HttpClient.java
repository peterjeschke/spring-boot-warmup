package dev.jeschke.spring.warmup.initializers;

import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;

import dev.jeschke.spring.warmup.Endpoint;
import dev.jeschke.spring.warmup.WarmUpFactory;
import dev.jeschke.spring.warmup.internal.WarmUpSettings;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.context.WebServerApplicationContext;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class HttpClient {

    private final WebServerApplicationContext context;
    private final WarmUpFactory warmUpFactory;

    public void callEndpoint(final Endpoint endpoint, final WarmUpSettings configuration) {
        final var port = context.getWebServer().getPort();
        final var url =
                "%s://%s:%s/%s".formatted(configuration.protocol(), configuration.hostname(), port, endpoint.path());
        log.info(
                "Calling endpoint {} {} with body {} ({})",
                endpoint.method(),
                url,
                endpoint.body(),
                endpoint.contentType());

        final var method = HttpMethod.valueOf(endpoint.method());
        var spec = warmUpFactory
                .getRestClient(configuration.httpClient())
                .method(method)
                .uri(url);
        if (endpoint.body() != null) {
            spec = spec //
                    .contentType(MediaType.valueOf(endpoint.contentType())) //
                    .body(endpoint.body());
        } else if (expectRequestBody(method)) {
            log.warn(
                    "Trying to call endpoint {} with method {} but no body was provided. Call might fail.",
                    endpoint,
                    method);
        }
        final var statusCode = spec //
                .retrieve() //
                .toBodilessEntity() //
                .getStatusCode();

        if (!statusCode.is2xxSuccessful()) {
            log.warn("Call to {} with method {} failed. Returned status code {}", url, endpoint.method(), statusCode);
        }
    }

    private boolean expectRequestBody(final HttpMethod httpMethod) {
        return List.of(POST, PUT, PATCH).contains(httpMethod);
    }
}
