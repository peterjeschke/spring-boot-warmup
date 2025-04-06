package dev.jeschke.spring.warmup;

import static dev.jeschke.spring.warmup.application.TestApplication.CUSTOMIZER_TEST_REQUEST_BODY;
import static dev.jeschke.spring.warmup.initializers.AutomaticEndpoint.AUTOMATIC_WARM_UP_ENDPOINT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.assertArg;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.test.context.bean.override.mockito.MockReset.NONE;

import dev.jeschke.spring.warmup.application.InitializedTestRequestBody;
import dev.jeschke.spring.warmup.application.TestApplication;
import dev.jeschke.spring.warmup.application.TestMock;
import dev.jeschke.spring.warmup.application.TestRequestBody;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.Getter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.filter.OncePerRequestFilter;

@SpringBootTest(webEnvironment = RANDOM_PORT, classes = TestApplication.class)
class ControllerWarmUpAnnotationTest {

    // Don't reset the mock between tests. The warmUp code only runs when the application starts, so the mock's methods
    // are only called once when the test starts. Were the mock to reset between test cases, it would be impossible to
    // verify on it after the first test case
    @MockitoBean(reset = NONE)
    private TestMock testMock;

    @Autowired
    private HealthEndpoint healthEndpoint;

    @Autowired
    private AutomaticEndpointCheckFilter filter;

    @BeforeEach
    void setUp() {
        waitUntilHealthy();
    }

    private void waitUntilHealthy() {
        await().until(() -> healthEndpoint.health().getStatus() == Status.UP);
    }

    @Test
    void getNoParams() {
        // WarmUpInitializer is called automatically

        verify(testMock).getNoParams();
    }

    @Test
    void deleteNoParams() {
        verify(testMock).deleteNoParams();
    }

    @Test
    void getByCustomizer() {
        verify(testMock).getByCustomizer();
    }

    @Test
    void postByCustomizer() {
        verify(testMock).postByCustomizer(customizerTestRequestBody());
    }

    @Test
    void postWithAnnotationRequestBody() {
        verify(testMock).postWithAnnotationRequestBody(initializedTestRequestBody());
    }

    @Test
    void postWithRequestBodyRequestBody() {
        verify(testMock).postWithRequestBodyParameter(initializedTestRequestBody());
    }

    @Test
    void notAnnotated() {
        verify(testMock, never()).neverCall();
    }

    @Test
    void automaticWarmUpEndpoint() {
        assertThat(filter.isHitAutomaticEndpoint()).isTrue();
    }

    private TestRequestBody customizerTestRequestBody() {
        return assertArg(
                testBody -> assertThat(testBody).usingRecursiveComparison().isEqualTo(CUSTOMIZER_TEST_REQUEST_BODY));
    }

    private TestRequestBody initializedTestRequestBody() {
        return assertArg(testRequestBody ->
                assertThat(testRequestBody).usingRecursiveComparison().isEqualTo(new InitializedTestRequestBody()));
    }

    @Getter
    @Component
    static class AutomaticEndpointCheckFilter extends OncePerRequestFilter {

        private boolean hitAutomaticEndpoint = false;

        @Override
        protected void doFilterInternal(
                final HttpServletRequest request,
                @NonNull final HttpServletResponse response,
                @NonNull final FilterChain filterChain)
                throws ServletException, IOException {
            if (request.getRequestURI().contains(AUTOMATIC_WARM_UP_ENDPOINT)) {
                hitAutomaticEndpoint = true;
            }

            filterChain.doFilter(request, response);
        }
    }
}
