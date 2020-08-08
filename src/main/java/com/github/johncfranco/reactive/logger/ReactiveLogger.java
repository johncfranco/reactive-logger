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

package com.github.johncfranco.reactive.logger;

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

/**
 *  A wrapper around an slf4j {@link Logger} instance that provides an equivalent interface
 *  but wraps each method invocation in a {@link Mono} that defers the invocation
 *  until a subscriber requests it, and runs the invocation on an appropriate {@link Scheduler}.
 *
 *  <p>
 *  By default, the scheduler is that produced by {@link Schedulers#boundedElastic()}, which
 *  allows the wrapped {@link Logger} instance to use blocking I/O.
 *  </p>
 *
 *  <p>
 *  Also supports mapped diagnostic contexts ({@link org.slf4j.MDC}) by propagating a {@link Map}
 *  from the subscriber {@link Context} to the slf4j MDC upon invoking the wrapped logger,
 *  and then clearing the MDC afterward.
 *  The application is responsible for populating the subscriber {@link Context} accordingly,
 *  but doing so is optional if the application does not use MDC.
 *  </p>
 *
 *  <p>
 *  Provides a {@link Builder} class for creating instances.
 *  Where an application might typically use
 *  <code>LoggerFactory.getLogger(SomeClass.class)</code>
 *  to create its logger, the equivalent here would become
 *  <code>ReactiveLogger.builder().withLogger(LoggerFactory.getLogger(SomeClass.class)).build()</code>.
 *  </p>
 *
 *  <p>
 *  For classes that mix reactive and imperative code, access to the wrapped logger is available
 *  from the {@link #imperative()} method.
 *  </p>
 *
 * @author John C. Franco
 * @since 1.0
 */
public class ReactiveLogger {

    /**
     *  The default key to use in the subscriber {@link Context} for the map to use
     *  as the slf4j {@link org.slf4j.MDC}.
     *
     *  Public because the application code is responsible for populating the {@link Context}.
     *  Can be overridden by {@link Builder#withMDCContextKey(String)}, if the application requires it.
     */
    public static final String DEFAULT_REACTOR_CONTEXT_MDC_KEY = "reactive-logger-mdc";

    private static final Scheduler DEFAULT_SCHEDULER = Schedulers.boundedElastic();
    private static final Logger DEFAULT_LOGGER = LoggerFactory.getLogger(ReactiveLogger.class);

    private final Scheduler scheduler;
    private final Logger logger;
    private final String mdcContextKey;

    private ReactiveLogger(final Builder builder) {
        this.scheduler = builder.scheduler;
        this.logger = builder.logger;
        this.mdcContextKey = builder.mdcContextKey;
    }

    /**
     * Create a {@link Builder} for creating one or more ReactiveLogger instances.
     *
     * @return The new builder.
     * @since 1.0
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Return the slf4j {@link Logger} used by this instance.
     *
     * Useful for logging outside the context of a reactive chain,
     * without having to maintain a separate reference to the wrapped logger.
     *
     * @return the slf4j {@link Logger} used by this instance.
     * @since 1.0
     */
    public Logger imperative() {
        return logger;
    }

    /**
     * Return the {@link Scheduler} used by this instance.
     *
     * Useful in conjunction with {@link reactor.core.publisher.Flux#publishOn(Scheduler)},
     * {@link reactor.core.publisher.Flux#cancelOn(Scheduler)}, {@link Mono#publishOn(Scheduler)},
     * and {@link Mono#cancelOn(Scheduler)} when logging imperatively within the various
     * <code>doOn*</code> handlers.
     *
     * @return the {@link Scheduler} used by this instance.
     * @since 1.0
     */
    public Scheduler scheduler() {
        return scheduler;
    }

    /**
     * Read a <code>Map&lt;String, String&gt;</code> from the given subscriber {@link Context}
     * using the context key provided when creating this instance.
     *
     * Useful for populating the {@link org.slf4j.MDC} prior to using the imperative logger.
     *
     * @param context the subscriber {@link Context} to use
     * @return an {@link Optional} containing the map at the key in the context, empty if the key is absent
     * @see MDCSnapshot
     * @since 1.0
     */
    public Optional<Map<String, String>> readMDC(final Context context) {
        return context.getOrEmpty(mdcContextKey);
    }

    /**
     * Return the name of this <code>ReactiveLogger</code> instance.
     *
     * This uses the name of the slf4j {@link Logger} provided when creating this instance.
     *
     * @return name of this logger instance
     * @since 1.0
     */
    public String getName() {
        return logger.getName();
    }

    /**
     * Is the logger instance enabled for the TRACE level?
     *
     * @return True if this Logger is enabled for the TRACE level,
     * false otherwise.
     * @since 1.0
    */
    public boolean isTraceEnabled() {
        return logger.isTraceEnabled();
    }

    /**
     * Log a message at the TRACE level.
     *
     * @param msg the message string to be logged
     * @return A {@link Mono} that makes an equivalent method call on this instance's {@link Logger}
     * and on this instance's {@link Scheduler} when requested.
     * @since 1.0
    */
    public Mono<Context> trace(final String msg) {
        return wrap(() -> logger.trace(msg));
    }

    /**
     * Log a message at the TRACE level according to the specified format
     * and argument.
     *
     * <p>This form avoids superfluous object creation when the logger
     * is disabled for the TRACE level. </p>
     *
     * @param format the format string
     * @param arg    the argument
     * @return A {@link Mono} that makes an equivalent method call on this instance's {@link Logger}
     * and on this instance's {@link Scheduler} when requested.
     * @since 1.0
    */
    public Mono<Context> trace(final String format, final Object arg) {
        return wrap(() -> logger.trace(format, arg));
    }

    /**
     * Log a message at the TRACE level according to the specified format
     * and arguments.
     *
     * <p>This form avoids superfluous object creation when the logger
     * is disabled for the TRACE level. </p>
     *
     * @param format the format string
     * @param arg1   the first argument
     * @param arg2   the second argument
     * @return A {@link Mono} that makes an equivalent method call on this instance's {@link Logger}
     * and on this instance's {@link Scheduler} when requested.
     * @since 1.0
    */
    public Mono<Context> trace(final String format, final Object arg1, final Object arg2) {
        return wrap(() -> logger.trace(format, arg1, arg2));
    }

    /**
     * Log a message at the TRACE level according to the specified format
     * and arguments.
     *
     * <p>This form avoids superfluous string concatenation when the logger
     * is disabled for the TRACE level. However, this variant incurs the hidden
     * (and relatively small) cost of creating an <code>Object[]</code> before invoking the method,
     * even if this logger is disabled for TRACE. The variants taking {@link #trace(String, Object) one} and
     * {@link #trace(String, Object, Object) two} arguments exist solely in order to avoid this hidden cost.</p>
     *
     * @param format    the format string
     * @param arguments a list of 3 or more arguments
     * @return A {@link Mono} that makes an equivalent method call on this instance's {@link Logger}
     * and on this instance's {@link Scheduler} when requested.
     * @since 1.0
    */
    public Mono<Context> trace(final String format, final Object... arguments) {
        return wrap(() -> logger.trace(format, arguments));
    }

    /**
     * Log an exception (throwable) at the TRACE level with an
     * accompanying message.
     *
     * @param msg the message accompanying the exception
     * @param t   the exception (throwable) to log
     * @return A {@link Mono} that makes an equivalent method call on this instance's {@link Logger}
     * and on this instance's {@link Scheduler} when requested.
     * @since 1.0
    */
    public Mono<Context> trace(final String msg, final Throwable t) {
        return wrap(() -> logger.trace(msg, t));
    }

    /**
     * Similar to {@link #isTraceEnabled()} method except that the
     * marker data is also taken into account.
     *
     * @param marker The marker data to take into consideration
     * @return True if this Logger is enabled for the TRACE level,
     * false otherwise.
     * @since 1.0
    */
    public boolean isTraceEnabled(final Marker marker) {
        return logger.isTraceEnabled(marker);
    }

    /**
     * Log a message with the specific Marker at the TRACE level.
     *
     * @param marker the marker data specific to this log statement
     * @param msg    the message string to be logged
     * @return A {@link Mono} that makes an equivalent method call on this instance's {@link Logger}
     * and on this instance's {@link Scheduler} when requested.
     * @since 1.0
    */
    public Mono<Context> trace(final Marker marker, final String msg) {
        return wrap(() -> logger.trace(marker, msg));
    }

    /**
     * This method is similar to {@link #trace(String, Object)} method except that the
     * marker data is also taken into consideration.
     *
     * @param marker the marker data specific to this log statement
     * @param format the format string
     * @param arg    the argument
     * @return A {@link Mono} that makes an equivalent method call on this instance's {@link Logger}
     * and on this instance's {@link Scheduler} when requested.
     * @since 1.0
    */
    public Mono<Context> trace(final Marker marker, final String format, final Object arg) {
        return wrap(() -> logger.trace(marker, format, arg));
    }

    /**
     * This method is similar to {@link #trace(String, Object, Object)}
     * method except that the marker data is also taken into
     * consideration.
     *
     * @param marker the marker data specific to this log statement
     * @param format the format string
     * @param arg1   the first argument
     * @param arg2   the second argument
     * @return A {@link Mono} that makes an equivalent method call on this instance's {@link Logger}
     * and on this instance's {@link Scheduler} when requested.
     * @since 1.0
    */
    public Mono<Context> trace(final Marker marker, final String format, final Object arg1, final Object arg2) {
        return wrap(() -> logger.trace(marker, format, arg1, arg2));
    }

    /**
     * This method is similar to {@link #trace(String, Object...)}
     * method except that the marker data is also taken into
     * consideration.
     *
     * @param marker   the marker data specific to this log statement
     * @param format   the format string
     * @param argArray an array of arguments
     * @return A {@link Mono} that makes an equivalent method call on this instance's {@link Logger}
     * and on this instance's {@link Scheduler} when requested.
     * @since 1.0
    */
    public Mono<Context> trace(final Marker marker, final String format, final Object... argArray) {
        return wrap(() -> logger.trace(marker, format, argArray));
    }

    /**
     * This method is similar to {@link #trace(String, Throwable)} method except that the
     * marker data is also taken into consideration.
     *
     * @param marker the marker data specific to this log statement
     * @param msg    the message accompanying the exception
     * @param t      the exception (throwable) to log
     * @return A {@link Mono} that makes an equivalent method call on this instance's {@link Logger}
     * and on this instance's {@link Scheduler} when requested.
     * @since 1.0
    */
    public Mono<Context> trace(final Marker marker, final String msg, final Throwable t) {
        return wrap(() -> logger.trace(marker, msg, t));
    }

    /**
     * Is the logger instance enabled for the DEBUG level?
     *
     * @return True if this Logger is enabled for the DEBUG level,
     * false otherwise.
     * @since 1.0
     */
    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    /**
     * Log a message at the DEBUG level.
     *
     * @param msg the message string to be logged
     * @return A {@link Mono} that makes an equivalent method call on this instance's {@link Logger}
     * and on this instance's {@link Scheduler} when requested.
     * @since 1.0
     */
    public Mono<Context> debug(final String msg) {
        return wrap(() -> logger.debug(msg));
    }

    /**
     * Log a message at the DEBUG level according to the specified format
     * and argument.
     *
     * <p>This form avoids superfluous object creation when the logger
     * is disabled for the DEBUG level. </p>
     *
     * @param format the format string
     * @param arg    the argument
     * @return A {@link Mono} that makes an equivalent method call on this instance's {@link Logger}
     * and on this instance's {@link Scheduler} when requested.
     * @since 1.0
     */
    public Mono<Context> debug(final String format, final Object arg) {
        return wrap(() -> logger.debug(format, arg));
    }

    /**
     * Log a message at the DEBUG level according to the specified format
     * and arguments.
     *
     * <p>This form avoids superfluous object creation when the logger
     * is disabled for the DEBUG level. </p>
     *
     * @param format the format string
     * @param arg1   the first argument
     * @param arg2   the second argument
     * @return A {@link Mono} that makes an equivalent method call on this instance's {@link Logger}
     * and on this instance's {@link Scheduler} when requested.
     * @since 1.0
     */
    public Mono<Context> debug(final String format, final Object arg1, final Object arg2) {
        return wrap(() -> logger.debug(format, arg1, arg2));
    }

    /**
     * Log a message at the DEBUG level according to the specified format
     * and arguments.
     *
     * <p>This form avoids superfluous string concatenation when the logger
     * is disabled for the DEBUG level. However, this variant incurs the hidden
     * (and relatively small) cost of creating an <code>Object[]</code> before invoking the method,
     * even if this logger is disabled for DEBUG. The variants taking
     * {@link #debug(String, Object) one} and {@link #debug(String, Object, Object) two}
     * arguments exist solely in order to avoid this hidden cost.</p>
     *
     * @param format    the format string
     * @param arguments a list of 3 or more arguments
     * @return A {@link Mono} that makes an equivalent method call on this instance's {@link Logger}
     * and on this instance's {@link Scheduler} when requested.
     * @since 1.0
     */
    public Mono<Context> debug(final String format, final Object... arguments) {
        return wrap(() -> logger.debug(format, arguments));
    }

    /**
     * Log an exception (throwable) at the DEBUG level with an
     * accompanying message.
     *
     * @param msg the message accompanying the exception
     * @param t   the exception (throwable) to log
     * @return A {@link Mono} that makes an equivalent method call on this instance's {@link Logger}
     * and on this instance's {@link Scheduler} when requested.
     * @since 1.0
     */
    public Mono<Context> debug(final String msg, final Throwable t) {
        return wrap(() -> logger.debug(msg, t));
    }

    /**
     * Similar to {@link #isDebugEnabled()} method except that the
     * marker data is also taken into account.
     *
     * @param marker The marker data to take into consideration
     * @return True if this Logger is enabled for the DEBUG level,
     * false otherwise.
     * @since 1.0
     */
    public boolean isDebugEnabled(final Marker marker) {
        return logger.isDebugEnabled(marker);
    }

    /**
     * Log a message with the specific Marker at the DEBUG level.
     *
     * @param marker the marker data specific to this log statement
     * @param msg    the message string to be logged
     * @return A {@link Mono} that makes an equivalent method call on this instance's {@link Logger}
     * and on this instance's {@link Scheduler} when requested.
     * @since 1.0
     */
    public Mono<Context> debug(final Marker marker, final String msg) {
        return wrap(() -> logger.debug(marker, msg));
    }

    /**
     * This method is similar to {@link #debug(String, Object)} method except that the
     * marker data is also taken into consideration.
     *
     * @param marker the marker data specific to this log statement
     * @param format the format string
     * @param arg    the argument
     * @return A {@link Mono} that makes an equivalent method call on this instance's {@link Logger}
     * and on this instance's {@link Scheduler} when requested.
     * @since 1.0
     */
    public Mono<Context> debug(final Marker marker, final String format, final Object arg) {
        return wrap(() -> logger.debug(marker, format, arg));
    }

    /**
     * This method is similar to {@link #debug(String, Object, Object)}
     * method except that the marker data is also taken into
     * consideration.
     *
     * @param marker the marker data specific to this log statement
     * @param format the format string
     * @param arg1   the first argument
     * @param arg2   the second argument
     * @return A {@link Mono} that makes an equivalent method call on this instance's {@link Logger}
     * and on this instance's {@link Scheduler} when requested.
     * @since 1.0
     */
    public Mono<Context> debug(final Marker marker, final String format, final Object arg1, final Object arg2) {
        return wrap(() -> logger.debug(marker, format, arg1, arg2));
    }

    /**
     * This method is similar to {@link #debug(String, Object...)}
     * method except that the marker data is also taken into
     * consideration.
     *
     * @param marker    the marker data specific to this log statement
     * @param format    the format string
     * @param arguments a list of 3 or more arguments
     * @return A {@link Mono} that makes an equivalent method call on this instance's {@link Logger}
     * and on this instance's {@link Scheduler} when requested.
     * @since 1.0
     */
    public Mono<Context> debug(final Marker marker, final String format, final Object... arguments) {
        return wrap(() -> logger.debug(marker, format, arguments));
    }

    /**
     * This method is similar to {@link #debug(String, Throwable)} method except that the
     * marker data is also taken into consideration.
     *
     * @param marker the marker data specific to this log statement
     * @param msg    the message accompanying the exception
     * @param t      the exception (throwable) to log
     * @return A {@link Mono} that makes an equivalent method call on this instance's {@link Logger}
     * and on this instance's {@link Scheduler} when requested.
     * @since 1.0
     */
    public Mono<Context> debug(final Marker marker, final String msg, final Throwable t) {
        return wrap(() -> logger.debug(marker, msg, t));
    }

    /**
     * Is the logger instance enabled for the INFO level?
     *
     * @return True if this Logger is enabled for the INFO level,
     * false otherwise.
     * @since 1.0
     */
    public boolean isInfoEnabled() {
        return logger.isInfoEnabled();
    }

    /**
     * Log a message at the INFO level.
     *
     * @param msg the message string to be logged
     * @return A {@link Mono} that makes an equivalent method call on this instance's {@link Logger}
     * and on this instance's {@link Scheduler} when requested.
     * @since 1.0
     */
    public Mono<Context> info(final String msg) {
        return wrap(() -> logger.info(msg));
    }

    /**
     * Log a message at the INFO level according to the specified format
     * and argument.
     *
     * <p>This form avoids superfluous object creation when the logger
     * is disabled for the INFO level. </p>
     *
     * @param format the format string
     * @param arg    the argument
     * @return A {@link Mono} that makes an equivalent method call on this instance's {@link Logger}
     * and on this instance's {@link Scheduler} when requested.
     * @since 1.0
     */
    public Mono<Context> info(final String format, final Object arg) {
        return wrap(() -> logger.info(format, arg));
    }

    /**
     * Log a message at the INFO level according to the specified format
     * and arguments.
     *
     * <p>This form avoids superfluous object creation when the logger
     * is disabled for the INFO level. </p>
     *
     * @param format the format string
     * @param arg1   the first argument
     * @param arg2   the second argument
     * @return A {@link Mono} that makes an equivalent method call on this instance's {@link Logger}
     * and on this instance's {@link Scheduler} when requested.
     * @since 1.0
     */
    public Mono<Context> info(final String format, final Object arg1, final Object arg2) {
        return wrap(() -> logger.info(format, arg1, arg2));
    }

    /**
     * Log a message at the INFO level according to the specified format
     * and arguments.
     *
     * <p>This form avoids superfluous string concatenation when the logger
     * is disabled for the INFO level. However, this variant incurs the hidden
     * (and relatively small) cost of creating an <code>Object[]</code> before invoking the method,
     * even if this logger is disabled for INFO. The variants taking
     * {@link #info(String, Object) one} and {@link #info(String, Object, Object) two}
     * arguments exist solely in order to avoid this hidden cost.</p>
     *
     * @param format    the format string
     * @param arguments a list of 3 or more arguments
     * @return A {@link Mono} that makes an equivalent method call on this instance's {@link Logger}
     * and on this instance's {@link Scheduler} when requested.
     * @since 1.0
     */
    public Mono<Context> info(final String format, final Object... arguments) {
        return wrap(() -> logger.info(format, arguments));
    }

    /**
     * Log an exception (throwable) at the INFO level with an
     * accompanying message.
     *
     * @param msg the message accompanying the exception
     * @param t   the exception (throwable) to log
     * @return A {@link Mono} that makes an equivalent method call on this instance's {@link Logger}
     * and on this instance's {@link Scheduler} when requested.
     * @since 1.0
     */
    public Mono<Context> info(final String msg, final Throwable t) {
        return wrap(() -> logger.info(msg, t));
    }

    /**
     * Similar to {@link #isInfoEnabled()} method except that the marker
     * data is also taken into consideration.
     *
     * @param marker The marker data to take into consideration
     * @return true if this logger is warn enabled, false otherwise
     * @since 1.0
     */
    public boolean isInfoEnabled(final Marker marker) {
        return logger.isInfoEnabled(marker);
    }

    /**
     * Log a message with the specific Marker at the INFO level.
     *
     * @param marker The marker specific to this log statement
     * @param msg    the message string to be logged
     * @return A {@link Mono} that makes an equivalent method call on this instance's {@link Logger}
     * and on this instance's {@link Scheduler} when requested.
     * @since 1.0
     */
    public Mono<Context> info(final Marker marker, final String msg) {
        return wrap(() -> logger.info(marker, msg));
    }

    /**
     * This method is similar to {@link #info(String, Object)} method except that the
     * marker data is also taken into consideration.
     *
     * @param marker the marker data specific to this log statement
     * @param format the format string
     * @param arg    the argument
     * @return A {@link Mono} that makes an equivalent method call on this instance's {@link Logger}
     * and on this instance's {@link Scheduler} when requested.
     * @since 1.0
     */
    public Mono<Context> info(final Marker marker, final String format, final Object arg) {
        return wrap(() -> logger.info(marker, format, arg));
    }

    /**
     * This method is similar to {@link #info(String, Object, Object)}
     * method except that the marker data is also taken into
     * consideration.
     *
     * @param marker the marker data specific to this log statement
     * @param format the format string
     * @param arg1   the first argument
     * @param arg2   the second argument
     * @return A {@link Mono} that makes an equivalent method call on this instance's {@link Logger}
     * and on this instance's {@link Scheduler} when requested.
     * @since 1.0
     */
    public Mono<Context> info(final Marker marker, final String format, final Object arg1, final Object arg2) {
        return wrap(() -> logger.info(marker, format, arg1, arg2));
    }

    /**
     * This method is similar to {@link #info(String, Object...)}
     * method except that the marker data is also taken into
     * consideration.
     *
     * @param marker    the marker data specific to this log statement
     * @param format    the format string
     * @param arguments a list of 3 or more arguments
     * @return A {@link Mono} that makes an equivalent method call on this instance's {@link Logger}
     * and on this instance's {@link Scheduler} when requested.
     * @since 1.0
     */
    public Mono<Context> info(final Marker marker, final String format, final Object... arguments) {
        return wrap(() -> logger.info(marker, format, arguments));
    }

    /**
     * This method is similar to {@link #info(String, Throwable)} method
     * except that the marker data is also taken into consideration.
     *
     * @param marker the marker data for this log statement
     * @param msg    the message accompanying the exception
     * @param t      the exception (throwable) to log
     * @return A {@link Mono} that makes an equivalent method call on this instance's {@link Logger}
     * and on this instance's {@link Scheduler} when requested.
     * @since 1.0
     */
    public Mono<Context> info(final Marker marker, final String msg, final Throwable t) {
        return wrap(() -> logger.info(marker, msg, t));
    }

    /**
     * Is the logger instance enabled for the WARN level?
     *
     * @return True if this Logger is enabled for the WARN level,
     * false otherwise.
     * @since 1.0
     */
    public boolean isWarnEnabled() {
        return logger.isWarnEnabled();
    }

    /**
     * Log a message at the WARN level.
     *
     * @param msg the message string to be logged
     * @return A {@link Mono} that makes an equivalent method call on this instance's {@link Logger}
     * and on this instance's {@link Scheduler} when requested.
     * @since 1.0
     */
    public Mono<Context> warn(final String msg) {
        return wrap(() -> logger.warn(msg));
    }

    /**
     * Log a message at the WARN level according to the specified format
     * and argument.
     *
     * <p>This form avoids superfluous object creation when the logger
     * is disabled for the WARN level. </p>
     *
     * @param format the format string
     * @param arg    the argument
     * @return A {@link Mono} that makes an equivalent method call on this instance's {@link Logger}
     * and on this instance's {@link Scheduler} when requested.
     * @since 1.0
     */
    public Mono<Context> warn(final String format, final Object arg) {
        return wrap(() -> logger.warn(format, arg));
    }

    /**
     * Log a message at the WARN level according to the specified format
     * and arguments.
     *
     * <p>This form avoids superfluous string concatenation when the logger
     * is disabled for the WARN level. However, this variant incurs the hidden
     * (and relatively small) cost of creating an <code>Object[]</code> before invoking the method,
     * even if this logger is disabled for WARN. The variants taking
     * {@link #warn(String, Object) one} and {@link #warn(String, Object, Object) two}
     * arguments exist solely in order to avoid this hidden cost.</p>
     *
     * @param format    the format string
     * @param arguments a list of 3 or more arguments
     * @return A {@link Mono} that makes an equivalent method call on this instance's {@link Logger}
     * and on this instance's {@link Scheduler} when requested.
     * @since 1.0
     */
    public Mono<Context> warn(final String format, final Object... arguments) {
        return wrap(() -> logger.warn(format, arguments));
    }

    /**
     * Log a message at the WARN level according to the specified format
     * and arguments.
     *
     * <p>This form avoids superfluous object creation when the logger
     * is disabled for the WARN level. </p>
     *
     * @param format the format string
     * @param arg1   the first argument
     * @param arg2   the second argument
     * @return A {@link Mono} that makes an equivalent method call on this instance's {@link Logger}
     * and on this instance's {@link Scheduler} when requested.
     * @since 1.0
     */
    public Mono<Context> warn(final String format, final Object arg1, final Object arg2) {
        return wrap(() -> logger.warn(format, arg1, arg2));
    }

    /**
     * Log an exception (throwable) at the WARN level with an
     * accompanying message.
     *
     * @param msg the message accompanying the exception
     * @param t   the exception (throwable) to log
     * @return A {@link Mono} that makes an equivalent method call on this instance's {@link Logger}
     * and on this instance's {@link Scheduler} when requested.
     * @since 1.0
     */
    public Mono<Context> warn(final String msg, final Throwable t) {
        return wrap(() -> logger.warn(msg, t));
    }

    /**
     * Similar to {@link #isWarnEnabled()} method except that the marker
     * data is also taken into consideration.
     *
     * @param marker The marker data to take into consideration
     * @return True if this Logger is enabled for the WARN level,
     * false otherwise.
     * @since 1.0
     */
    public boolean isWarnEnabled(final Marker marker) {
        return logger.isWarnEnabled(marker);
    }

    /**
     * Log a message with the specific Marker at the WARN level.
     *
     * @param marker The marker specific to this log statement
     * @param msg    the message string to be logged
     * @return A {@link Mono} that makes an equivalent method call on this instance's {@link Logger}
     * and on this instance's {@link Scheduler} when requested.
     * @since 1.0
     */
    public Mono<Context> warn(final Marker marker, final String msg) {
        return wrap(() -> logger.warn(marker, msg));
    }

    /**
     * This method is similar to {@link #warn(String, Object)} method except that the
     * marker data is also taken into consideration.
     *
     * @param marker the marker data specific to this log statement
     * @param format the format string
     * @param arg    the argument
     * @return A {@link Mono} that makes an equivalent method call on this instance's {@link Logger}
     * and on this instance's {@link Scheduler} when requested.
     * @since 1.0
     */
    public Mono<Context> warn(final Marker marker, final String format, final Object arg) {
        return wrap(() -> logger.warn(marker, format, arg));
    }

    /**
     * This method is similar to {@link #warn(String, Object, Object)}
     * method except that the marker data is also taken into
     * consideration.
     *
     * @param marker the marker data specific to this log statement
     * @param format the format string
     * @param arg1   the first argument
     * @param arg2   the second argument
     * @return A {@link Mono} that makes an equivalent method call on this instance's {@link Logger}
     * and on this instance's {@link Scheduler} when requested.
     * @since 1.0
     */
    public Mono<Context> warn(final Marker marker, final String format, final Object arg1, final Object arg2) {
        return wrap(() -> logger.warn(marker, format, arg1, arg2));
    }

    /**
     * This method is similar to {@link #warn(String, Object...)}
     * method except that the marker data is also taken into
     * consideration.
     *
     * @param marker    the marker data specific to this log statement
     * @param format    the format string
     * @param arguments a list of 3 or more arguments
     * @return A {@link Mono} that makes an equivalent method call on this instance's {@link Logger}
     * and on this instance's {@link Scheduler} when requested.
     * @since 1.0
     */
    public Mono<Context> warn(final Marker marker, final String format, final Object... arguments) {
        return wrap(() -> logger.warn(marker, format, arguments));
    }

    /**
     * This method is similar to {@link #warn(String, Throwable)} method
     * except that the marker data is also taken into consideration.
     *
     * @param marker the marker data for this log statement
     * @param msg    the message accompanying the exception
     * @param t      the exception (throwable) to log
     * @return A {@link Mono} that makes an equivalent method call on this instance's {@link Logger}
     * and on this instance's {@link Scheduler} when requested.
     * @since 1.0
     */
    public Mono<Context> warn(final Marker marker, final String msg, final Throwable t) {
        return wrap(() -> logger.warn(marker, msg, t));
    }

    /**
     * Is the logger instance enabled for the ERROR level?
     *
     * @return True if this Logger is enabled for the ERROR level,
     * false otherwise.
     * @since 1.0
     */
    public boolean isErrorEnabled() {
        return logger.isErrorEnabled();
    }

    /**
     * Log a message at the ERROR level.
     *
     * @param msg the message string to be logged
     * @return A {@link Mono} that makes an equivalent method call on this instance's {@link Logger}
     * and on this instance's {@link Scheduler} when requested.
     * @since 1.0
     */
    public Mono<Context> error(final String msg) {
        return wrap(() -> logger.error(msg));
    }

    /**
     * Log a message at the ERROR level according to the specified format
     * and argument.
     *
     * <p>This form avoids superfluous object creation when the logger
     * is disabled for the ERROR level. </p>
     *
     * @param format the format string
     * @param arg    the argument
     * @return A {@link Mono} that makes an equivalent method call on this instance's {@link Logger}
     * and on this instance's {@link Scheduler} when requested.
     * @since 1.0
     */
    public Mono<Context> error(final String format, final Object arg) {
        return wrap(() -> logger.error(format, arg));
    }

    /**
     * Log a message at the ERROR level according to the specified format
     * and arguments.
     *
     * <p>This form avoids superfluous object creation when the logger
     * is disabled for the ERROR level. </p>
     *
     * @param format the format string
     * @param arg1   the first argument
     * @param arg2   the second argument
     * @return A {@link Mono} that makes an equivalent method call on this instance's {@link Logger}
     * and on this instance's {@link Scheduler} when requested.
     * @since 1.0
     */
    public Mono<Context> error(final String format, final Object arg1, final Object arg2) {
        return wrap(() -> logger.error(format, arg1, arg2));
    }

    /**
     * Log a message at the ERROR level according to the specified format
     * and arguments.
     *
     * <p>This form avoids superfluous string concatenation when the logger
     * is disabled for the ERROR level. However, this variant incurs the hidden
     * (and relatively small) cost of creating an <code>Object[]</code> before invoking the method,
     * even if this logger is disabled for ERROR. The variants taking
     * {@link #error(String, Object) one} and {@link #error(String, Object, Object) two}
     * arguments exist solely in order to avoid this hidden cost.</p>
     *
     * @param format    the format string
     * @param arguments a list of 3 or more arguments
     * @return A {@link Mono} that makes an equivalent method call on this instance's {@link Logger}
     * and on this instance's {@link Scheduler} when requested.
     * @since 1.0
     */
    public Mono<Context> error(final String format, final Object... arguments) {
        return wrap(() -> logger.error(format, arguments));
    }

    /**
     * Log an exception (throwable) at the ERROR level with an
     * accompanying message.
     *
     * @param msg the message accompanying the exception
     * @param t   the exception (throwable) to log
     * @return A {@link Mono} that makes an equivalent method call on this instance's {@link Logger}
     * and on this instance's {@link Scheduler} when requested.
     * @since 1.0
     */
    public Mono<Context> error(final String msg, final Throwable t) {
        return wrap(() -> logger.error(msg, t));
    }

    /**
     * Similar to {@link #isErrorEnabled()} method except that the
     * marker data is also taken into consideration.
     *
     * @param marker The marker data to take into consideration
     * @return True if this Logger is enabled for the ERROR level,
     * false otherwise.
     * @since 1.0
     */
    public boolean isErrorEnabled(final Marker marker) {
        return logger.isErrorEnabled(marker);
    }

    /**
     * Log a message with the specific Marker at the ERROR level.
     *
     * @param marker The marker specific to this log statement
     * @param msg    the message string to be logged
     * @return A {@link Mono} that makes an equivalent method call on this instance's {@link Logger}
     * and on this instance's {@link Scheduler} when requested.
     * @since 1.0
     */
    public Mono<Context> error(final Marker marker, final String msg) {
        return wrap(() -> logger.error(marker, msg));
    }

    /**
     * This method is similar to {@link #error(String, Object)} method except that the
     * marker data is also taken into consideration.
     *
     * @param marker the marker data specific to this log statement
     * @param format the format string
     * @param arg    the argument
     * @return A {@link Mono} that makes an equivalent method call on this instance's {@link Logger}
     * and on this instance's {@link Scheduler} when requested.
     * @since 1.0
     */
    public Mono<Context> error(final Marker marker, final String format, final Object arg) {
        return wrap(() -> logger.error(marker, format, arg));
    }

    /**
     * This method is similar to {@link #error(String, Object, Object)}
     * method except that the marker data is also taken into
     * consideration.
     *
     * @param marker the marker data specific to this log statement
     * @param format the format string
     * @param arg1   the first argument
     * @param arg2   the second argument
     * @return A {@link Mono} that makes an equivalent method call on this instance's {@link Logger}
     * and on this instance's {@link Scheduler} when requested.
     * @since 1.0
     */
    public Mono<Context> error(final Marker marker, final String format, final Object arg1, final Object arg2) {
        return wrap(() -> logger.error(marker, format, arg1, arg2));
    }

    /**
     * This method is similar to {@link #error(String, Object...)}
     * method except that the marker data is also taken into
     * consideration.
     *
     * @param marker    the marker data specific to this log statement
     * @param format    the format string
     * @param arguments a list of 3 or more arguments
     * @return A {@link Mono} that makes an equivalent method call on this instance's {@link Logger}
     * and on this instance's {@link Scheduler} when requested.
     * @since 1.0
     */
    public Mono<Context> error(final Marker marker, final String format, final Object... arguments) {
        return wrap(() -> logger.error(marker, format, arguments));
    }

    /**
     * This method is similar to {@link #error(String, Throwable)}
     * method except that the marker data is also taken into
     * consideration.
     *
     * @param marker the marker data specific to this log statement
     * @param msg    the message accompanying the exception
     * @param t      the exception (throwable) to log
     * @return A {@link Mono} that makes an equivalent method call on this instance's {@link Logger}
     * and on this instance's {@link Scheduler} when requested.
     * @since 1.0
     */
    public Mono<Context> error(final Marker marker, final String msg, final Throwable t) {
        return wrap(() -> logger.error(marker, msg, t));
    }

    private Mono<Context> wrap(final Runnable runnable) {
        return Mono.subscriberContext()
                .map(context -> {
                    try (final MDCSnapshot snapshot = MDCSnapshot.of(readMDC(context).orElse(null))) {
                        runnable.run();
                    }
                    return context;
                })
                .subscribeOn(scheduler);
    }

    /**
     *  Fluent-interface builder for {@link ReactiveLogger} instances.
     *
     *  Available from the static {@link ReactiveLogger#builder()} method.
     *
     *  Use the various <code>with*</code> methods to override defaults as desired,
     *  then use the {@link #build()} method to create a {@link ReactiveLogger} instance.
     *
     * @since 1.0
     */
    public static class Builder {
        private Scheduler scheduler = DEFAULT_SCHEDULER;
        private Logger logger = DEFAULT_LOGGER;
        private String mdcContextKey = DEFAULT_REACTOR_CONTEXT_MDC_KEY;

        private Builder() {}

        /**
         * Provide a {@link Logger} instance to wrap.
         *
         * If not provided, the built instance uses a default logger associated with the {@link ReactiveLogger} class.
         *
         * @param logger The logger to wrap.
         * @return This builder, to allow for chaining.
         * @throws NullPointerException if the parameter is null
         * @since 1.0
         */
        public Builder withLogger(final Logger logger) {
            this.logger = Objects.requireNonNull(logger, "logger must not be null");
            return this;
        }

        /**
         * Provide a {@link Scheduler} on which to invoke the wrapped logger.
         *
         * If not provided, the built instance uses the scheduler from {@link Schedulers#boundedElastic()}.
         *
         * @param scheduler The scheduler to use.
         * @return This builder, to allow for chaining.
         * @throws NullPointerException if parameter is null
         * @since 1.0
         */
        public Builder withScheduler(final Scheduler scheduler) {
            this.scheduler = Objects.requireNonNull(scheduler, "scheduler must not be null");
            return this;
        }

        /**
         * Provide a subscriber {@link Context} key to use when propagating a <code>Map&lt;String, String&gt;</code>
         * to the {@link org.slf4j.MDC} upon invoking the wrapped logger.
         *
         * If not provided, the built instance uses {@value #DEFAULT_REACTOR_CONTEXT_MDC_KEY}.
         *
         * @param mdcContextKey The key to use.
         * @return This builder, to allow for chaining.
         * @throws NullPointerException if parameter is null
         * @throws IllegalArgumentException if the parameter is blank
         * @since 1.0
         */
        public Builder withMDCContextKey(final String mdcContextKey) {
            if (mdcContextKey == null) {
                throw new NullPointerException("MDC context key must not be null");
            }

            if (mdcContextKey.trim().isEmpty()) {
                throw new IllegalArgumentException("MDC context key must not be blank");
            }

            this.mdcContextKey = mdcContextKey;
            return this;
        }

        /**
         * Create a {@link ReactiveLogger} instance with parameters according to the builder's current state.
         * @return The created instance
         * @since 1.0
         */
        public ReactiveLogger build() {
            return new ReactiveLogger(this);
        }
    }
}
