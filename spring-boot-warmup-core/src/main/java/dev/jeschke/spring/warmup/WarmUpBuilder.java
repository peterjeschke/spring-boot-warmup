package dev.jeschke.spring.warmup;

import java.net.http.HttpClient;

/**
 * Builder to configure the WarmUp library.
 * <p>
 * This interface is implemented by the library; clients should not create custom implementations.
 */
public interface WarmUpBuilder {

    WarmUpBuilder addEndpoint(Endpoint endpoint);

    WarmUpBuilder addEndpoint(String path);

    WarmUpBuilder addEndpoint(String method, String path);

    WarmUpBuilder addEndpoint(String path, Object requestBody);

    WarmUpBuilder addEndpoint(String path, Object requestBody, String contentType);

    WarmUpBuilder addEndpoint(String method, String path, Object requestBody);

    WarmUpBuilder addEndpoint(String method, String path, Object requestBody, String contentType);

    /**
     * Enables the automatic endpoint functionality.
     * This will create an internal endpoint to call during start up.
     * It aims to initialize the web server, (de-)serializer and validation framework.
     * The internal endpoint will be removed after the WarmUp is done.
     * <p>
     * This feature is disabled by default.
     *
     * @see #disableAutomaticMvcWarmUpEndpoint()
     */
    WarmUpBuilder enableAutomaticMvcWarmUpEndpoint();

    /**
     * Disables the automatic endpoint functionality.
     * <p>
     * The feature is disabled by default, you should not need to call this method under normal circumstances.
     *
     * @see #enableAutomaticMvcWarmUpEndpoint()
     */
    WarmUpBuilder disableAutomaticMvcWarmUpEndpoint();

    /**
     * Enables the readiness indicator feature.
     * <p>
     * The readiness indicator will contribute to your health endpoint and reject traffic until all WarmUp features are done.
     * <p>
     * The feature is enabled by default, you should not need to call this method under normal circumstances.
     *
     * @see #disableReadinessIndicator()
     */
    WarmUpBuilder enableReadinessIndicator();

    /**
     * Disables the readiness indicator feature.
     * <p>
     * The feature is enabled by default.
     *
     * @see #enableReadinessIndicator()
     */
    WarmUpBuilder disableReadinessIndicator();

    /**
     * Configures the protocol to use for REST calls. If not changed, the default is set to "http".
     * <p>
     * Of you enable https, you probably also need to set the hostname to match it to the certificate.
     *
     * @param protocol should be either "http" or "https"
     * @see #setHttpHostname(String)
     */
    WarmUpBuilder setRestProtocol(String protocol);

    /**
     * Sets the HTTP client the library will use for REST calls.
     */
    WarmUpBuilder setHttpClient(HttpClient httpClient);

    /**
     * Sets the host name to connect to. If not changed, defaults to localhost.
     * <p>
     * This can be used if your service only responds to the correct hostname.
     */
    WarmUpBuilder setHttpHostname(String hostname);

    /**
     * Construct the final settings.
     * <p>
     * This method is intended for internal use only; clients should not call this method.
     *
     * @hidden
     */
    WarmUpSettings build();
}
