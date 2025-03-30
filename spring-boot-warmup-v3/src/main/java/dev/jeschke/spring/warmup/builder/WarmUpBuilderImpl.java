package dev.jeschke.spring.warmup.builder;

import static org.springframework.http.MediaType.APPLICATION_JSON;

import dev.jeschke.spring.warmup.Endpoint;
import dev.jeschke.spring.warmup.WarmUpBuilder;
import dev.jeschke.spring.warmup.WarmUpSettings;
import java.util.ArrayList;
import java.util.List;

public class WarmUpBuilderImpl implements WarmUpBuilder {

    private final List<Endpoint> endpoints = new ArrayList<>();
    private boolean enableInternalWarmUpEndpoint = false;

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
    public WarmUpBuilder addEndpoint(final String path, final Object payload) {
        endpoints.add(new Endpoint(path, payload, APPLICATION_JSON.toString()));
        return this;
    }

    @Override
    public WarmUpBuilder addEndpoint(final String path, final Object payload, final String contentType) {
        endpoints.add(new Endpoint(path, payload, contentType));
        return this;
    }

    @Override
    public WarmUpBuilder addEndpoint(final String method, final String path, final Object payload) {
        endpoints.add(new Endpoint(method, path, payload, APPLICATION_JSON.toString()));
        return this;
    }

    @Override
    public WarmUpBuilder addEndpoint(
            final String method, final String path, final Object payload, final String contentType) {
        endpoints.add(new Endpoint(method, path, payload, contentType));
        return this;
    }

    @Override
    public WarmUpBuilder enableInternalWarmUpEndpoint() {
        enableInternalWarmUpEndpoint = true;
        return this;
    }

    @Override
    public WarmUpBuilder disableInternalWarmUpEndpoint() {
        enableInternalWarmUpEndpoint = false;
        return this;
    }

    @Override
    public WarmUpSettings build() {
        return new WarmUpSettings(endpoints, enableInternalWarmUpEndpoint);
    }
}
