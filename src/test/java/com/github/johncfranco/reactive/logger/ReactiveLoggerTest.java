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

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.Logger;
import org.slf4j.MDC;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.util.context.Context;

import java.util.Map;
import java.util.Optional;

import static com.github.johncfranco.reactive.logger.TestHelpers.randomText;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ReactiveLoggerTest {

    private static ReactiveLogger logger;
    private static Logger imperativeLogger;

    @BeforeAll
    public static void beforeAll() {
        imperativeLogger = mock(Logger.class);
        logger = ReactiveLogger.builder().withLogger(imperativeLogger).build();
    }

    @AfterEach
    public void afterEach() {
        reset(imperativeLogger);
    }

    @Test
    public void getName() {
        final String name = randomText();
        when(imperativeLogger.getName()).thenReturn(name);

        assertThat(logger.getName(), equalTo(name));
    }

    @Test
    public void readMDC() {
        final Map<String, String> mdc = ImmutableMap.of(randomText(), randomText());
        final Context context = Context.of(ReactiveLogger.DEFAULT_REACTOR_CONTEXT_MDC_KEY, mdc);
        assertThat(logger.readMDC(context), equalTo(Optional.of(mdc)));
    }

    @Test
    public void takeMDCSnapshot() {
        final Map<String, String> mdc = ImmutableMap.of(randomText(), randomText());
        final Context context = Context.of(ReactiveLogger.DEFAULT_REACTOR_CONTEXT_MDC_KEY, mdc);
        try (final MDCSnapshot snapshot = logger.takeMDCSnapshot(context)) {
            assertThat(MDC.getCopyOfContextMap(), equalTo(mdc));
        }
    }

    @Test
    public void imperative() {
        assertThat(logger.imperative(), sameInstance(imperativeLogger));
    }

    @Test
    public void scheduler() {
        final Scheduler customScheduler = Schedulers.newBoundedElastic(10, 10, randomText());
        final ReactiveLogger loggerWithCustomScheduler = ReactiveLogger.builder().withScheduler(customScheduler).build();
        assertThat(loggerWithCustomScheduler.scheduler(), sameInstance(customScheduler));
    }

    @Test
    public void traceEnabled() {
        when(imperativeLogger.isTraceEnabled()).thenReturn(true, false, true);
        assertThat("trace not enabled when it should be", logger.isTraceEnabled());
        assertThat("trace enabled when it should not be", !logger.isTraceEnabled());
        assertThat("trace not enabled when it should be", logger.isTraceEnabled());
    }

    @Test
    public void traceEnabledMarker() {
        final Marker marker = MarkerFactory.getMarker(randomText());
        when(imperativeLogger.isTraceEnabled(eq(marker))).thenReturn(true, false, true);
        assertThat("trace not enabled when it should be", logger.isTraceEnabled(marker));
        assertThat("trace enabled when it should not be", !logger.isTraceEnabled(marker));
        assertThat("trace not enabled when it should be", logger.isTraceEnabled(marker));
    }

    @Test
    public void debugEnabled() {
        when(imperativeLogger.isDebugEnabled()).thenReturn(true, false, true);
        assertThat("debug not enabled when it should be", logger.isDebugEnabled());
        assertThat("debug enabled when it should not be", !logger.isDebugEnabled());
        assertThat("debug not enabled when it should be", logger.isDebugEnabled());
    }

    @Test
    public void debugEnabledMarker() {
        final Marker marker = MarkerFactory.getMarker(randomText());
        when(imperativeLogger.isDebugEnabled(eq(marker))).thenReturn(true, false, true);
        assertThat("debug not enabled when it should be", logger.isDebugEnabled(marker));
        assertThat("debug enabled when it should not be", !logger.isDebugEnabled(marker));
        assertThat("debug not enabled when it should be", logger.isDebugEnabled(marker));
    }

    @Test
    public void infoEnabled() {
        when(imperativeLogger.isInfoEnabled()).thenReturn(true, false, true);
        assertThat("info not enabled when it should be", logger.isInfoEnabled());
        assertThat("info enabled when it should not be", !logger.isInfoEnabled());
        assertThat("info not enabled when it should be", logger.isInfoEnabled());
    }

    @Test
    public void infoEnabledMarker() {
        final Marker marker = MarkerFactory.getMarker(randomText());
        when(imperativeLogger.isInfoEnabled(eq(marker))).thenReturn(true, false, true);
        assertThat("info not enabled when it should be", logger.isInfoEnabled(marker));
        assertThat("info enabled when it should not be", !logger.isInfoEnabled(marker));
        assertThat("info not enabled when it should be", logger.isInfoEnabled(marker));
    }

    @Test
    public void warnEnabled() {
        when(imperativeLogger.isWarnEnabled()).thenReturn(true, false, true);
        assertThat("warn not enabled when it should be", logger.isWarnEnabled());
        assertThat("warn enabled when it should not be", !logger.isWarnEnabled());
        assertThat("warn not enabled when it should be", logger.isWarnEnabled());
    }

    @Test
    public void warnEnabledMarker() {
        final Marker marker = MarkerFactory.getMarker(randomText());
        when(imperativeLogger.isWarnEnabled(eq(marker))).thenReturn(true, false, true);
        assertThat("warn not enabled when it should be", logger.isWarnEnabled(marker));
        assertThat("warn enabled when it should not be", !logger.isWarnEnabled(marker));
        assertThat("warn not enabled when it should be", logger.isWarnEnabled(marker));
    }

    @Test
    public void errorEnabled() {
        when(imperativeLogger.isErrorEnabled()).thenReturn(true, false, true);
        assertThat("error not enabled when it should be", logger.isErrorEnabled());
        assertThat("error enabled when it should not be", !logger.isErrorEnabled());
        assertThat("error not enabled when it should be", logger.isErrorEnabled());
    }

    @Test
    public void errorEnabledMarker() {
        final Marker marker = MarkerFactory.getMarker(randomText());
        when(imperativeLogger.isErrorEnabled(eq(marker))).thenReturn(true, false, true);
        assertThat("error not enabled when it should be", logger.isErrorEnabled(marker));
        assertThat("error enabled when it should not be", !logger.isErrorEnabled(marker));
        assertThat("error not enabled when it should be", logger.isErrorEnabled(marker));
    }

    @Test
    public void traceMessage() {
        final String message = randomText();
        logger.trace(message).block();
        verify(imperativeLogger).trace(eq(message));
    }

    @Test
    public void traceFormatOneArgument() {
        final String format = randomText();
        final String argument = randomText();
        logger.trace(format, argument).block();
        verify(imperativeLogger).trace(eq(format), eq(argument));
    }

    @Test
    public void traceFormatTwoArguments() {
        final String format = randomText();
        final String argument1 = randomText();
        final String argument2 = randomText();
        logger.trace(format, argument1, argument2).block();
        verify(imperativeLogger).trace(eq(format), eq(argument1), eq(argument2));
    }

    @Test
    public void traceFormatArgumentArray() {
        final String format = randomText();
        final String argument1 = randomText();
        final String argument2 = randomText();
        final String argument3 = randomText();
        logger.trace(format, argument1, argument2, argument3).block();
        verify(imperativeLogger).trace(eq(format), eq(argument1), eq(argument2), eq(argument3));
    }

    @Test
    public void traceMessageThrowable() {
        final String message = randomText();
        final SimulatedException exception = new SimulatedException(randomText());
        logger.trace(message, exception).block();

        final ArgumentCaptor<SimulatedException> exceptionCaptor = ArgumentCaptor.forClass(SimulatedException.class);
        verify(imperativeLogger).trace(eq(message), exceptionCaptor.capture());
        assertThat(exceptionCaptor.getValue().getMessage(), equalTo(exception.getMessage()));
    }

    @Test
    public void traceMessageMarker() {
        final Marker marker = MarkerFactory.getMarker(randomText());
        final String message = randomText();
        logger.trace(marker, message).block();
        verify(imperativeLogger).trace(eq(marker), eq(message));
    }

    @Test
    public void traceFormatOneArgumentMarker() {
        final Marker marker = MarkerFactory.getMarker(randomText());
        final String format = randomText();
        final String argument = randomText();
        logger.trace(marker, format, argument).block();
        verify(imperativeLogger).trace(eq(marker), eq(format), eq(argument));
    }

    @Test
    public void traceFormatTwoArgumentsMarker() {
        final Marker marker = MarkerFactory.getMarker(randomText());
        final String format = randomText();
        final String argument1 = randomText();
        final String argument2 = randomText();
        logger.trace(marker, format, argument1, argument2).block();
        verify(imperativeLogger).trace(eq(marker), eq(format), eq(argument1), eq(argument2));
    }

    @Test
    public void traceFormatArgumentArrayMarker() {
        final Marker marker = MarkerFactory.getMarker(randomText());
        final String format = randomText();
        final String argument1 = randomText();
        final String argument2 = randomText();
        final String argument3 = randomText();
        logger.trace(marker, format, argument1, argument2, argument3).block();
        verify(imperativeLogger).trace(eq(marker), eq(format), eq(argument1), eq(argument2), eq(argument3));
    }

    @Test
    public void traceMessageThrowableMarker() {
        final Marker marker = MarkerFactory.getMarker(randomText());
        final String message = randomText();
        final SimulatedException exception = new SimulatedException(randomText());
        logger.trace(marker, message, exception).block();

        final ArgumentCaptor<SimulatedException> exceptionCaptor = ArgumentCaptor.forClass(SimulatedException.class);
        verify(imperativeLogger).trace(eq(marker), eq(message), exceptionCaptor.capture());
        assertThat(exceptionCaptor.getValue().getMessage(), equalTo(exception.getMessage()));
    }

    @Test
    public void debugMessage() {
        final String message = randomText();
        logger.debug(message).block();
        verify(imperativeLogger).debug(eq(message));
    }

    @Test
    public void debugFormatOneArgument() {
        final String format = randomText();
        final String argument = randomText();
        logger.debug(format, argument).block();
        verify(imperativeLogger).debug(eq(format), eq(argument));
    }

    @Test
    public void debugFormatTwoArguments() {
        final String format = randomText();
        final String argument1 = randomText();
        final String argument2 = randomText();
        logger.debug(format, argument1, argument2).block();
        verify(imperativeLogger).debug(eq(format), eq(argument1), eq(argument2));
    }

    @Test
    public void debugFormatArgumentArray() {
        final String format = randomText();
        final String argument1 = randomText();
        final String argument2 = randomText();
        final String argument3 = randomText();
        logger.debug(format, argument1, argument2, argument3).block();
        verify(imperativeLogger).debug(eq(format), eq(argument1), eq(argument2), eq(argument3));
    }

    @Test
    public void debugMessageThrowable() {
        final String message = randomText();
        final SimulatedException exception = new SimulatedException(randomText());
        logger.debug(message, exception).block();

        final ArgumentCaptor<SimulatedException> exceptionCaptor = ArgumentCaptor.forClass(SimulatedException.class);
        verify(imperativeLogger).debug(eq(message), exceptionCaptor.capture());
        assertThat(exceptionCaptor.getValue().getMessage(), equalTo(exception.getMessage()));
    }

    @Test
    public void debugMessageMarker() {
        final Marker marker = MarkerFactory.getMarker(randomText());
        final String message = randomText();
        logger.debug(marker, message).block();
        verify(imperativeLogger).debug(eq(marker), eq(message));
    }

    @Test
    public void debugFormatOneArgumentMarker() {
        final Marker marker = MarkerFactory.getMarker(randomText());
        final String format = randomText();
        final String argument = randomText();
        logger.debug(marker, format, argument).block();
        verify(imperativeLogger).debug(eq(marker), eq(format), eq(argument));
    }

    @Test
    public void debugFormatTwoArgumentsMarker() {
        final Marker marker = MarkerFactory.getMarker(randomText());
        final String format = randomText();
        final String argument1 = randomText();
        final String argument2 = randomText();
        logger.debug(marker, format, argument1, argument2).block();
        verify(imperativeLogger).debug(eq(marker), eq(format), eq(argument1), eq(argument2));
    }

    @Test
    public void debugFormatArgumentArrayMarker() {
        final Marker marker = MarkerFactory.getMarker(randomText());
        final String format = randomText();
        final String argument1 = randomText();
        final String argument2 = randomText();
        final String argument3 = randomText();
        logger.debug(marker, format, argument1, argument2, argument3).block();
        verify(imperativeLogger).debug(eq(marker), eq(format), eq(argument1), eq(argument2), eq(argument3));
    }

    @Test
    public void debugMessageThrowableMarker() {
        final Marker marker = MarkerFactory.getMarker(randomText());
        final String message = randomText();
        final SimulatedException exception = new SimulatedException(randomText());
        logger.debug(marker, message, exception).block();

        final ArgumentCaptor<SimulatedException> exceptionCaptor = ArgumentCaptor.forClass(SimulatedException.class);
        verify(imperativeLogger).debug(eq(marker), eq(message), exceptionCaptor.capture());
        assertThat(exceptionCaptor.getValue().getMessage(), equalTo(exception.getMessage()));
    }

    @Test
    public void infoMessage() {
        final String message = randomText();
        logger.info(message).block();
        verify(imperativeLogger).info(eq(message));
    }

    @Test
    public void infoFormatOneArgument() {
        final String format = randomText();
        final String argument = randomText();
        logger.info(format, argument).block();
        verify(imperativeLogger).info(eq(format), eq(argument));
    }

    @Test
    public void infoFormatTwoArguments() {
        final String format = randomText();
        final String argument1 = randomText();
        final String argument2 = randomText();
        logger.info(format, argument1, argument2).block();
        verify(imperativeLogger).info(eq(format), eq(argument1), eq(argument2));
    }

    @Test
    public void infoFormatArgumentArray() {
        final String format = randomText();
        final String argument1 = randomText();
        final String argument2 = randomText();
        final String argument3 = randomText();
        logger.info(format, argument1, argument2, argument3).block();
        verify(imperativeLogger).info(eq(format), eq(argument1), eq(argument2), eq(argument3));
    }

    @Test
    public void infoMessageThrowable() {
        final String message = randomText();
        final SimulatedException exception = new SimulatedException(randomText());
        logger.info(message, exception).block();

        final ArgumentCaptor<SimulatedException> exceptionCaptor = ArgumentCaptor.forClass(SimulatedException.class);
        verify(imperativeLogger).info(eq(message), exceptionCaptor.capture());
        assertThat(exceptionCaptor.getValue().getMessage(), equalTo(exception.getMessage()));
    }

    @Test
    public void infoMessageMarker() {
        final Marker marker = MarkerFactory.getMarker(randomText());
        final String message = randomText();
        logger.info(marker, message).block();
        verify(imperativeLogger).info(eq(marker), eq(message));
    }

    @Test
    public void infoFormatOneArgumentMarker() {
        final Marker marker = MarkerFactory.getMarker(randomText());
        final String format = randomText();
        final String argument = randomText();
        logger.info(marker, format, argument).block();
        verify(imperativeLogger).info(eq(marker), eq(format), eq(argument));
    }

    @Test
    public void infoFormatTwoArgumentsMarker() {
        final Marker marker = MarkerFactory.getMarker(randomText());
        final String format = randomText();
        final String argument1 = randomText();
        final String argument2 = randomText();
        logger.info(marker, format, argument1, argument2).block();
        verify(imperativeLogger).info(eq(marker), eq(format), eq(argument1), eq(argument2));
    }

    @Test
    public void infoFormatArgumentArrayMarker() {
        final Marker marker = MarkerFactory.getMarker(randomText());
        final String format = randomText();
        final String argument1 = randomText();
        final String argument2 = randomText();
        final String argument3 = randomText();
        logger.info(marker, format, argument1, argument2, argument3).block();
        verify(imperativeLogger).info(eq(marker), eq(format), eq(argument1), eq(argument2), eq(argument3));
    }

    @Test
    public void infoMessageThrowableMarker() {
        final Marker marker = MarkerFactory.getMarker(randomText());
        final String message = randomText();
        final SimulatedException exception = new SimulatedException(randomText());
        logger.info(marker, message, exception).block();

        final ArgumentCaptor<SimulatedException> exceptionCaptor = ArgumentCaptor.forClass(SimulatedException.class);
        verify(imperativeLogger).info(eq(marker), eq(message), exceptionCaptor.capture());
        assertThat(exceptionCaptor.getValue().getMessage(), equalTo(exception.getMessage()));
    }

    @Test
    public void warnMessage() {
        final String message = randomText();
        logger.warn(message).block();
        verify(imperativeLogger).warn(eq(message));
    }

    @Test
    public void warnFormatOneArgument() {
        final String format = randomText();
        final String argument = randomText();
        logger.warn(format, argument).block();
        verify(imperativeLogger).warn(eq(format), eq(argument));
    }

    @Test
    public void warnFormatTwoArguments() {
        final String format = randomText();
        final String argument1 = randomText();
        final String argument2 = randomText();
        logger.warn(format, argument1, argument2).block();
        verify(imperativeLogger).warn(eq(format), eq(argument1), eq(argument2));
    }

    @Test
    public void warnFormatArgumentArray() {
        final String format = randomText();
        final String argument1 = randomText();
        final String argument2 = randomText();
        final String argument3 = randomText();
        logger.warn(format, argument1, argument2, argument3).block();
        verify(imperativeLogger).warn(eq(format), eq(argument1), eq(argument2), eq(argument3));
    }

    @Test
    public void warnMessageThrowable() {
        final String message = randomText();
        final SimulatedException exception = new SimulatedException(randomText());
        logger.warn(message, exception).block();

        final ArgumentCaptor<SimulatedException> exceptionCaptor = ArgumentCaptor.forClass(SimulatedException.class);
        verify(imperativeLogger).warn(eq(message), exceptionCaptor.capture());
        assertThat(exceptionCaptor.getValue().getMessage(), equalTo(exception.getMessage()));
    }

    @Test
    public void warnMessageMarker() {
        final Marker marker = MarkerFactory.getMarker(randomText());
        final String message = randomText();
        logger.warn(marker, message).block();
        verify(imperativeLogger).warn(eq(marker), eq(message));
    }

    @Test
    public void warnFormatOneArgumentMarker() {
        final Marker marker = MarkerFactory.getMarker(randomText());
        final String format = randomText();
        final String argument = randomText();
        logger.warn(marker, format, argument).block();
        verify(imperativeLogger).warn(eq(marker), eq(format), eq(argument));
    }

    @Test
    public void warnFormatTwoArgumentsMarker() {
        final Marker marker = MarkerFactory.getMarker(randomText());
        final String format = randomText();
        final String argument1 = randomText();
        final String argument2 = randomText();
        logger.warn(marker, format, argument1, argument2).block();
        verify(imperativeLogger).warn(eq(marker), eq(format), eq(argument1), eq(argument2));
    }

    @Test
    public void warnFormatArgumentArrayMarker() {
        final Marker marker = MarkerFactory.getMarker(randomText());
        final String format = randomText();
        final String argument1 = randomText();
        final String argument2 = randomText();
        final String argument3 = randomText();
        logger.warn(marker, format, argument1, argument2, argument3).block();
        verify(imperativeLogger).warn(eq(marker), eq(format), eq(argument1), eq(argument2), eq(argument3));
    }

    @Test
    public void warnMessageThrowableMarker() {
        final Marker marker = MarkerFactory.getMarker(randomText());
        final String message = randomText();
        final SimulatedException exception = new SimulatedException(randomText());
        logger.warn(marker, message, exception).block();

        final ArgumentCaptor<SimulatedException> exceptionCaptor = ArgumentCaptor.forClass(SimulatedException.class);
        verify(imperativeLogger).warn(eq(marker), eq(message), exceptionCaptor.capture());
        assertThat(exceptionCaptor.getValue().getMessage(), equalTo(exception.getMessage()));
    }

    @Test
    public void errorMessage() {
        final String message = randomText();
        logger.error(message).block();
        verify(imperativeLogger).error(eq(message));
    }

    @Test
    public void errorFormatOneArgument() {
        final String format = randomText();
        final String argument = randomText();
        logger.error(format, argument).block();
        verify(imperativeLogger).error(eq(format), eq(argument));
    }

    @Test
    public void errorFormatTwoArguments() {
        final String format = randomText();
        final String argument1 = randomText();
        final String argument2 = randomText();
        logger.error(format, argument1, argument2).block();
        verify(imperativeLogger).error(eq(format), eq(argument1), eq(argument2));
    }

    @Test
    public void errorFormatArgumentArray() {
        final String format = randomText();
        final String argument1 = randomText();
        final String argument2 = randomText();
        final String argument3 = randomText();
        logger.error(format, argument1, argument2, argument3).block();
        verify(imperativeLogger).error(eq(format), eq(argument1), eq(argument2), eq(argument3));
    }

    @Test
    public void errorMessageThrowable() {
        final String message = randomText();
        final SimulatedException exception = new SimulatedException(randomText());
        logger.error(message, exception).block();

        final ArgumentCaptor<SimulatedException> exceptionCaptor = ArgumentCaptor.forClass(SimulatedException.class);
        verify(imperativeLogger).error(eq(message), exceptionCaptor.capture());
        assertThat(exceptionCaptor.getValue().getMessage(), equalTo(exception.getMessage()));
    }

    @Test
    public void errorMessageMarker() {
        final Marker marker = MarkerFactory.getMarker(randomText());
        final String message = randomText();
        logger.error(marker, message).block();
        verify(imperativeLogger).error(eq(marker), eq(message));
    }

    @Test
    public void errorFormatOneArgumentMarker() {
        final Marker marker = MarkerFactory.getMarker(randomText());
        final String format = randomText();
        final String argument = randomText();
        logger.error(marker, format, argument).block();
        verify(imperativeLogger).error(eq(marker), eq(format), eq(argument));
    }

    @Test
    public void errorFormatTwoArgumentsMarker() {
        final Marker marker = MarkerFactory.getMarker(randomText());
        final String format = randomText();
        final String argument1 = randomText();
        final String argument2 = randomText();
        logger.error(marker, format, argument1, argument2).block();
        verify(imperativeLogger).error(eq(marker), eq(format), eq(argument1), eq(argument2));
    }

    @Test
    public void errorFormatArgumentArrayMarker() {
        final Marker marker = MarkerFactory.getMarker(randomText());
        final String format = randomText();
        final String argument1 = randomText();
        final String argument2 = randomText();
        final String argument3 = randomText();
        logger.error(marker, format, argument1, argument2, argument3).block();
        verify(imperativeLogger).error(eq(marker), eq(format), eq(argument1), eq(argument2), eq(argument3));
    }

    @Test
    public void errorMessageThrowableMarker() {
        final Marker marker = MarkerFactory.getMarker(randomText());
        final String message = randomText();
        final SimulatedException exception = new SimulatedException(randomText());
        logger.error(marker, message, exception).block();

        final ArgumentCaptor<SimulatedException> exceptionCaptor = ArgumentCaptor.forClass(SimulatedException.class);
        verify(imperativeLogger).error(eq(marker), eq(message), exceptionCaptor.capture());
        assertThat(exceptionCaptor.getValue().getMessage(), equalTo(exception.getMessage()));
    }

    public static class SimulatedException extends RuntimeException {
        public SimulatedException(final String message) {
            super(message);
        }
    }
}
