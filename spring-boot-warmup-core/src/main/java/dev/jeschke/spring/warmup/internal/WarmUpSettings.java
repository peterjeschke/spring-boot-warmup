package dev.jeschke.spring.warmup.internal;

import dev.jeschke.spring.warmup.Endpoint;
import java.net.http.HttpClient;
import java.util.Collection;

/**
 * For internal use only.
 * @hidden
 */
public record WarmUpSettings(
        Collection<Endpoint> endpoints,
        boolean enableAutomaticMvcEndpoint,
        boolean enableReadinessIndicator,
        String protocol,
        HttpClient httpClient,
        String hostname,
        boolean enableHttpTlsVerification,
        Collection<RepeatingWarmUpSettings> repeatingWarmUpSettings) {}
