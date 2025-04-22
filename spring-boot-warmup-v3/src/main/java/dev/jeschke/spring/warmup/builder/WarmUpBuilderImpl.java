package dev.jeschke.spring.warmup.builder;

import static org.springframework.http.MediaType.APPLICATION_JSON;

import dev.jeschke.spring.warmup.Endpoint;
import dev.jeschke.spring.warmup.WarmUpBuilder;
import dev.jeschke.spring.warmup.WarmUpCustomizer;
import dev.jeschke.spring.warmup.internal.RepeatingWarmUpSettings;
import dev.jeschke.spring.warmup.internal.WarmUpSettings;
import java.net.http.HttpClient;
import java.security.GeneralSecurityException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

public class WarmUpBuilderImpl implements WarmUpBuilder {

    private final List<Endpoint> endpoints = new ArrayList<>();
    private boolean enableAutomaticWarmUpEndpoint = false;
    private boolean enableReadinessIndicator = true;
    private String protocol = "http";
    private HttpClient httpClient = null;
    private HttpClient.Builder defaultHttpClient;
    private String httpHostname = "localhost";
    private boolean enableTlsVerification = true;
    private final Collection<RepeatingWarmUpSettings> repeatingWarmUpSettings = new ArrayList<>();

    public WarmUpBuilderImpl(final HttpClient.Builder defaultHttpClient) {
        this.defaultHttpClient = defaultHttpClient;
    }

    @Override
    public WarmUpBuilder addEndpoint(final Endpoint endpoint) {
        endpoints.add(endpoint);
        return this;
    }

    @Override
    public WarmUpBuilder addEndpoint(final String path) {
        endpoints.add(new Endpoint(path));
        return this;
    }

    @Override
    public WarmUpBuilder addEndpoint(final String method, final String path) {
        endpoints.add(new Endpoint(method, path));
        return this;
    }

    @Override
    public WarmUpBuilder addEndpoint(final String path, final Object requestBody) {
        endpoints.add(new Endpoint(path, requestBody, APPLICATION_JSON.toString()));
        return this;
    }

    @Override
    public WarmUpBuilder addEndpoint(final String path, final Object requestBody, final String contentType) {
        endpoints.add(new Endpoint(path, requestBody, contentType));
        return this;
    }

    @Override
    public WarmUpBuilder addEndpoint(final String method, final String path, final Object requestBody) {
        endpoints.add(new Endpoint(method, path, requestBody, APPLICATION_JSON.toString()));
        return this;
    }

    @Override
    public WarmUpBuilder addEndpoint(
            final String method, final String path, final Object requestBody, final String contentType) {
        endpoints.add(new Endpoint(method, path, requestBody, contentType));
        return this;
    }

    @Override
    public WarmUpBuilder enableAutomaticMvcWarmUpEndpoint() {
        enableAutomaticWarmUpEndpoint = true;
        return this;
    }

    @Override
    public WarmUpBuilder disableAutomaticMvcWarmUpEndpoint() {
        enableAutomaticWarmUpEndpoint = false;
        return this;
    }

    @Override
    public WarmUpBuilder enableReadinessIndicator() {
        enableReadinessIndicator = true;
        return this;
    }

    @Override
    public WarmUpBuilder disableReadinessIndicator() {
        enableReadinessIndicator = false;
        return this;
    }

    @Override
    public WarmUpBuilder setRestProtocol(final String protocol) {
        this.protocol = protocol;
        return this;
    }

    @Override
    public WarmUpBuilder setHttpClient(final HttpClient httpClient) {
        this.httpClient = httpClient;
        return this;
    }

    @Override
    public WarmUpBuilder setHttpHostname(final String hostname) {
        this.httpHostname = hostname;
        return this;
    }

    @Override
    public WarmUpBuilder disableHttpTlsVerification() throws GeneralSecurityException {
        final var sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, new TrustManager[] {new TrustAllTrustManager()}, null);

        this.defaultHttpClient = this.defaultHttpClient.sslContext(sslContext);
        this.enableTlsVerification = false;
        return this;
    }

    @Override
    public WarmUpBuilder enableHttpTlsVerification() throws GeneralSecurityException {
        this.defaultHttpClient = this.defaultHttpClient.sslContext(SSLContext.getDefault());
        this.enableTlsVerification = true;
        return this;
    }

    @Override
    public WarmUpBuilder initializingMultipleTimes(
            final int times, final Duration interval, final WarmUpCustomizer customizer) throws Exception {
        repeatingWarmUpSettings.add(new RepeatingWarmUpSettings(
                times,
                interval,
                customizer.apply(new WarmUpBuilderImpl(defaultHttpClient)).build()));
        return this;
    }

    @Override
    public WarmUpSettings build() {
        final var actualHttpClient = this.httpClient == null ? defaultHttpClient.build() : httpClient;
        return new WarmUpSettings(
                endpoints,
                enableAutomaticWarmUpEndpoint,
                enableReadinessIndicator,
                protocol,
                actualHttpClient,
                httpHostname,
                enableTlsVerification,
                repeatingWarmUpSettings);
    }
}
