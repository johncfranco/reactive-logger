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

package com.github.johncfranco.reactive.logger.examples.springboot;

import com.github.johncfranco.reactive.logger.MDCSnapshot;
import com.github.johncfranco.reactive.logger.ReactiveLogger;
import com.google.common.base.Strings;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;
import reactor.util.function.Tuple2;

import java.time.DateTimeException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@RestController
public class DelayController {
    private static final ReactiveLogger log = ReactiveLogger.builder()
            .withLogger(LoggerFactory.getLogger(DelayController.class))
            .build();

    @GetMapping(value = "/delay/{delayText}", produces = MediaType.TEXT_PLAIN_VALUE)
    public Mono<ResponseEntity<String>> delayRequest(@PathVariable final String delayText) {
        if (Strings.isNullOrEmpty(delayText)) {
            final String errorMessage = "delay parameter missing";
            return log.info(errorMessage)
                    .zipWith(createResponse(HttpStatus.BAD_REQUEST, errorMessage))
                    .map(Tuple2::getT2);
        }

        final Duration delay;
        try {
            delay = Duration.parse(delayText);
        } catch (final DateTimeException e) {
            final String errorMessage = String.format("delay '%s' invalid: %s", delayText, e.getMessage());
            return log.info(errorMessage)
                    .zipWith(createResponse(HttpStatus.BAD_REQUEST, errorMessage))
                    .map(Tuple2::getT2);
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
                        .zipWith(createResponse(HttpStatus.OK, delayLengthText)))
                .map(Tuple2::getT2)
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
                        .zipWith(createResponse(HttpStatus.OK, delayLengthText)))
                .map(Tuple2::getT2)
                .doOnCancel(() -> {
                    try (final MDCSnapshot snapshot = MDCSnapshot.of(logContextMap)) {
                        log.imperative().info("delay request cancelled.");
                    }
                })
                .cancelOn(log.scheduler());
    }

    private Mono<ResponseEntity<String>> createResponse(final HttpStatus status, final String responseBody) {
        return Mono.just(ResponseEntity.status(status)
                .contentType(MediaType.TEXT_PLAIN)
                .body(responseBody));
    }
}
