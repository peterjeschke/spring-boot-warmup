package dev.jeschke.spring.warmup;

public record Endpoint(String method, String path, Object payload, String contentType) {

    public Endpoint(String path) {
        this("GET", path, null, null);
    }

    public Endpoint(String method, String path) {
        this(method, path, null, null);
    }

    public Endpoint(String path, Object payload, String contentType) {
        this("GET", path, payload, contentType);
    }
}
