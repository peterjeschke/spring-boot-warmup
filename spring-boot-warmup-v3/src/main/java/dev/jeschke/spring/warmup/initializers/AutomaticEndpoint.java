package dev.jeschke.spring.warmup.initializers;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import dev.jeschke.spring.warmup.IgnoreBean;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@IgnoreBean // Don't auto-discover this bean. It will be registered manually, if necessary
@RestController
public class AutomaticEndpoint {

    public static final String AUTOMATIC_WARM_UP_ENDPOINT = "/automaticWarmUpEndpoint";

    @PostMapping(
            value = AUTOMATIC_WARM_UP_ENDPOINT,
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE)
    public AutomaticEndpointRequestBody warmUp(@RequestBody @Validated final AutomaticEndpointRequestBody requestBody) {
        return requestBody;
    }
}
