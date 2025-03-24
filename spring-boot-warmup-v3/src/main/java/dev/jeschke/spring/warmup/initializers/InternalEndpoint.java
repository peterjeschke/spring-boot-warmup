package dev.jeschke.spring.warmup.initializers;

import dev.jeschke.spring.warmup.IgnoreBean;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@IgnoreBean // Don't auto-discover this bean. It will be registered manually, if necessary
@RestController
public class InternalEndpoint {

    public static final String INTERNAL_WARM_UP_ENDPOINT = "/internalWarmUpEndpoint";

    @PostMapping(
        value = INTERNAL_WARM_UP_ENDPOINT,
        consumes = APPLICATION_JSON_VALUE,
        produces = APPLICATION_JSON_VALUE)
    public InternalEndpointPayload warmUp(@RequestBody @Validated final InternalEndpointPayload payload) {
        return payload;
    }
}
