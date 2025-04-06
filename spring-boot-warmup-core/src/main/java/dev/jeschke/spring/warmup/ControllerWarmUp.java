package dev.jeschke.spring.warmup;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Used to mark request handlers to be called during initialization.
 * Methods with this annotation will be discovered automatically, no further configuration is necessary.
 * <p>
 * This annotation SHOULD NOT be used not request handlers that can cause side effects unless you are okay with the handler being run during start up.
 * <p>
 * Should the annotated method have a parameter annotated with @RequestBody, the initializer will try to automatically instantiate it.
 * This requires that the class has a zero parameter constructor that fully initializes all required fields.
 * If it's not possible to instantiate the parameter type, the handler MIGHT be called without a body.
 * <p>
 * This annotation CANNOT be used for methods that require path or query parameters.
 */
@Target(METHOD)
@Retention(RUNTIME)
public @interface ControllerWarmUp {
    /**
     * A class to use as the body.
     * You can use this if your handler requires a request body, but you can't use the type annotated with @RequestBody.
     * The class needs to have a zero-parameter constructor that fully initializes all required fields.
     *
     * @return the class that will be used to construct the request body.
     */
    Class<?> requestBody() default DefaultRequestBodyType.class;

    /**
     * For internal use only.
     *
     * @hidden
     */
    interface DefaultRequestBodyType {}
}
