package dev.jeschke.spring.warmup;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.boot.actuate.health.Status.OUT_OF_SERVICE;
import static org.springframework.boot.actuate.health.Status.UP;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import dev.jeschke.spring.warmup.application.TestApplication;
import dev.jeschke.spring.warmup.application.TestApplication.BlockingController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = "test.blocking.enable=true")
@SpringBootTest(webEnvironment = RANDOM_PORT, classes = TestApplication.class)
class ReadinessTest {

    @Autowired
    private BlockingController controller;

    @Autowired
    private HealthEndpoint healthEndpoint;

    @Test
    void healthIsUpdated() {
        assertThat(healthEndpoint.health().getStatus()).isEqualTo(OUT_OF_SERVICE);

        controller.unblock();

        await().until(() -> healthEndpoint.health().getStatus().equals(UP));
    }
}
