package dev.jeschke.spring.warmup.config;

import static dev.jeschke.spring.warmup.initializers.AutomaticEndpoint.AUTOMATIC_WARM_UP_ENDPOINT;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@AutoConfiguration
@ConditionalOnClass({SecurityFilterChain.class, HttpSecurity.class})
@ConditionalOnProperty(value = "warmup.http.internal.enable-auto-security", havingValue = "true", matchIfMissing = true)
public class InternalEndpointSecurityConfiguration {

    @Bean
    @Order(1)
    @SuppressWarnings("java:S4502") // Disabling CSRF is safe, it won't be registered after warmup is done
    public SecurityFilterChain warmUpSecurity(final HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .securityMatcher(new AntPathRequestMatcher(AUTOMATIC_WARM_UP_ENDPOINT))
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(customizer -> customizer.anyRequest().permitAll())
                .build();
    }
}
