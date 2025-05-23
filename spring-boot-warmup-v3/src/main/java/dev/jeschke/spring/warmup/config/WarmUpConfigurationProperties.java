package dev.jeschke.spring.warmup.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("warmup")
public record WarmUpConfigurationProperties(HttpConfigurationProperties http) {

    public record HttpConfigurationProperties(InternalEndpointConfigurationProperties internal) {

        /**
         * @param enableAutoSecurity Whether to automatically configure the security for the internal endpoint. Will allowlist all
         *                           requests to the internal endpoint.
         */
        public record InternalEndpointConfigurationProperties(boolean enableAutoSecurity) {}
    }
}
