package dev.jeschke.spring.warmup.builder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_XML_VALUE;

import dev.jeschke.spring.warmup.Endpoint;
import dev.jeschke.spring.warmup.WarmUpBuilder;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WarmUpBuilderImplTest {
    private WarmUpBuilderImpl builder;

    @BeforeEach
    void setUp() {
        builder = new WarmUpBuilderImpl();
    }

    @ParameterizedTest
    @MethodSource("endpointArguments")
    void addEndpoint(final UnaryOperator<WarmUpBuilder> method, final Endpoint endpoint) {
        final var newBuilder = method.apply(builder);

        final var actual = newBuilder.build();

        assertThat(actual.endpoints()).containsExactly(endpoint);
    }

    private static Stream<Arguments> endpointArguments() {
        final Object payload = "payload";
        return Stream.of(
                args(b -> b.addEndpoint(new Endpoint("path")), new Endpoint("path")),
                args(b -> b.addEndpoint("path"), new Endpoint("path")),
                args(b -> b.addEndpoint("POST", "path"), new Endpoint("POST", "path")),
                args(b -> b.addEndpoint("path", payload), new Endpoint("path", payload, APPLICATION_JSON_VALUE)),
                args(
                        b -> b.addEndpoint("path", payload, APPLICATION_XML_VALUE),
                        new Endpoint("path", payload, APPLICATION_XML_VALUE)),
                args(
                        b -> b.addEndpoint("PUT", "path", payload),
                        new Endpoint("PUT", "path", payload, APPLICATION_JSON_VALUE)),
                args(
                        b -> b.addEndpoint("PUT", "path", payload, APPLICATION_XML_VALUE),
                        new Endpoint("PUT", "path", payload, APPLICATION_XML_VALUE)));
    }

    private static Arguments args(UnaryOperator<WarmUpBuilder> method, final Endpoint expectedEndpoint) {
        return arguments(method, expectedEndpoint);
    }

    @Test
    void internalWarmUpEndpoint_default() {
        final var actual = builder.build();

        assertThat(actual.useInternalEndpoint()).isFalse();
    }

    @Test
    void enableIternalWarmUpEndpoint() {
        final var actual = builder.enableInternalWarmUpEndpoint().build();

        assertThat(actual.useInternalEndpoint()).isTrue();
    }

    @Test
    void disableInternalWarmUpEndpoint() {
        final var actual = builder.enableInternalWarmUpEndpoint()
                .disableInternalWarmUpEndpoint()
                .build();

        assertThat(actual.useInternalEndpoint()).isFalse();
    }
}
