package dev.jeschke.spring.warmup.internal;

import java.time.Duration;

/**
 * For internal use only.
 * @hidden
 */
public record RepeatingWarmUpSettings(int times, Duration interval, WarmUpSettings settings) {}
