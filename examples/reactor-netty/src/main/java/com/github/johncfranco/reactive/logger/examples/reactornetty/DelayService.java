/*
Copyright (c) 2020 John C. Franco

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package com.github.johncfranco.reactive.logger.examples.reactornetty;

import com.github.johncfranco.reactive.logger.MDCSnapshot;
import com.github.johncfranco.reactive.logger.ReactiveLogger;
import com.google.common.base.Strings;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.reactivestreams.Publisher;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;
import reactor.util.function.Tuple2;

import java.time.DateTimeException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class DelayService {
    public static final String DELAY_PARAMETER_NAME = "delay";
    public static final String DELAY_PATH = String.format("/delay/{%s}", DELAY_PARAMETER_NAME);

    private static final ReactiveLogger log = ReactiveLogger.builder()
            .withLogger(LoggerFactory.getLogger(DelayService.class))
            .build();

    public Publisher<Void> delayRequest(final HttpServerRequest request, final HttpServerResponse response) {
        final String delayText = request.param(DELAY_PARAMETER_NAME);
        if (Strings.isNullOrEmpty(delayText)) {
            return log.info("delay parameter missing")
                    .and(writeTextResponse(response, HttpResponseStatus.BAD_REQUEST, "delay parameter required"));
        }

        final Duration delay;
        try {
            delay = Duration.parse(delayText);
        } catch (final DateTimeException e) {
            final String errorMessage = String.format("delay '%s' invalid: %s", delayText, e.getMessage());
            return log.info(errorMessage)
                    .and(writeTextResponse(response, HttpResponseStatus.BAD_REQUEST, errorMessage));
        }

        /*
        The following could be simplified by an overload of Mono.doOnCancel that took a Consumer<Context>
        rather than a Runnable.
        The local logContextMap variable would not be necessary, and the chain would start
        with the log.info statement.
        Here's how it would look:

        return log.info("delay {} requested", delayText)
                .zipWith(Mono.delay(delay))
                .elapsed()
                .map(Tuple2::getT1)
                .map(Duration::ofMillis)
                .map(Duration::toString)
                .flatMap(delayLengthText -> log.info("returning after delay {}...", delayLengthText)
                        .and(writeTextResponse(response, HttpResponseStatus.OK, delayLengthText)))
                .doOnCancel(context -> {
                    try (final MDCSnapshot snapshot = MDCSnapshot.of(log.readMDC(context).orElse(null))) {
                        log.imperative().info("delay request cancelled.");
                    }
                })
                .cancelOn(log.scheduler());

        I'll try submitting a pull request to reactor-core and update this example if they accept it.
        */

        final Map<String, String> logContextMap = new HashMap<>(1);
        return Mono.subscriberContext()
                .map(log::readMDC)
                .doOnNext(mdc -> mdc.ifPresent(logContextMap::putAll))
                .then(log.info("delay {} requested", delayText)
                        .zipWith(Mono.delay(delay))
                )
                .elapsed()
                .map(Tuple2::getT1)
                .map(Duration::ofMillis)
                .map(Duration::toString)
                .flatMap(delayLengthText -> log.info("returning after delay {}...", delayLengthText)
                        .and(writeTextResponse(response, HttpResponseStatus.OK, delayLengthText)))
                .doOnCancel(() -> {
                    try (final MDCSnapshot snapshot = MDCSnapshot.of(logContextMap)) {
                        log.imperative().info("delay request cancelled.");
                    }
                })
                .cancelOn(log.scheduler());
    }

    private Publisher<Void> writeTextResponse(final HttpServerResponse response, final HttpResponseStatus status, final String text) {
        response.status(status);
        response.addHeader(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.TEXT_PLAIN);
        response.addHeader(HttpHeaderNames.CONTENT_LENGTH, Integer.toString(text.length()));
        return response.sendString(Mono.just(text));
    }

}
