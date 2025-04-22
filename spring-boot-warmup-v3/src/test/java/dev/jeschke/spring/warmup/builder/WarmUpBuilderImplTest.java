package dev.jeschke.spring.warmup.builder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_XML_VALUE;

import dev.jeschke.spring.warmup.Endpoint;
import dev.jeschke.spring.warmup.WarmUpBuilder;
import dev.jeschke.spring.warmup.internal.RepeatingWarmUpSettings;
import dev.jeschke.spring.warmup.internal.WarmUpSettings;
import java.net.http.HttpClient;
import java.security.GeneralSecurityException;
import java.time.Duration;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WarmUpBuilderImplTest {

    @Mock
    private HttpClient.Builder defaultHttpClientBuilder;

    @Mock
    private HttpClient defaultHttpClient;

    @Mock
    private HttpClient customHttpClient;

    private WarmUpBuilderImpl builder;

    @BeforeEach
    void setUp() {
        builder = new WarmUpBuilderImpl(defaultHttpClientBuilder);
    }

    @ParameterizedTest
    @MethodSource("endpointArguments")
    void addEndpoint(final UnaryOperator<WarmUpBuilder> method, final Endpoint endpoint) {
        final var newBuilder = method.apply(builder);

        final var actual = newBuilder.build();

        assertThat(actual.endpoints()).containsExactly(endpoint);
    }

    private static Stream<Arguments> endpointArguments() {
        final Object requestBody = "body";
        return Stream.of(
                args(builder -> builder.addEndpoint(new Endpoint("path")), new Endpoint("path")),
                args(builder -> builder.addEndpoint("path"), new Endpoint("path")),
                args(builder -> builder.addEndpoint("POST", "path"), new Endpoint("POST", "path")),
                args(
                        builder -> builder.addEndpoint("path", requestBody),
                        new Endpoint("path", requestBody, APPLICATION_JSON_VALUE)),
                args(
                        builder -> builder.addEndpoint("path", requestBody, APPLICATION_XML_VALUE),
                        new Endpoint("path", requestBody, APPLICATION_XML_VALUE)),
                args(
                        builder -> builder.addEndpoint("PUT", "path", requestBody),
                        new Endpoint("PUT", "path", requestBody, APPLICATION_JSON_VALUE)),
                args(
                        builder -> builder.addEndpoint("PUT", "path", requestBody, APPLICATION_XML_VALUE),
                        new Endpoint("PUT", "path", requestBody, APPLICATION_XML_VALUE)));
    }

    private static Arguments args(final UnaryOperator<WarmUpBuilder> method, final Endpoint expectedEndpoint) {
        return arguments(method, expectedEndpoint);
    }

    @Test
    void automaticWarmUpEndpoint_default() {
        final var actual = builder.build();

        assertThat(actual.enableAutomaticMvcEndpoint()).isFalse();
    }

    @Test
    void enableAutomaticMvcWarmUpEndpoint() {
        final var actual = builder.enableAutomaticMvcWarmUpEndpoint().build();

        assertThat(actual.enableAutomaticMvcEndpoint()).isTrue();
    }

    @Test
    void disableAutomaticMvcWarmUpEndpoint() {
        final var actual = builder.enableAutomaticMvcWarmUpEndpoint()
                .disableAutomaticMvcWarmUpEndpoint()
                .build();

        assertThat(actual.enableAutomaticMvcEndpoint()).isFalse();
    }

    @Test
    void readinessIndicator_default() {
        final var actual = builder.build();

        assertThat(actual.enableReadinessIndicator()).isTrue();
    }

    @Test
    void enableReadinessIndicator() {
        final var actual = builder.disableAutomaticMvcWarmUpEndpoint()
                .enableReadinessIndicator()
                .build();

        assertThat(actual.enableReadinessIndicator()).isTrue();
    }

    @Test
    void disableReadinessIndicator() {
        final var actual = builder.disableReadinessIndicator().build();

        assertThat(actual.enableReadinessIndicator()).isFalse();
    }

    @Test
    void setProtocol_default() {
        final var actual = builder.build();

        assertThat(actual.protocol()).isEqualTo("http");
    }

    @Test
    void setProtocol() {
        final var actual = builder.setRestProtocol("https").build();

        assertThat(actual.protocol()).isEqualTo("https");
    }

    @Test
    void setHttpClient_default() {
        when(defaultHttpClientBuilder.build()).thenReturn(defaultHttpClient);
        final var actual = builder.build();

        assertThat(actual.httpClient()).isSameAs(defaultHttpClient);
    }

    @Test
    void setHttpClient() {
        final var actual = builder.setHttpClient(customHttpClient).build();

        assertThat(actual.httpClient()).isSameAs(customHttpClient);
    }

    @Test
    void setHttpHostname_default() {
        final var actual = builder.build();

        assertThat(actual.hostname()).isEqualTo("localhost");
    }

    @Test
    void setHttpHostname() {
        final var actual = builder.setHttpHostname("customName").build();

        assertThat(actual.hostname()).isEqualTo("customName");
    }

    @Test
    void httpTlsVerification_default() {
        final var actual = builder.build();

        assertThat(actual.enableHttpTlsVerification()).isTrue();
    }

    @Test
    void enableHttpTlsVerification() throws GeneralSecurityException {
        when(defaultHttpClientBuilder.sslContext(any())).thenReturn(defaultHttpClientBuilder);

        final var actual =
                builder.enableHttpTlsVerification().enableReadinessIndicator().build();

        verify(defaultHttpClientBuilder).sslContext(any());
        assertThat(actual.enableHttpTlsVerification()).isTrue();
    }

    @Test
    void disableHttpTlsVerification() throws GeneralSecurityException {
        when(defaultHttpClientBuilder.sslContext(any())).thenReturn(defaultHttpClientBuilder);

        final var actual = builder.disableHttpTlsVerification().build();

        verify(defaultHttpClientBuilder).sslContext(any());
        assertThat(actual.enableHttpTlsVerification()).isFalse();
    }

    @Test
    void initializingMultipleTimes_defaultParameterValues() throws Exception {
        final var actual = builder.initializingMultipleTimes(nestedBuilder -> nestedBuilder)
                .build();

        assertThat(actual.repeatingWarmUpSettings())
                .singleElement()
                .extracting(RepeatingWarmUpSettings::times, RepeatingWarmUpSettings::interval)
                .containsExactly(5, Duration.ofMillis(500));
    }

    @Test
    void initializingMultipleTimes() throws Exception {
        final var times = 3;
        final var interval = Duration.ofSeconds(3);
        final var expectedPath = "/multiplePath";

        final var actual = builder.initializingMultipleTimes(
                        times, interval, nestedBuilder -> nestedBuilder.addEndpoint(expectedPath))
                .build();

        assertThat(actual.repeatingWarmUpSettings())
                .singleElement()
                .extracting(RepeatingWarmUpSettings::times, RepeatingWarmUpSettings::interval)
                .containsExactly(times, interval);
        assertThat(actual.repeatingWarmUpSettings())
                .singleElement()
                .extracting(RepeatingWarmUpSettings::settings)
                .extracting(WarmUpSettings::endpoints, InstanceOfAssertFactories.list(Endpoint.class))
                .singleElement()
                .extracting(Endpoint::path)
                .isEqualTo(expectedPath);
    }
}
