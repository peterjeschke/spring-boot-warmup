package dev.jeschke.spring.warmup;

import java.net.http.HttpClient;
import java.util.Collection;

/**
 * For internal use only.
 */
public record WarmUpSettings(
        Collection<Endpoint> endpoints,
        boolean enableAutomaticMvcEndpoint,
        boolean enableReadinessIndicator,
        String protocol,
        HttpClient httpClient) {}
