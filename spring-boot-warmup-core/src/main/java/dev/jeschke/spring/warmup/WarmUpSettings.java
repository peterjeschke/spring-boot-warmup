package dev.jeschke.spring.warmup;

import java.util.Collection;

public record WarmUpSettings(Collection<Endpoint> endpoints, boolean useInternalEndpoint) {
}
