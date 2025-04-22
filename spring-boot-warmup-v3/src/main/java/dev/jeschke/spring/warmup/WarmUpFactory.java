package dev.jeschke.spring.warmup;

import static java.util.Objects.requireNonNullElseGet;

import dev.jeschke.spring.warmup.builder.WarmUpBuilderImpl;
import dev.jeschke.spring.warmup.initializers.WarmUpInitializer;
import dev.jeschke.spring.warmup.internal.WarmUpSettings;
import java.net.http.HttpClient;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.bind.Name;
import org.springframework.context.ApplicationContext;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class WarmUpFactory {
    private final ApplicationContext context;

    @Name("defaultWarmUpHttpClient")
    private final HttpClient.Builder defaultWarmUpHttpClient;

    private final AtomicReference<Result<WarmUpSettings>> cachedSettings = new AtomicReference<>();
    private final AtomicReference<RestClient> cachedRestClient = new AtomicReference<>();

    // Remove once we have something better for the unreachable code
    @SuppressWarnings("java:S112")
    public WarmUpSettings getSettings(final List<WarmUpInitializer> initializers) throws Exception {
        final var result = cachedSettings.updateAndGet(
                settings -> requireNonNullElseGet(settings, () -> buildSettings(initializers)));
        if (result instanceof Result.Success<WarmUpSettings> success) {
            return success.result();
        } else if (result instanceof Result.Failure<WarmUpSettings> failure) {
            throw failure.cause();
        }
        throw new RuntimeException("unreachable");
    }

    public RestClient getRestClient(final HttpClient httpClient) {
        return cachedRestClient.updateAndGet(
                restClient -> requireNonNullElseGet(restClient, () -> buildRestClient(httpClient)));
    }

    private RestClient buildRestClient(final HttpClient httpClient) {
        return RestClient.builder()
                .requestFactory(new JdkClientHttpRequestFactory(httpClient))
                .build();
    }

    private Result<WarmUpSettings> buildSettings(final List<WarmUpInitializer> initializers) {
        try {
            var builder = configureCustomizers(context);
            for (final var initializer : initializers) {
                builder = initializer.configure(builder);
            }
            return new Result.Success<>(builder.build());
        } catch (Exception e) {
            return new Result.Failure<>(e);
        }
    }
    // Sonar complains that Exception is too generic, but that's what we get, so what can we do
    @SuppressWarnings("java:S112")
    private WarmUpBuilder configureCustomizers(final ApplicationContext applicationContext) throws Exception {
        WarmUpBuilder result = new WarmUpBuilderImpl(defaultWarmUpHttpClient);
        for (final var customizer :
                applicationContext.getBeansOfType(WarmUpCustomizer.class).values()) {
            result = customizer.apply(result);
        }
        return result;
    }

    @SuppressWarnings({"unused", "java:S2326"})
    private sealed interface Result<T> {
        record Success<T>(T result) implements Result<T> {}

        record Failure<T>(Exception cause) implements Result<T> {}
    }
}
