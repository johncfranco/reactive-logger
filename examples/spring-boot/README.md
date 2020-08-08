# Spring Boot Example

This directory contains the Spring Boot implementation of the example service described in the parent `examples` directory's README.

The MDC initialization is done in the `LogContextFilter` class.

The `GET /delay/{ISO8601 duration}` method is implemented in the `DelayController` class.
That's where you can see the `ReactiveLogger` class in use.
