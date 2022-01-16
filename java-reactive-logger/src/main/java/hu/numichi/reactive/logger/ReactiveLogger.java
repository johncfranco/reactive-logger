package hu.numichi.reactive.logger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.util.context.Context;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class ReactiveLogger {
    public static final String DEFAULT_REACTOR_CONTEXT_MDC_KEY = "reactive-logger-mdc";
    public static final Scheduler DEFAULT_SCHEDULER = Schedulers.boundedElastic();
    private static final Logger DEFAULT_LOGGER = LoggerFactory.getLogger(ReactiveLogger.class);
    private final Scheduler scheduler;
    private final Logger logger;
    private final String mdcContextKey;

    private ReactiveLogger(final Builder builder) {
        this.scheduler = builder.scheduler;
        this.logger = builder.logger;
        this.mdcContextKey = builder.mdcContextKey;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Logger imperative() {
        return logger;
    }

    public Scheduler scheduler() {
        return scheduler;
    }

    public Optional<Map<String, String>> readMDC(final Context context) {
        return context.getOrEmpty(mdcContextKey);
    }

    public String getName() {
        return logger.getName();
    }
    
    //region Trace
    public boolean isTraceEnabled() {
        return logger.isTraceEnabled();
    }
    
    public Mono<Context> trace(final String msg) {
        return wrap(() -> logger.trace(msg));
    }

    public Mono<Context> trace(final String format, final Object arg) {
        return wrap(() -> logger.trace(format, arg));
    }

    public Mono<Context> trace(final String format, final Object arg1, final Object arg2) {
        return wrap(() -> logger.trace(format, arg1, arg2));
    }

    public Mono<Context> trace(final String format, final Object... arguments) {
        return wrap(() -> logger.trace(format, arguments));
    }

    public Mono<Context> trace(final String msg, final Throwable t) {
        return wrap(() -> logger.trace(msg, t));
    }

    public boolean isTraceEnabled(final Marker marker) {
        return logger.isTraceEnabled(marker);
    }
    
    public Mono<Context> trace(final Marker marker, final String msg) {
        return wrap(() -> logger.trace(marker, msg));
    }

    public Mono<Context> trace(final Marker marker, final String format, final Object arg) {
        return wrap(() -> logger.trace(marker, format, arg));
    }

    public Mono<Context> trace(final Marker marker, final String format, final Object arg1, final Object arg2) {
        return wrap(() -> logger.trace(marker, format, arg1, arg2));
    }

    public Mono<Context> trace(final Marker marker, final String format, final Object... argArray) {
        return wrap(() -> logger.trace(marker, format, argArray));
    }

    public Mono<Context> trace(final Marker marker, final String msg, final Throwable t) {
        return wrap(() -> logger.trace(marker, msg, t));
    }
    //endregion
    
    //region Debug
    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    public Mono<Context> debug(final String msg) {
        return wrap(() -> logger.debug(msg));
    }

    public Mono<Context> debug(final String format, final Object arg) {
        return wrap(() -> logger.debug(format, arg));
    }

    public Mono<Context> debug(final String format, final Object arg1, final Object arg2) {
        return wrap(() -> logger.debug(format, arg1, arg2));
    }

    public Mono<Context> debug(final String format, final Object... arguments) {
        return wrap(() -> logger.debug(format, arguments));
    }

    public Mono<Context> debug(final String msg, final Throwable t) {
        return wrap(() -> logger.debug(msg, t));
    }

    public boolean isDebugEnabled(final Marker marker) {
        return logger.isDebugEnabled(marker);
    }

    public Mono<Context> debug(final Marker marker, final String msg) {
        return wrap(() -> logger.debug(marker, msg));
    }

    public Mono<Context> debug(final Marker marker, final String format, final Object arg) {
        return wrap(() -> logger.debug(marker, format, arg));
    }

    public Mono<Context> debug(final Marker marker, final String format, final Object arg1, final Object arg2) {
        return wrap(() -> logger.debug(marker, format, arg1, arg2));
    }

    public Mono<Context> debug(final Marker marker, final String format, final Object... arguments) {
        return wrap(() -> logger.debug(marker, format, arguments));
    }

    public Mono<Context> debug(final Marker marker, final String msg, final Throwable t) {
        return wrap(() -> logger.debug(marker, msg, t));
    }
    //endregion
    
    //region Info
    public boolean isInfoEnabled() {
        return logger.isInfoEnabled();
    }

    public Mono<Context> info(final String msg) {
        return wrap(() -> logger.info(msg));
    }

    public Mono<Context> info(final String format, final Object arg) {
        return wrap(() -> logger.info(format, arg));
    }

    public Mono<Context> info(final String format, final Object arg1, final Object arg2) {
        return wrap(() -> logger.info(format, arg1, arg2));
    }

    public Mono<Context> info(final String format, final Object... arguments) {
        return wrap(() -> logger.info(format, arguments));
    }

    public Mono<Context> info(final String msg, final Throwable t) {
        return wrap(() -> logger.info(msg, t));
    }

    public boolean isInfoEnabled(final Marker marker) {
        return logger.isInfoEnabled(marker);
    }

    public Mono<Context> info(final Marker marker, final String msg) {
        return wrap(() -> logger.info(marker, msg));
    }

    public Mono<Context> info(final Marker marker, final String format, final Object arg) {
        return wrap(() -> logger.info(marker, format, arg));
    }

    public Mono<Context> info(final Marker marker, final String format, final Object arg1, final Object arg2) {
        return wrap(() -> logger.info(marker, format, arg1, arg2));
    }

    public Mono<Context> info(final Marker marker, final String format, final Object... arguments) {
        return wrap(() -> logger.info(marker, format, arguments));
    }

    public Mono<Context> info(final Marker marker, final String msg, final Throwable t) {
        return wrap(() -> logger.info(marker, msg, t));
    }
    //endregion
    
    //region Warn
    public boolean isWarnEnabled() {
        return logger.isWarnEnabled();
    }

    public Mono<Context> warn(final String msg) {
        return wrap(() -> logger.warn(msg));
    }

    public Mono<Context> warn(final String format, final Object arg) {
        return wrap(() -> logger.warn(format, arg));
    }

    public Mono<Context> warn(final String format, final Object... arguments) {
        return wrap(() -> logger.warn(format, arguments));
    }

    public Mono<Context> warn(final String format, final Object arg1, final Object arg2) {
        return wrap(() -> logger.warn(format, arg1, arg2));
    }
    
    public Mono<Context> warn(final String msg, final Throwable t) {
        return wrap(() -> logger.warn(msg, t));
    }

    public boolean isWarnEnabled(final Marker marker) {
        return logger.isWarnEnabled(marker);
    }

    public Mono<Context> warn(final Marker marker, final String msg) {
        return wrap(() -> logger.warn(marker, msg));
    }

    public Mono<Context> warn(final Marker marker, final String format, final Object arg) {
        return wrap(() -> logger.warn(marker, format, arg));
    }

    public Mono<Context> warn(final Marker marker, final String format, final Object arg1, final Object arg2) {
        return wrap(() -> logger.warn(marker, format, arg1, arg2));
    }

    public Mono<Context> warn(final Marker marker, final String format, final Object... arguments) {
        return wrap(() -> logger.warn(marker, format, arguments));
    }

    public Mono<Context> warn(final Marker marker, final String msg, final Throwable t) {
        return wrap(() -> logger.warn(marker, msg, t));
    }
    //endregion
    
    //region Error
    public boolean isErrorEnabled() {
        return logger.isErrorEnabled();
    }

    public Mono<Context> error(final String msg) {
        return wrap(() -> logger.error(msg));
    }

    public Mono<Context> error(final String format, final Object arg) {
        return wrap(() -> logger.error(format, arg));
    }

    public Mono<Context> error(final String format, final Object arg1, final Object arg2) {
        return wrap(() -> logger.error(format, arg1, arg2));
    }

    public Mono<Context> error(final String format, final Object... arguments) {
        return wrap(() -> logger.error(format, arguments));
    }

    public Mono<Context> error(final String msg, final Throwable t) {
        return wrap(() -> logger.error(msg, t));
    }

    public boolean isErrorEnabled(final Marker marker) {
        return logger.isErrorEnabled(marker);
    }

    public Mono<Context> error(final Marker marker, final String msg) {
        return wrap(() -> logger.error(marker, msg));
    }

    public Mono<Context> error(final Marker marker, final String format, final Object arg) {
        return wrap(() -> logger.error(marker, format, arg));
    }

    public Mono<Context> error(final Marker marker, final String format, final Object arg1, final Object arg2) {
        return wrap(() -> logger.error(marker, format, arg1, arg2));
    }

    public Mono<Context> error(final Marker marker, final String format, final Object... arguments) {
        return wrap(() -> logger.error(marker, format, arguments));
    }

    public Mono<Context> error(final Marker marker, final String msg, final Throwable t) {
        return wrap(() -> logger.error(marker, msg, t));
    }
    //endregion
    
    private MDCSnapshot takeMDCSnapshot(final Context context) {
        return readMDC(context).map(MDCSnapshot::of).orElseGet(MDCSnapshot::empty);
    }
    
    private Mono<Context> wrap(final Runnable runnable) {
        return Mono.deferContextual(contextView -> {
            Context context = Context.of(contextView);
            
            try (final MDCSnapshot snapshot = takeMDCSnapshot(context)) {
                runnable.run();
            }
            
            return Mono.just(context);
        }).subscribeOn(scheduler);
    }

    public static class Builder {
        private Scheduler scheduler = DEFAULT_SCHEDULER;
        private Logger logger = DEFAULT_LOGGER;
        private String mdcContextKey = DEFAULT_REACTOR_CONTEXT_MDC_KEY;

        private Builder() {}

        public Builder withLogger(final Logger logger) {
            this.logger = Objects.requireNonNull(logger, "logger must not be null");
            return this;
        }

        public Builder withScheduler(final Scheduler scheduler) {
            this.scheduler = Objects.requireNonNull(scheduler, "scheduler must not be null");
            return this;
        }

        public Builder withMDCContextKey(final String mdcContextKey) {
            if (mdcContextKey == null) {
                throw new IllegalArgumentException("MDC context key must not be null");
            }

            if (mdcContextKey.trim().isEmpty()) {
                throw new IllegalArgumentException("MDC context key must not be blank");
            }

            this.mdcContextKey = mdcContextKey;
            return this;
        }

        public ReactiveLogger build() {
            return new ReactiveLogger(this);
        }
    }
}
