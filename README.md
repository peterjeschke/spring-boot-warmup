# Spring Boot WarmUp

This helper library provides you tools to "warm up" your Spring Boot application.

Spring uses lazy initialization for most of its components.
The first time a component is used (e.g. the first HTTP request) is very slow, because the application is busy loading
and initializing many classes and beans.
By initializing ("warming up") Spring components during start up, the first action will be much faster.
This is useful if you want to make sure that your clients don't run into any timeouts on their first requests.

## Usage

Include this dependency in your project, e.g. Maven:

```xml

<dependency>
    <groupId>dev.jeschke.spring</groupId>
    <artifactId>spring-boot-warmup-v3</artifactId>
    <version>[latest version]</version>
</dependency>
```

To enable initialization, activate one or more of the features.
The library does not run any initialization without configuration, to make sure that it causes no side effects.

## Features

You can choose to enable one or more initialization methods.
Before enabling them, consider that initialization needs to run features of your application.
This has the possibility to cause side effects, like sending requests to other services, accessing a database, etc.

### Readiness indicator

To make sure that a load balancer does not route traffic to an instance before it is ready, the readiness indicator will
set the health of this service as `OUT_OF_SERVICE` until all WarmUp steps are done.

All WarmUp steps run _after_ context initialization is done.
Without the readiness indicator, the health feature of the Spring Boot actuator will report the service as UP, even
before all WarmUp steps are done.

**Note**: "After context initialization" means that WarmUp will run asynchronously after Spring emits a
`ContextRefreshedEvent`.
If the WarmUp were run synchronously, Spring would only report as "finished" after the event handler is done.
**Note**: This feature is enabled by default.

#### Examples

**Example**: Disable the readiness indicator

```java

@Configuration
public static class TestConfiguration {
    @Bean
    public WarmUpCustomizer warmUpCustomizer() {
        return builder -> builder.disableReadinessIndicator();
    }
}
```

### Calling an existing HTTP endpoint

You can specify an endpoint that will be called during start up.
This will initialize all features that are used by this endpoint.
This is the best choice if you have an endpoint that causes no side effects.
Consider GET endpoints or other endpoints that will not modify any state.

#### Automatic configuration

Annotate endpoints with `@ControllerWarmUp` to have them automatically called after start up.
This works only under the following conditions:

- The path does not contain any mandatory path or query parameters
- The endpoint does not need any request body OR
- A valid request body can be constructed by calling a parameterless constructor

**Note**: The automatic configuration can currently only discover MVC endpoints.
For reactive endpoints, please use the explicit configuration.

#### Explicit configuration

You can register a bean that implements `WarmUpCustomizer` and explicitly configure endpoints.
This allows you to register endpoints that you don't have control over.
It's also possible to set a request body without having to rely on a parameterless constructor.

#### Examples

**Example 1**: Automatic configuration for endpoints without a request body.

```java

@ControllerWarmUp
@GetMapping("/path")
public EntityResponse<?> handle() {
    return doStuff();
}
```

**Example 2**: Automatic configuration for endpoints with request bodies that fully initialize the class in its
constructor.

```java

@ControllerWarmUp
@PostMapping("/path")
public EntityResponse<?> handle(@RequestBody RequestBodyType param) {
    return doStuff( param );
}

//

class RequestBodyType {
    private final String id;

    public RequestBodyType() {
        this.id = "ExampleId";
    }

    // Getter & Setter
}
```

**Example 3**: Automatic configuration, setting a different request body class to construct.

```java

@ControllerWarmUp(requestBody = ExampleRequestBodyType.class)
@PostMapping("/path")
public EntityResponse<?> handle(@RequestBody RequestBodyType param) {
    return doStuff( param );
}

//

class RequestBodyType {
    private String id;

    // Getter & Setter
}

class ExampleRequestBodyType extends RequestBodyType {
    // This class will be used to construct the request body
    public ExampleRequestBodyType() {
        super();
        setId( "ExampleId" );
    }
}
```

** Example 4**: Explicit configuration

```java

@Configuration
public class WarmUpConfiguration {
    @Bean
    public WarmUpCustomizer warmUpCustomizer() {
        return builder -> builder.addEndpoint( "/getPath" ) // add GET endpoint
            .addEndpoint(
                POST.name(),
                "/postPath",
                createRequestBody(),
                APPLICATION_JSON.toString()
            ); // add POST endpoint
    }
}
```

### Calling an automatic endpoint

If you have no existing endpoint available, and you don't want to create one just for warm-up, you can use an endpoint
that's auto-generated by the library.
This will initialize the webserver, your (de-)serializers and (if present) your validation library.

The library will add an endpoint during startup, call it, then remove the endpoint again.

**Note**: Depending on your application, this may not be as effective as calling an existing endpoint.
The automatic endpoint can only initialize a specific set of beans.
Your application might have to initialize more beans during a "real" request.

**Note**: The automatic endpoint is implemented using Spring MVC.
In a reactive application, not all parts of the application will be warmed up.

#### Configuration

You need to register a bean that implements `WarmUpCustomizer` and call `enableAutomaticWarmUpEndpoint()`.

#### Examples

```java

@Configuration
public static class TestConfiguration {
    @Bean
    public WarmUpCustomizer warmUpCustomizer() {
        return builder -> builder.enableAutomaticWarmUpEndpoint();
    }
}
```