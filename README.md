# reactive-logger

`reactive-logger` is a Java library adapting the `slf4j` logging library for reactive applications.
It treats the various message-writing methods in the slf4j `Logger` interface as blocking I/O by wrapping
each with a `Mono<Context>` that invokes the method on a scheduler appropriate for blocking I/O.
The library has three main goals:
* Provide reactive code with a familiar and natural logging interface analogous to the slf4j `Logger` interface in imperative code.
* Facilitate a novel approach to the three-way trade-off between reliability, performance, and resource consumption that application logs commonly face.
* Obey the rule restricting blocking I/O to bounded elastic schedulers without requiring a specific logging configuration to do so.

## Maven Dependency

This library is new and and I'm still working on making it available in the Maven central repository.
I expect to have this done by the end of August 2020.
I'll update this section once it becomes available.

## Examples of Use

Create an instance of the `ReactiveLogger` class by creating an slf4j `Logger` instance however you usually would for
imperative code, and then passing that to `ReactiveLogger.Builder` and calling `ReactiveLogger.Builder.build()`, as below:

```java
private static final ReactiveLogger log = ReactiveLogger.builder()
        .withLogger(LoggerFactory.getLogger(SomeClass.class))
        .build();
```

Because the message-writing methods return a `Mono<Context>`, you can use them either to start a reactive chain
or within operators such as `flatMap`.

The GitHub repository has an `examples` directory that contains two implementations of the same simple REST server:
one implementation shows the logger in use with Spring Boot, and the other shows the logger's independence of Spring
by implementing the server directly on `reactor-netty`.
The README file in the examples directory has more information about these examples.

## MDC Support

The `ReactiveLogger` class follows a convention for application subscribers to pass a map of key-value pairs through the
reactive chain for inclusion in the slf4j mapped diagnostic context (MDC) when invoking the wrapped slf4j `Logger` instance.
The vehicle for propagating the key-value pairs is the `Context` class from the `reactor-core` library,
and that's why the message-writing methods return a `Mono<Context>` rather than a `Mono<Void>`.

Both the Spring Boot and `reactor-netty` examples in the `examples` directory show how to use this support.

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

More finely-grained trade-offs seem possible -- for example, applying different backpressure policies according to log level, as logback's AsyncAppender does in a rudimentary way -- but I think the best way to do that would be to make fully-reactive implementations of logback and slf4j, as described in the "Potential Improvements" section below, rather than in this library.

## Potential Improvements

The experience of implementing this library led to a couple of ideas for improvements to the `reactor-core`, `slf4j` and `logback` libraries.
One idea seems pretty small, straightforward, and practical, and the other is more of a large, vague intuition.
I'll describe the practical idea first.

### Context Support in Flux and Mono `do*` Event Handlers

The library's MDC support relies on the availability of the subscriber's Context object in the reactive chain, but the Context object currently is not available in the Flux and Mono `do*` event handlers, such as `doOnNext`, `doOnCancel`, and `doFinally`.
This lack of availability poses an obstacle to logging with MDC within those event handlers.

I looked briefly at the `reactor-core` source for these event handlers, and it seems fairly straightforward to add overloads of the event handlers that provide the subscriber's Context object in addition to any other parameters they currently provide.
I'll try opening a pull request on `reactor-core` to add those overloads, and update this project according to the result.

In the meantime, a somewhat clumsy workaround is available by copying the MDC out of the subscriber context and into a local variable, and then copying that local variable into the MDC prior to logging within one of the event handlers.
Both the `reactor-netty` and `spring-boot` examples illustrate this workaround to log with MDC in the `doOnCancel` event handler.

While the workaround does work properly, it requires boilerplate code that distracts from the main work of the reactive chain, and could require repeated MDC copying for chains assembled from multiple sources.

### Fully-Reactive Implementations of `slf4j` and `logback`

As mentioned in the "Background" section above, fully-reactive versions of `slf4j` and `logback` could enable fine-grained backpressure policies per logger and log level that connect seamlessly with the reactive chains in application code, using the same backpressure support that the application does.

One complication is that `reactor-netty` and `reactor-core` currently [rely](https://github.com/reactor/reactor-core/blob/master/reactor-core/src/main/java/reactor/util/Loggers.java) on `slf4j` for their internal logging.
While this might have helped the projects get started more quickly while providing users with help in debugging, I think eventually it would have to change, because it forces applications either to use logback's AsyncAppender or to disable the libraries' internal logging.
I can imagine people commonly forgetting about this restriction and then hitting performance problems in production as a result -- or, remembering the restriction but objecting to it because they need to configure their logging differently.

I would resolve this complication through a still more ambitious project: split the reactive logback library into a low-level `logback-netty` library and a higher-level `logback-reactor` library.

The `logback-netty` library would expose an interface similar to the current `slf4j` interface, but all the log-writing methods would return `ChannelFuture` objects instead of `void`.
The library would have netty-based analogues to all the appenders in the current `logback` library (or at least the more commonly used appenders), and it would know how to read its configuration from an XML file, similarly to how the current `logback` library does.

The `logback-reactor` library would wrap the `logback-netty` library in a manner similar to how `reactor-netty` wraps `netty`, and add the ability to route log requests through backpressure policies based on logger, log level, or both.
An application would specify the backpressure policies just as it would for any `Flux` instances it creates, by chaining backpressure-related operators together.
The `logback-reactor` library also would expose an `slf4j`-like interface, but all the log-writing methods would return a `Mono<Context>` instead of `void`, just as this library (`reactive-logger`) does.

![Dependency graph for hypothetical fully-reactive logging libraries](/logback-netty-high-level.png "Dependency graph for hypothetical fully-reactive logging libraries")

This dependency graph summarizes these ideas.

#### Would it be Overkill?

Making such `logback-netty` and `logback-reactor` libraries as described would be large project, and the logback developers might have future plans that are incompatible with this idea.

Even the changes to `reactor-netty` and `reactor-core` to use `logback-netty` for internal logging might encounter resistance from the Project Reactor developers, if those changes would disrupt large amounts of code.

I think answering the question of whether the work would be worthwhile depends largely on whether reactive programming truly is the future for server-side Java: if so, then I think the foundation provided by `reactor-netty` and `reactor-core` should avoid the logically inconsistent use of an imperative library such as `logback` and the fragile workaround (and layer violation) of requiring the use of AsyncAppender.
If people run into trouble with mysterious deadlocks made more difficult to investigate because the deadlocks also freeze the application log, they'll be more likely to decide that Project Reactor is not fit for production use.

Over the past twenty years, I've encountered several attempts to reap the benefits of non-blocking I/O.
Some of the attempts succeeded because they were application-specific, and that specificity allowed them to make some simplifications that a general-purpose framework such as Project Reactor can't.
The popularity of Node.js is further evidence of the enduring appeal of reactive programming.

So, I think that even if Project Reactor fades away for some reason, the demand for a programming model in Java that takes advantage of non-blocking I/O would not.
I think also that investing early in a solid foundation for non-blocking application logging would accelerate Project Reactor's adoption and help it become the long-term solution for reactive programming in Java.
