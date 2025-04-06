package dev.jeschke.spring.warmup.initializers;

import static java.util.Objects.requireNonNull;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;

import dev.jeschke.spring.warmup.ControllerWarmUp;
import dev.jeschke.spring.warmup.ControllerWarmUp.DefaultRequestBodyType;
import dev.jeschke.spring.warmup.Endpoint;
import dev.jeschke.spring.warmup.WarmUpBuilder;
import dev.jeschke.spring.warmup.WarmUpSettings;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.bind.Name;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.context.ApplicationContext;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.RestClient;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@Slf4j
@Component
@RequiredArgsConstructor
public class HttpInitializer implements WarmUpInitializer {

    private final ApplicationContext applicationContext;

    @Name("warmUpRestClient")
    private final RestClient restClient;

    private final ServletWebServerApplicationContext webServerContext;

    @Override
    public WarmUpBuilder configure(final WarmUpBuilder builder) {
        applicationContext.getBeansOfType(RequestMappingHandlerMapping.class).values().stream()
                .map(RequestMappingHandlerMapping::getHandlerMethods)
                .map(Map::entrySet)
                .flatMap(Set::stream)
                .filter(mapping -> mapping.getValue().hasMethodAnnotation(ControllerWarmUp.class))
                .forEach(mappingInfo -> addEndpoint(mappingInfo.getKey(), mappingInfo.getValue(), builder));
        return builder;
    }

    @Override
    public void warmUp(final WarmUpSettings configuration) {
        for (final var endpoint : configuration.endpoints()) {
            callEndpoint(endpoint);
        }
    }

    private void addEndpoint(
            final RequestMappingInfo requestMappingInfo,
            final HandlerMethod handlerMethod,
            final WarmUpBuilder builder) {
        final var requestMethod = requestMappingInfo //
                .getMethodsCondition() //
                .getMethods() //
                .stream() //
                .findAny();
        final var path = requestMappingInfo.getDirectPaths().stream().findAny();
        final var requestBody = getRequestBody(handlerMethod);

        if (requestMethod.isEmpty() || path.isEmpty()) {
            log.warn(
                    "Could not find request for {}. Got method {} and path {}",
                    requestMappingInfo,
                    requestMethod,
                    path);
            return;
        }
        builder.addEndpoint(requestMethod.get().toString(), path.get(), requestBody);
    }

    private Object getRequestBody(final HandlerMethod handlerMethod) {
        final var methodAnnotation = requireNonNull(
                handlerMethod.getMethodAnnotation(ControllerWarmUp.class),
                () -> "The handler method %s should have a @ControllerWarmUp annotation"
                        .formatted(handlerMethod.getMethod().getName()));

        // First: Try a body configured with the annotation
        final var configuredType = Optional.<Class<?>>ofNullable(methodAnnotation.requestBody())
                .filter(clazz -> !clazz.isAssignableFrom(DefaultRequestBodyType.class));
        // Second: Try to find a @RequestBody parameter
        final var annotatedParameterType = configuredType.or(() -> findParameterTypeByAnnotation(handlerMethod));

        return annotatedParameterType.stream()
                .map(Class::getConstructors)
                .flatMap(Stream::of)
                .filter(constructor -> constructor.getParameterCount() == 0)
                .findFirst()
                .flatMap(this::instantiate)
                .orElse(null);
    }

    private Optional<Class<?>> findParameterTypeByAnnotation(final HandlerMethod handlerMethod) {
        return Stream.of(handlerMethod.getMethodParameters())
                .map(MethodParameter::getParameter)
                .filter(parameter -> parameter.isAnnotationPresent(RequestBody.class))
                .<Class<?>>map(Parameter::getType)
                .findFirst();
    }

    private Optional<?> instantiate(final Constructor<?> constructor) {
        try {
            return Optional.of(constructor.newInstance());
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            log.error(
                    "Failed to instantiate body with constructor {}. Will call the endpoint without body.",
                    constructor,
                    e);
            return Optional.empty();
        }
    }

    private void callEndpoint(final Endpoint endpoint) {
        final var port = webServerContext.getWebServer().getPort();
        final var url = "http://localhost:%s/%s".formatted(port, endpoint.path());

        final var method = HttpMethod.valueOf(endpoint.method());
        var spec = restClient //
                .method(method) //
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
