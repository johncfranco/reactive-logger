# `reactive-logger` Examples

This directory contains two implementations of a simple HTTP server that demonstrates how to use the `reactive-logger` library,
including how to use its support for MDC.

The implementations differ in that one uses Spring Boot, and the other does not use Spring at all.

By providing both examples, I hope to demonstrate that the library fits well with a widely-used application framework, while also being sufficiently portable and light-weight for use in lower-level infrastructure.

## The HTTP Server

The HTTP server in the examples is a simple "delay" service.
When it receives a GET request with an ISO9601 duration in the URL path, it waits for the specified duration, then responds with a 200 having a plain text body that has the elapsed time measured on the server for how long it waited, expressed as an ISO9601 duration.

For example, here is a request for a one-second delay sent with curl:
```
% curl --verbose 'http://10.0.0.29:63980/delay/PT1S' ; echo
*   Trying 10.0.0.29...
* TCP_NODELAY set
* Connected to 10.0.0.29 (10.0.0.29) port 63980 (#0)
> GET /delay/PT1S HTTP/1.1
> Host: 10.0.0.29:63980
> User-Agent: curl/7.64.1
> Accept: */*
>
< HTTP/1.1 200 OK
< X-Log-Context-Id: 52525453-07b2-4de3-97ff-77f525b757b1
< content-type: text/plain
< content-length: 8
<
* Connection #0 to host 10.0.0.29 left intact
PT1.005S* Closing connection 0
```

The `X-Log-Context-Id` response header comes from the demonstration of MDC support.
Each example integrates with the example's HTTP server to generate a unique "context ID" for each request it receives and add the ID to the logback MDC for inclusion in log messages generated while handling the request, as below:

```
22:51:11.649 [boundedElastic-1] INFO  c.g.j.r.l.e.r.DelayService - context_id=52525453-07b2-4de3-97ff-77f525b757b1 delay PT1S requested
22:51:12.654 [boundedElastic-1] INFO  c.g.j.r.l.e.r.DelayService - context_id=52525453-07b2-4de3-97ff-77f525b757b1 returning after delay PT1.005S...
```

Note that the encoder pattern in the logback XML configuration file must include a term such as `%mdc` to add the context ID to the log messages as above.

Returning the ID to the client in a response header is a useful practice in service-oriented architectures: if the requesting client records the server's context ID in its own log, then someone investigating why a client received a certain response can use the ID from the client's log to find all related messages in the server's log.
