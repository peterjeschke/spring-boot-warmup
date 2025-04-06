package dev.jeschke.spring.warmup;

import java.util.function.UnaryOperator;

// @formatter:off
/**
 * Applications can create beans implementing this interface to customize the WarmUp features.
 * <p>
 * Example:
 * <p>
 * {@snippet :
 * @Bean
 * public WarmUpCustomizer warmUpCustomizer() {
 *     return builder -> builder.addEndpoint( "/getPath" ) // add GET endpoint
 *                              .addEndpoint( POST.name(), "/postPath", createRequestBody(), APPLICATION_JSON.toString() ); // add POST endpoint
 * }
 * }
 */
// @formatter:on
@FunctionalInterface
@SuppressWarnings("JavadocDeclaration")
public interface WarmUpCustomizer extends UnaryOperator<WarmUpBuilder> {}
