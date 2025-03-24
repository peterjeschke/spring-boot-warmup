package dev.jeschke.spring.warmup;

import static dev.jeschke.spring.warmup.application.TestApplication.CUSTOMIZER_TEST_PAYLOAD;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.assertArg;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.test.context.bean.override.mockito.MockReset.NONE;

import dev.jeschke.spring.warmup.application.InitializedTestPayload;
import dev.jeschke.spring.warmup.application.TestApplication;
import dev.jeschke.spring.warmup.application.TestMock;
import dev.jeschke.spring.warmup.application.TestPayload;
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
    private InternalEndpointCheckFilter filter;

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
        verify(testMock).postByCustomizer(customizerTestPayload());
    }

    @Test
    void postWithAnnotationPayload() {
        verify(testMock).postWithAnnotationPayload(initializedTestPayload());
    }

    @Test
    void postWithRequestBodyPayload() {
        verify(testMock).postWithRequestBodyPayload(initializedTestPayload());
    }

    @Test
    void notAnnotated() {
        verify(testMock, never()).neverCall();
    }

    @Test
    void internalWarmUpEndpoint() {
        assertThat(filter.isHitInternalEndpoint()).isTrue();
    }

    private TestPayload customizerTestPayload() {
        return assertArg(testPayload -> {
            assertThat(testPayload).usingRecursiveComparison().isEqualTo(CUSTOMIZER_TEST_PAYLOAD);
        });
    }

    private TestPayload initializedTestPayload() {
        return assertArg(testPayload -> {
            assertThat(testPayload).usingRecursiveComparison().isEqualTo(new InitializedTestPayload());
        });
    }

    @Getter
    @Component
    static class InternalEndpointCheckFilter extends OncePerRequestFilter {

        private boolean hitInternalEndpoint = false;

        @Override
        protected void doFilterInternal(
                final HttpServletRequest request, final HttpServletResponse response, final FilterChain filterChain)
                throws ServletException, IOException {
            if (request.getRequestURI().contains("internalWarmUpEndpoint")) {
                hitInternalEndpoint = true;
            }

            filterChain.doFilter(request, response);
        }
    }
}
