package dev.jeschke.spring.warmup.application;

import org.springframework.stereotype.Component;

@Component
public class TestMock {

    public void getNoParams() {
    }

    public void deleteNoParams() {
    }

    public void neverCall() {
    }

    public void getByCustomizer() {
    }

    public void postByCustomizer(final TestPayload payload) {
    }

    public void postWithAnnotationPayload(final TestPayload payload) {
    }

    public void postWithRequestBodyPayload(final TestPayload payload) {
    }
}
