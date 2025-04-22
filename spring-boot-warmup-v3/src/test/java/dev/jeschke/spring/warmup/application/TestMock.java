package dev.jeschke.spring.warmup.application;

import org.springframework.stereotype.Component;

@Component
@SuppressWarnings({"unused", "java:S1186"})
public class TestMock {

    public void getNoParams() {}

    public void deleteNoParams() {}

    public void neverCall() {}

    public void getByCustomizer() {}

    public void postByCustomizer(final TestRequestBody body) {}

    public void postWithAnnotationRequestBody(final TestRequestBody body) {}

    public void postWithRequestBodyParameter(final TestRequestBody body) {}

    public void getMultipleTimes() {}
}
