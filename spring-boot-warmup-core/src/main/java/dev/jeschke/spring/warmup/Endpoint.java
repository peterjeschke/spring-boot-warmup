package dev.jeschke.spring.warmup;

/**
 * Describes an endpoint for the controller warm up feature.
 *
 * @param method      the HTTP method to use (e.g. GET)
 * @param path        the API path to call, relative to localhost
 * @param body        the request body to send; null if the request should not include a body
 * @param contentType the content type of the request body
 */
public record Endpoint(String method, String path, Object body, String contentType) {

    /**
     * Describes a GET endpoint
     *
     * @param path the API path to call, relative to localhost
     */
    public Endpoint(final String path) {
        this("GET", path);
    }

    /**
     * Describes a POST endpoint
     *
     * @param path        the API path to call, relative to localhost
     * @param body        the request body to send
     * @param contentType the content type of the request body
     */
    public Endpoint(final String path, final Object body, final String contentType) {
        this("POST", path, body, contentType);
    }

    /**
     * Describes an endpoint without body
     *
     * @param method the HTTP method, (e.g. DELETE)
     * @param path   the API path to call, relative to localhost
     */
    public Endpoint(final String method, final String path) {
        this(method, path, null, null);
    }
}
