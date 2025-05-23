package dev.jeschke.spring.warmup;

import dev.jeschke.spring.warmup.internal.WarmUpSettings;
import java.net.http.HttpClient;
import java.security.GeneralSecurityException;
import java.time.Duration;

/**
 * Builder to configure the WarmUp library.
 * <p>
 * This interface is implemented by the library; clients should not create custom implementations.
 */
@SuppressWarnings("JavadocDeclaration")
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
     * <p>
     * <strong>Note:</strong> If you set a custom HttpClient, other settings like {@link #disableHttpTlsVerification()} will have no effect.
     */
    WarmUpBuilder setHttpClient(HttpClient httpClient);

    /**
     * Sets the host name to connect to. If not changed, defaults to localhost.
     * <p>
     * This can be used if your service only responds to the correct hostname.
     */
    WarmUpBuilder setHttpHostname(String hostname);

    /**
     * Disables TLS certificate verification for REST calls. This is useful if your application only allows https but uses a self-signed
     * certificate or you have to use localhost as the hostname.
     * <p>
     * <strong>This is insecure!</strong> You should always prefer to import custom certificates by configuring your own HTTP client or
     * connect to the real hostname instead of localhost, if possible. If you absolutely have to disable certificate checks, make sure
     * that you don't configure the library to call anything besides your own endpoints on localhost.
     * <p>
     * This feature is enabled by default.
     * <strong>Note:</strong> Enabling or disabling this feature has no effect if you configure a custom HttpClient using
     * {@link #setHttpClient(HttpClient)}
     *
     * @throws GeneralSecurityException if any operations on the underlying SSLContext fail
     * @see #enableHttpTlsVerification()
     */
    WarmUpBuilder disableHttpTlsVerification() throws GeneralSecurityException;

    /**
     * Enables TLS certificate verification for REST calls.
     * <p>
     * This feature is enabled by default.
     * <strong>Note:</strong> Enabling or disabling this feature has no effect if you configure a custom HttpClient using
     * {@link #setHttpClient(HttpClient)}
     *
     * @throws GeneralSecurityException if any operations on the underlying SSLContext fail
     * @see #disableHttpTlsVerification()
     */
    WarmUpBuilder enableHttpTlsVerification() throws GeneralSecurityException;

    /**
     * Modifies the given customizer to call defined endpoints multiple times. This can be useful if your endpoint needs to be called
     * multiple times to "fully" warm up.
     * <p>
     * By default, endpoints will be called five times with 0.5s time between calls. See overrides if you want to change these parameters.
     * <p>
     * <strong>Note:</strong> While valid code, nesting this method multiple times is not supported. Only the first level will be evaluated.
     * <p>
     * {@snippet :
     *  public WarmUpCustomizer warmUpCustomizer() {
     *      return builder->builder.addEndpoint("/getPath") // will be called only once
     *      .initializingMultipleTimes(builder ->
     *          builder.addEndpoint("/getPath2") // will be called multiple times
     *      )
     *      .build();
     *  }
     * }
     * @throws Exception any exception thrown by the customizer
     */
    default WarmUpBuilder initializingMultipleTimes(WarmUpCustomizer customizer) throws Exception {
        return initializingMultipleTimes(5, Duration.ofMillis(500), customizer);
    }

    /**
     * Modifies the given customizer to call defined endpoints multiple times. This can be useful if your endpoint needs to be called
     * multiple times to "fully" warm up.
     * <p>
     * <strong>Note:</strong> While valid code, nesting this method multiple times is not supported. Only the first level will be evaluated.
     * <p>
     * {@snippet :
     *  public WarmUpCustomizer warmUpCustomizer() {
     *      return builder->builder.addEndpoint("/getPath") // will be called only once
     *      .initializingMultipleTimes(5, Duration.ofSeconds(1), builder ->
     *          builder.addEndpoint("/getPath2") // will be called five times
     *      )
     *      .build();
     *  }
     * }
     *
     * @param times    how often the initializers should run
     * @param interval how long to wait between initializer runs
     * @throws Exception any exception thrown by the customizer
     */
    WarmUpBuilder initializingMultipleTimes(int times, Duration interval, WarmUpCustomizer customizer) throws Exception;

    /**
     * Construct the final settings.
     * <p>
     * This method is intended for internal use only; clients should not call this method.
     *
     * @hidden
     */
    WarmUpSettings build();
}
