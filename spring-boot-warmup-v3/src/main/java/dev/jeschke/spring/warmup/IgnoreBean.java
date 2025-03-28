package dev.jeschke.spring.warmup;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Beans annotated with this will not automatically be added to the spring context.
 */
@Target(TYPE)
@Retention(RUNTIME)
public @interface IgnoreBean {}
