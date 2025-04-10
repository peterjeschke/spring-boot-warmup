package dev.jeschke.spring.warmup.builder;

import static org.springframework.http.MediaType.APPLICATION_JSON;

import dev.jeschke.spring.warmup.Endpoint;
import dev.jeschke.spring.warmup.WarmUpBuilder;
import dev.jeschke.spring.warmup.WarmUpSettings;
import java.net.http.HttpClient;
import java.util.ArrayList;
import java.util.List;

public class WarmUpBuilderImpl implements WarmUpBuilder {

    private final List<Endpoint> endpoints = new ArrayList<>();
    private boolean enableAutomaticWarmUpEndpoint = false;
    private boolean enableReadinessIndicator = true;
    private String protocol = "http";
    private HttpClient httpClient;
    private String httpHostname = "localhost";

    public WarmUpBuilderImpl(final HttpClient defaultHttpClient) {
        this.httpClient = defaultHttpClient;
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
    public WarmUpSettings build() {
        return new WarmUpSettings(
                endpoints, enableAutomaticWarmUpEndpoint, enableReadinessIndicator, protocol, httpClient, httpHostname);
    }
}
