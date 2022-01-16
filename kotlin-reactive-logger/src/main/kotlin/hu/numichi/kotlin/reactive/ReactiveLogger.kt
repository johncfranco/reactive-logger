package hu.numichi.kotlin.reactive

import hu.numichi.reactive.logger.ReactiveLogger as ReactiveJavaLogger
import kotlinx.coroutines.reactor.ReactorContext
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.reactor.mono
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.Marker
import reactor.core.publisher.Mono
import reactor.core.scheduler.Scheduler
import reactor.util.context.Context
import java.util.*
import kotlin.coroutines.coroutineContext

class ReactiveLogger private constructor(private val reactiveLogger: ReactiveJavaLogger) {
    companion object {
        const val DEFAULT_REACTOR_CONTEXT_MDC_KEY = ReactiveJavaLogger.DEFAULT_REACTOR_CONTEXT_MDC_KEY

        @JvmStatic
        private val DEFAULT_SCHEDULER: Scheduler = ReactiveJavaLogger.DEFAULT_SCHEDULER

        @JvmStatic
        private val DEFAULT_LOGGER: Logger = LoggerFactory.getLogger(ReactiveLogger::class.java)

        @JvmStatic
        fun builder() = Builder()
    }

    fun imperative(): Logger {
        return reactiveLogger.imperative()
    }

    fun scheduler(): Scheduler {
        return reactiveLogger.scheduler()
    }

    fun readMDC(context: Context): Optional<Map<String, String>> {
        return reactiveLogger.readMDC(context)
    }

    //region Trace
    fun isTraceEnabled(): Boolean {
        return reactiveLogger.isTraceEnabled
    }

    suspend fun trace(msg: String) {
        return wrap { logger -> logger.trace(msg) }
    }

    suspend fun trace(format: String, arg: Any) {
        return wrap { logger -> logger.trace(format, arg) }
    }

    suspend fun trace(format: String, arg1: Any, arg2: Any) {
        return wrap { logger -> logger.trace(format, arg1, arg2) }
    }

    suspend fun trace(format: String, vararg arguments: Any) {
        return wrap { logger -> logger.trace(format, *arguments) }
    }

    suspend fun trace(msg: String, t: Throwable) {
        return wrap { logger -> logger.trace(msg, t) }
    }

    fun isTraceEnabled(marker: Marker): Boolean {
        return reactiveLogger.isTraceEnabled(marker)
    }

    suspend fun trace(marker: Marker, msg: String) {
        return wrap { logger -> logger.trace(marker, msg) }
    }

    suspend fun trace(marker: Marker, format: String, arg: Any) {
        return wrap { logger -> logger.trace(marker, format, arg) }
    }

    suspend fun trace(marker: Marker, format: String, arg1: Any, arg2: Any) {
        return wrap { logger -> logger.trace(marker, format, arg1, arg2) }
    }

    suspend fun trace(marker: Marker, format: String, vararg argArray: Any) {
        return wrap { logger -> logger.trace(marker, format, *argArray) }
    }

    suspend fun trace(marker: Marker, msg: String, t: Throwable) {
        return wrap { logger -> logger.trace(marker, msg, t) }
    }
    //endregion

    //region Debug
    fun isDebugEnabled(): Boolean {
        return reactiveLogger.isDebugEnabled
    }

    suspend fun debug(msg: String) {
        return wrap { logger -> logger.debug(msg) }
    }

    suspend fun debug(format: String, arg: Any) {
        return wrap { logger -> logger.debug(format, arg) }
    }

    suspend fun debug(format: String, arg1: Any, arg2: Any) {
        return wrap { logger -> logger.debug(format, arg1, arg2) }
    }

    suspend fun debug(format: String, vararg arguments: Any) {
        return wrap { logger -> logger.debug(format, *arguments) }
    }

    suspend fun debug(msg: String, t: Throwable) {
        return wrap { logger -> logger.debug(msg, t) }
    }

    fun isDebugEnabled(marker: Marker): Boolean {
        return reactiveLogger.isDebugEnabled(marker)
    }

    suspend fun debug(marker: Marker, msg: String) {
        return wrap { logger -> logger.debug(marker, msg) }
    }

    suspend fun debug(marker: Marker, format: String, arg: Any) {
        return wrap { logger -> logger.debug(marker, format, arg) }
    }

    suspend fun debug(marker: Marker, format: String, arg1: Any, arg2: Any) {
        return wrap { logger -> logger.debug(marker, format, arg1, arg2) }
    }

    suspend fun debug(marker: Marker, format: String, vararg arguments: Any) {
        return wrap { logger -> logger.debug(marker, format, *arguments) }
    }

    suspend fun debug(marker: Marker, msg: String, t: Throwable) {
        return wrap { logger -> logger.debug(marker, msg, t) }
    }
    //endregion

    //region Info
    fun isInfoEnabled(): Boolean {
        return reactiveLogger.isInfoEnabled
    }

    suspend fun info(msg: String) {
        return wrap { logger -> logger.info(msg) }
    }

    suspend fun info(format: String, arg: Any) {
        return wrap { logger -> logger.info(format, arg) }
    }

    suspend fun info(format: String, arg1: Any, arg2: Any) {
        return wrap { logger -> logger.info(format, arg1, arg2) }
    }

    suspend fun info(format: String, vararg arguments: Any) {
        return wrap { logger -> logger.info(format, *arguments) }
    }

    suspend fun info(msg: String, t: Throwable) {
        return wrap { logger -> logger.info(msg, t) }
    }

    fun isInfoEnabled(marker: Marker): Boolean {
        return reactiveLogger.isInfoEnabled(marker)
    }

    suspend fun info(marker: Marker, msg: String) {
        return wrap { logger -> logger.info(marker, msg) }
    }

    suspend fun info(marker: Marker, format: String, arg: Any) {
        return wrap { logger -> logger.info(marker, format, arg) }
    }

    suspend fun info(marker: Marker, format: String, arg1: Any, arg2: Any) {
        return wrap { logger -> logger.info(marker, format, arg1, arg2) }
    }

    suspend fun info(marker: Marker, format: String, vararg arguments: Any) {
        return wrap { logger -> logger.info(marker, format, *arguments) }
    }

    suspend fun info(marker: Marker, msg: String, t: Throwable) {
        return wrap { logger -> logger.info(marker, msg, t) }
    }
    //endregion

    //region Warn
    fun isWarnEnabled(): Boolean {
        return reactiveLogger.isWarnEnabled
    }

    suspend fun warn(msg: String) {
        return wrap { logger -> logger.warn(msg) }
    }

    suspend fun warn(format: String, arg: Any) {
        return wrap { logger -> logger.warn(format, arg) }
    }

    suspend fun warn(format: String, vararg arguments: Any) {
        return wrap { logger -> logger.warn(format, *arguments) }
    }

    suspend fun warn(format: String, arg1: Any, arg2: Any) {
        return wrap { logger -> logger.warn(format, arg1, arg2) }
    }

    suspend fun warn(msg: String, t: Throwable) {
        return wrap { logger -> logger.warn(msg, t) }
    }

    fun isWarnEnabled(marker: Marker): Boolean {
        return reactiveLogger.isWarnEnabled(marker)
    }

    suspend fun warn(marker: Marker, msg: String) {
        return wrap { logger -> logger.warn(marker, msg) }
    }

    suspend fun warn(marker: Marker, format: String, arg: Any) {
        return wrap { logger -> logger.warn(marker, format, arg) }
    }

    suspend fun warn(marker: Marker, format: String, arg1: Any, arg2: Any) {
        return wrap { logger -> logger.warn(marker, format, arg1, arg2) }
    }

    suspend fun warn(marker: Marker, format: String, vararg arguments: Any) {
        return wrap { logger -> logger.warn(marker, format, *arguments) }
    }

    suspend fun warn(marker: Marker, msg: String, t: Throwable) {
        return wrap { logger -> logger.warn(marker, msg, t) }
    }
    //endregion

    //region Error
    fun isErrorEnabled(): Boolean {
        return reactiveLogger.isErrorEnabled
    }

    suspend fun error(msg: String) {
        return wrap { logger -> logger.error(msg) }
    }

    suspend fun error(format: String, arg: Any) {
        return wrap { logger -> logger.error(format, arg) }
    }

    suspend fun error(format: String, arg1: Any, arg2: Any) {
        return wrap { logger -> logger.error(format, arg1, arg2) }
    }

    suspend fun error(format: String, vararg arguments: Any) {
        return wrap { logger -> logger.error(format, *arguments) }
    }

    suspend fun error(msg: String, t: Throwable) {
        return wrap { logger -> logger.error(msg, t) }
    }

    fun isErrorEnabled(marker: Marker): Boolean {
        return reactiveLogger.isErrorEnabled(marker)
    }

    suspend fun error(marker: Marker, msg: String) {
        return wrap { logger -> logger.error(marker, msg) }
    }

    suspend fun error(marker: Marker, format: String, arg: Any) {
        return wrap { logger -> logger.error(marker, format, arg) }
    }

    suspend fun error(marker: Marker, format: String, arg1: Any, arg2: Any) {
        return wrap { logger -> logger.error(marker, format, arg1, arg2) }
    }

    suspend fun error(marker: Marker, format: String, vararg arguments: Any) {
        return wrap { logger -> logger.error(marker, format, *arguments) }
    }

    suspend fun error(marker: Marker, msg: String, t: Throwable) {
        return wrap { logger -> logger.error(marker, msg, t) }
    }
    //endregion

    private suspend fun wrap(fn: (ReactiveJavaLogger) -> Mono<Context>) {
        val context = coroutineContext[ReactorContext]?.context ?: Context.empty()

        mono { fn(reactiveLogger) }
            .contextWrite { it.putAll(context.readOnly()) }
            .awaitSingleOrNull()
    }

    class Builder {
        private var scheduler = DEFAULT_SCHEDULER
        private var logger = DEFAULT_LOGGER
        private var mdcContextKey = DEFAULT_REACTOR_CONTEXT_MDC_KEY

        fun withLogger(logger: Logger): Builder {
            this.logger = logger
            return this
        }

        fun withScheduler(scheduler: Scheduler): Builder {
            this.scheduler = scheduler
            return this
        }

        fun withMDCContextKey(mdcContextKey: String): Builder {
            require(mdcContextKey.trim { it <= ' ' }.isNotEmpty()) { "MDC context key must not be blank" }

            this.mdcContextKey = mdcContextKey
            return this
        }

        fun build(): ReactiveLogger {
            return ReactiveLogger(
                ReactiveJavaLogger.builder()
                    .withLogger(logger)
                    .withScheduler(scheduler)
                    .withMDCContextKey(mdcContextKey)
                    .build()
            )
        }
    }
}

