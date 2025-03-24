package dev.jeschke.spring.warmup;

import java.util.function.UnaryOperator;

@FunctionalInterface
public interface WarmUpCustomizer extends UnaryOperator<WarmUpBuilder> {}
