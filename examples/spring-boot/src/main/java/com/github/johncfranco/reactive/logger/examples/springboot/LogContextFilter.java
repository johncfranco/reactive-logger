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

import com.github.johncfranco.reactive.logger.ReactiveLogger;
import com.google.common.base.Strings;
import org.springframework.lang.NonNull;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

public class LogContextFilter implements WebFilter {
    public static final String DEFAULT_RESPONSE_HEADER_NAME = "X-Log-Context-Id";
    public static final String DEFAULT_MDC_CONTEXT_KEY = ReactiveLogger.DEFAULT_REACTOR_CONTEXT_MDC_KEY;
    public static final String DEFAULT_CONTEXT_FIELD_NAME = "context_id";
    public static final Supplier<String> DEFAULT_CONTEXT_ID_GENERATOR = () -> UUID.randomUUID().toString();

    private final String responseHeaderName;
    private final String mdcContextKey;
    private final String contextLogField;
    private final Supplier<String> contextIdGenerator;

    private LogContextFilter(final Builder builder) {
        responseHeaderName = builder.responseHeaderName;
        mdcContextKey = builder.mdcContextKey;
        contextLogField = builder.contextLogField;
        contextIdGenerator = builder.contextIdGenerator;
    }

    @Override
    public @NonNull Mono<Void> filter(@NonNull final ServerWebExchange serverWebExchange, @NonNull final WebFilterChain webFilterChain) {
        return Mono.fromSupplier(contextIdGenerator)
                .flatMap(contextId -> {
                    serverWebExchange.getResponse().getHeaders().add(responseHeaderName, contextId);
                    final Map<String, String> mdc = new HashMap<>(1);
                    mdc.put(contextLogField, contextId);
                    return webFilterChain.filter(serverWebExchange)
                            .subscriberContext(Context.of(mdcContextKey, mdc));
                });
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String responseHeaderName = DEFAULT_RESPONSE_HEADER_NAME;
        private String mdcContextKey = DEFAULT_MDC_CONTEXT_KEY;
        private String contextLogField = DEFAULT_CONTEXT_FIELD_NAME;
        private Supplier<String> contextIdGenerator = DEFAULT_CONTEXT_ID_GENERATOR;

        private Builder() {
        }

        public Builder withResponseHeaderName(final String headerName) {
            responseHeaderName = Objects.requireNonNull(Strings.emptyToNull(headerName), "header name required");
            return this;
        }

        public Builder withMDCContextKey(final String contextKey) {
            mdcContextKey = Objects.requireNonNull(Strings.emptyToNull(contextKey), "context key required");
            return this;
        }

        public Builder withLogFieldName(final String fieldName) {
            contextLogField = Objects.requireNonNull(Strings.emptyToNull(fieldName), "log field name required");
            return this;
        }

        public Builder withContextIdGenerator(final Supplier<String> contextIdGenerator) {
            this.contextIdGenerator = Objects.requireNonNull(contextIdGenerator, "context ID generator required");
            return this;
        }

        public LogContextFilter build() {
            return new LogContextFilter(this);
        }
    }
}
