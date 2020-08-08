# `reactor-netty` Example

This directory contains the non-Spring implementation of the example service described in the parent `examples` directory's README.
It uses the `HttpServer` class from `reactor-netty` as its application server.

The MDC initialization is done in the `LogContextHttpMethodWrapper` class.

The `GET /delay/{ISO8601 duration}` method is implemented in the `DelayService` class.
That's where you can see the `ReactiveLogger` class in use.
