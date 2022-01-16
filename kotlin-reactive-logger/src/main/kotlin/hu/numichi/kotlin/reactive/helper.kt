package hu.numichi.kotlin.reactive

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.reactor.ReactorContext
import kotlinx.coroutines.reactor.asCoroutineContext
import reactor.util.context.Context
import java.util.stream.Collectors
import kotlin.coroutines.coroutineContext

@Suppress("UNCHECKED_CAST")
suspend fun contextToMap(mdcContextKey: String): MutableMap<String, String> {
    val result = coroutineContext[ReactorContext]?.context?.readOnly()?.stream()?.collect(
        Collectors.toMap(
            { (key): Map.Entry<Any, Any> -> key },
            { (_, value): Map.Entry<Any, Any> -> value }
        )
    )?.toMutableMap()?.get(mdcContextKey)

    return if (result is MutableMap<*, *>) {
        result as MutableMap<String, String>
    } else {
        mutableMapOf()
    }
}

suspend fun <T> withContext(mdc: MDC, block: suspend CoroutineScope.() -> T): T {
    return withContext(listOf(mdc), block)
}

suspend fun <T> withContext(mdcCollection: Collection<MDC>, block: suspend CoroutineScope.() -> T): T {
    var context = coroutineContext[ReactorContext]?.context ?: Context.empty()

    mdcCollection.forEach {
        context = context.delete(it.mdcContextKey)
        context = context.put(it.mdcContextKey, it.asMap())
    }

    return kotlinx.coroutines.withContext(context.asCoroutineContext()) {
        block()
    }
}