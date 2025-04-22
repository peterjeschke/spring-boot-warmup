package dev.jeschke.spring.warmup.application;

import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import dev.jeschke.spring.warmup.ControllerWarmUp;
import dev.jeschke.spring.warmup.WarmUpConfiguration;
import dev.jeschke.spring.warmup.WarmUpCustomizer;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@Import(WarmUpConfiguration.class)
public class TestApplication {
    public static final TestRequestBody CUSTOMIZER_TEST_REQUEST_BODY = new TestRequestBody("customizerTestRequestBody");
    public static final int INIT_CALL_COUNT = 3;

    public static void main(final String[] args) {
        SpringApplication.run(TestApplication.class, args);
    }

    @Configuration
    public static class TestConfiguration {
        @Bean
        public WarmUpCustomizer warmUpCustomizer() {
            return builder -> builder.addEndpoint("/getByCustomizer")
                    .addEndpoint(
                            POST.name(), "/postByCustomizer", CUSTOMIZER_TEST_REQUEST_BODY, APPLICATION_JSON.toString())
                    .initializingMultipleTimes(
                            INIT_CALL_COUNT,
                            Duration.ofMillis(100),
                            multipleTimesBuilder -> multipleTimesBuilder.addEndpoint("/getMultipleTimes"))
                    .enableAutomaticMvcWarmUpEndpoint();
        }
    }

    @RestController
    public static class TestController {
        private final TestMock testMock;

        public TestController(final TestMock testMock) {
            this.testMock = testMock;
        }

        @ControllerWarmUp
        @GetMapping("/getNoParams")
        public String getNoParams() {
            testMock.getNoParams();
            return "getNoParams";
        }

        @ControllerWarmUp
        @DeleteMapping("/deleteNoParams")
        public String deleteNoParams() {
            testMock.deleteNoParams();
            return "deleteNoParams";
        }

        @GetMapping("/notAnnotated")
        public String notAnnotated() {
            testMock.neverCall();
            return "notAnnotated";
        }

        @GetMapping("/getByCustomizer")
        public String getByCustomizer() {
            testMock.getByCustomizer();
            return "getByCustomizer";
        }

        @PostMapping("/postByCustomizer")
        public String postByCustomizer(@RequestBody final TestRequestBody requestBody) {
            testMock.postByCustomizer(requestBody);
            return "postByCustomizer";
        }

        @ControllerWarmUp(requestBody = InitializedTestRequestBody.class)
        @PostMapping("/postWithAnnotationRequestBody")
        public String postWithAnnotationRequestBody(@RequestBody final TestRequestBody requestBody) {
            testMock.postWithAnnotationRequestBody(requestBody);
            return "postWithAnnotationRequestBody";
        }

        @ControllerWarmUp
        @PostMapping("/postWithRequestBodyParameter")
        public String postWithRequestBodyParameter(@RequestBody final InitializedTestRequestBody requestBody) {
            testMock.postWithRequestBodyParameter(requestBody);
            return "postWithRequestBodyParameter";
        }

        @GetMapping("/getMultipleTimes")
        public String getMultipleTimes() {
            testMock.getMultipleTimes();
            return "getMultipleTimes";
        }
    }

    @RestController
    @ConditionalOnProperty(name = "test.blocking.enable", havingValue = "true")
    public static class BlockingController {
        private final CountDownLatch countDownLatch = new CountDownLatch(1);

        @ControllerWarmUp
        @GetMapping("/block")
        public String blockUntilContinued() throws InterruptedException {
            countDownLatch.await();
            return "block";
        }

        public void unblock() {
            countDownLatch.countDown();
        }
    }
}
