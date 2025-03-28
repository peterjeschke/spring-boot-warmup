package dev.jeschke.spring.warmup;

public interface WarmUpBuilder {

    WarmUpBuilder addEndpoint(Endpoint endpoint);

    WarmUpBuilder addEndpoint(String path);

    WarmUpBuilder addEndpoint(String method, String path);

    WarmUpBuilder addEndpoint(String path, Object payload);

    WarmUpBuilder addEndpoint(String path, Object payload, String contentType);

    WarmUpBuilder addEndpoint(String method, String path, Object payload);

    WarmUpBuilder addEndpoint(String method, String path, Object payload, String contentType);

    WarmUpBuilder enableInternalWarmUpEndpoint();

    WarmUpBuilder disableInternalWarmUpEndpoint();

    WarmUpSettings build();
}
