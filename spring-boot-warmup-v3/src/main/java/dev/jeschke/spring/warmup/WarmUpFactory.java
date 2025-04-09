package dev.jeschke.spring.warmup;

import static java.util.Objects.requireNonNullElseGet;

import dev.jeschke.spring.warmup.builder.WarmUpBuilderImpl;
import dev.jeschke.spring.warmup.initializers.WarmUpInitializer;
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
    private final HttpClient defaultWarmUpHttpClient;

    private final AtomicReference<WarmUpSettings> cachedSettings = new AtomicReference<>();
    private final AtomicReference<RestClient> cachedRestClient = new AtomicReference<>();

    public WarmUpSettings getSettings(final List<WarmUpInitializer> initializers) {
        return cachedSettings.updateAndGet(
                settings -> requireNonNullElseGet(settings, () -> buildSettings(initializers)));
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

    private WarmUpSettings buildSettings(final List<WarmUpInitializer> initializers) {
        var builder = configureCustomizers(context);
        for (final var initializer : initializers) {
            builder = initializer.configure(builder);
        }
        return builder.build();
    }

    private WarmUpBuilder configureCustomizers(final ApplicationContext applicationContext) {
        WarmUpBuilder result = new WarmUpBuilderImpl(defaultWarmUpHttpClient);
        for (final var customizer :
                applicationContext.getBeansOfType(WarmUpCustomizer.class).values()) {
            result = customizer.apply(result);
        }
        return result;
    }
}
