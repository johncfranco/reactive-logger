# reactive-logger
[![Build Status](https://travis-ci.com/johncfranco/reactive-logger.svg?branch=develop)](https://travis-ci.com/johncfranco/reactive-logger) [![Test Coverage](https://api.codeclimate.com/v1/badges/44f5a5a2e944e5750d15/test_coverage)](https://codeclimate.com/github/johncfranco/reactive-logger/test_coverage)

`reactive-logger` is a Java library adapting the `slf4j` logging library for reactive applications.
It treats the various message-writing methods in the slf4j `Logger` interface as blocking I/O by wrapping
each with a `Mono<Context>` that invokes the method on a scheduler appropriate for blocking I/O.
The library has three main goals:
* Provide reactive code with a familiar and natural logging interface analogous to the slf4j `Logger` interface in imperative code.
* Facilitate a novel approach to the three-way trade-off between reliability, performance, and resource consumption that application logs commonly face.
* Obey the rule restricting blocking I/O to bounded elastic schedulers without requiring a specific logging configuration to do so.

## Maven Dependency

```xml
<dependency>
  <groupId>com.github.johncfranco</groupId>
  <artifactId>reactive-logger</artifactId>
  <version>1.0.1</version>
</dependency>
```

## Examples of Use

The GitHub repository has an `examples` [directory](https://github.com/johncfranco/reactive-logger/tree/develop/examples) that contains two implementations of the same simple REST server:
[one implementation](https://github.com/johncfranco/reactive-logger/tree/develop/examples/spring-boot) shows the logger in use with Spring Boot, and [the other](https://github.com/johncfranco/reactive-logger/tree/develop/examples/reactor-netty) shows the logger's independence of Spring
by implementing the server directly on `reactor-netty`.
The [README file](https://github.com/johncfranco/reactive-logger/blob/develop/examples/README.md) in the examples directory has more information about these examples.

### Creating an Instance

Create an instance of the `ReactiveLogger` class by creating an slf4j `Logger` instance however you usually would for
imperative code, and then passing that to `ReactiveLogger.Builder` and calling `ReactiveLogger.Builder.build()`, as below:

```java
private static final ReactiveLogger log = ReactiveLogger.builder()
        .withLogger(LoggerFactory.getLogger(SomeClass.class))
        .build();
```

By default, a ReactiveLogger instance uses the scheduler returned by `Schedulers.boundedElastic()` for invoking the wrapped slf4j Logger instance, to satisfy the rule restricting blocking I/O to bounded elastic schedulers regardless of the logging configuration.
You can choose a different scheduler through the builder's `withScheduler` method.

### Writing Log Messages In-Flow

Because the message-writing methods return a `Mono<Context>`, you can use them either to start a reactive chain
or within operators such as `flatMap`, and follow them with operators such as `zipWith` to log in parallel with other work.

Here's an example of an in-flow log message:
```java
final Duration delay;
try {
    delay = Duration.parse(delayText);
} catch (final DateTimeException e) {
    final String errorMessage = String.format("delay '%s' invalid: %s", delayText, e.getMessage());
    return log.info(errorMessage)
            .zipWith(createResponse(HttpStatus.BAD_REQUEST, errorMessage))
            .map(Tuple2::getT2);
}

```

### Writing Log Messages in Signal Handlers

Logging within a handler such as `doOnComplete` or `doOnCancel` is not as self-contained as writing messages in-flow.
Application code has to provide the subscriber's context for MDC and ensure that the signal handlers run on an appropriate scheduler.
The `reactive-logger` library provides a few facilities to assist with this case.

#### `doOnComplete` Example

To obtain the subscriber's context for a signal handler such as `doOnComplete`, wrap the chain with the `deferContextual` method of Mono or Flux, and in the signal handler, use `MDCSnapshot` to propagate the logging context from the subscriber's context to the slf4j MDC.

To ensure that the handler runs on the appropriate scheduler, precede the `doOnComplete` call with `publishOn` and pass the logger's scheduler.

Here's the form:
```java
Flux.deferContextual(context -> Flux.someOtherCreationMethod()
    //...some chain of operators...
    .publishOn(log.scheduler())
    .doOnComplete(() -> {
        try (final MDCSnapshot snapshot = log.takeMDCSnapshot(context)) {
            log.imperative().someLoggingStatement("Flux has completed.");
        }
    })
);
```

#### `doOnCancel` Example

The `doOnCancel` case follows the `doOnComplete` example except that it replaces the `publishOn` call with a `cancelOn` call:
```java
Flux.deferContextual(context -> Flux.someOtherCreationMethod()
    //...some chain of operators...
    .doOnCancel(() -> {
        try (final MDCSnapshot snapshot = log.takeMDCSnapshot(context)) {
            log.imperative().someLoggingStatement("Flux was cancelled.");
        }
    })
    .cancelOn(log.scheduler())
);
```

## MDC Support

The `ReactiveLogger` class follows a convention for application subscribers to pass a map of key-value pairs through the
reactive chain for inclusion in the slf4j mapped diagnostic context (MDC) when invoking the wrapped slf4j `Logger` instance.
The vehicle for propagating the key-value pairs is the `Context` class from the `reactor-core` library,
and that's why the message-writing methods return a `Mono<Context>` rather than a `Mono<Void>`.

Both the Spring Boot and `reactor-netty` [examples](https://github.com/johncfranco/reactive-logger/tree/develop/examples) show how to use this support.

## Dependencies

The library depends on only `slf4j-api` and `reactor-core`.

## Background: Application Logging Performance, Reliability, and Resource Consumption

Application logs differ from most other uses of I/O in that organizations often have been willing to trade away some reliability for the sakes of performance and resource consumption.
A prominent historical example is the use of UDP as the transport for `syslog` on Unix.

A contemporary example is logback's `AsyncAppender` class, about which the [documentation](http://logback.qos.ch/manual/appenders.html#AsyncAppender) says the following:

> LOSSY BY DEFAULT IF 80% FULL AsyncAppender buffers events in a BlockingQueue. A worker thread created by AsyncAppender takes events from the head of the queue, and dispatches them to the single appender attached to AsyncAppender. Note that by default, AsyncAppender will drop events of level TRACE, DEBUG and INFO if its queue is 80% full. This strategy has an amazingly favorable effect on performance at the cost of event loss.

Depending on an organization's operational needs and how it satisfies them, such a trade can be appropriate.

On the other hand, minimizing log message loss has benefits, especially in the context of a service-oriented architecture spread across many hosts in a data center, with a log aggregator such as ELK or Splunk that supports sophisticated queries.
Highly-reliable logs that capture relevant details of application activity, together with powerful and scalable query capabilities, offer a wealth of possibilities for metrics, anomaly detection, behavioral analysis and optimization, post-hoc failure investigations, and customer support.

Through the ability to schedule multiple publishers to run in parallel while retaining them in the backpressure of a reactive chain, the reactive programming model provides a path to raising application log reliability while mitigating the performance penalty of doing so.

For example, a log statement recording the parameters of an operation can execute in parallel with the operation, such as by the Mono `and` or `zipWith` operators, so that the total elapsed time is the longer of the two, rather than the sum of the two.

The inclusion of the log statements in backpressure prevents the application from outrunning the capacity of log I/O, and so avoids the need for a queue such as that used by logback's AsyncAppender.
This is consistent with the premise of an organization that wants reliable application logs: if log I/O becomes the bottleneck, such an organization might choose to expand log I/O capacity rather than drop messages.

More finely-grained trade-offs seem possible -- for example, applying different backpressure policies according to log level, as logback's AsyncAppender does in a rudimentary way -- but I think the best way to do that would be to make fully-reactive equivalents of logback and slf4j, rather than in this library.

## Recommendations for `logback` Configuration

By default, the `reactor-core` and `reactor-netty` libraries [use slf4j](https://github.com/reactor/reactor-core/blob/master/reactor-core/src/main/java/reactor/util/Loggers.java) for their internal logging if slf4j is on the classpath.
There is no guarantee that their internal logging can tolerate blocking I/O, and the `projectreactor.io` [documentation](https://projectreactor.io/docs/core/release/reference/#_logging_a_sequence) has the following warning:
> In all cases, when logging in production you should take care to configure the underlying logging framework to use its most asynchronous and non-blocking approach — for instance, an AsyncAppender in Logback or AsyncLogger in Log4j 2.

There are a couple of ways to accommodate this warning without sacrificing reliability for your application's logs:
* Disable all `reactor-core` and `reactor-netty` logging entirely.
* Configure an `AsyncAppender` for use only by `reactor-core` and `reactor-netty`, and point it at whatever blocking appender the rest of the application is using.
Both of these approaches rely only on the `logback` XML configuration file.

### Disabling All `reactor-core` and `reactor-netty` Logging

The simplest and safest approach is simply to disable `reactor-core` and `reactor-netty` logging entirely.
Here's an example of how to do that:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>

    <appender name="APPLICATION" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <!-- application appender details not pertinent to this example -->
    </appender>

    <logger name="reactor" level="OFF"/>
    <logger name="io.netty" level="OFF"/>

    <root level="debug">
        <appender-ref ref="APPLICATION" />
    </root>
</configuration>
```

If that's too drastic and you want to see at least some logging from `reactor-core` and `reactor-netty`, consider the next approach.

### Dedicated `AsyncAppender` for `reactor-core` and `reactor-netty`

You can see at least some logging from `reactor-core` and `reactor-netty` without blocking them, and without sacrificing the reliability of your application's logs, by giving `reactor-core` and `reactor-netty` a dedicated `AsyncAppender` pointed at your application's main appender.
Here's an example of how to do that:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>

    <appender name="APPLICATION" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <!-- application appender details not pertinent to this example -->
    </appender>

    <appender name="REACTOR-ASYNC" class="ch.qos.logback.classic.AsyncAppender">
        <queueSize>10</queueSize> <!-- keep memory consumption low -->
        <neverBlock>true</neverBlock> <!-- drop messages rather than block on the queue -->
        <appender-ref ref="APPLICATION"/>
    </appender>

    <!-- set additivity to false on the reactor loggers to exclude the root's appender -->
    <!-- (otherwise each message they generate would go to both appenders) -->
    <logger name="reactor" additivity="false" level="debug">
        <appender-ref ref="REACTOR-ASYNC"/>
    </logger>
    <logger name="io.netty" additivity="false" level="debug">
        <appender-ref ref="REACTOR-ASYNC"/>
    </logger>

    <root level="debug">
        <appender-ref ref="APPLICATION"/>
    </root>
</configuration>
```

The [examples](https://github.com/johncfranco/reactive-logger/tree/develop/examples) use this approach, and the [Spring Boot example](https://github.com/johncfranco/reactive-logger/blob/develop/examples/spring-boot/src/main/resources/logback.xml) routes all Spring log messages through the AsyncAppender as well.
