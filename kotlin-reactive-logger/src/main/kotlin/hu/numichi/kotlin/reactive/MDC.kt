package hu.numichi.kotlin.reactive

import reactor.util.context.ContextView

class MDC private constructor(val mdcContextKey: String, private val mutableMap: MutableMap<String, String>) {
    companion object {
        @JvmStatic
        fun restore(contextView: ContextView): MDC {
            val def = contextView.getOrDefault<MutableMap<String, String>>(ReactiveLogger.DEFAULT_REACTOR_CONTEXT_MDC_KEY, null) ?: emptyMap()
            return MDC(ReactiveLogger.DEFAULT_REACTOR_CONTEXT_MDC_KEY, def)
        }

        @JvmStatic
        fun restore(mdcContextKey: String, contextView: ContextView): MDC {
            val def = contextView.getOrDefault<MutableMap<String, String>>(mdcContextKey, null) ?: emptyMap()
            return MDC(mdcContextKey, def)
        }

        @JvmStatic
        suspend fun restore(): MDC {
            return MDC(ReactiveLogger.DEFAULT_REACTOR_CONTEXT_MDC_KEY, contextToMap(ReactiveLogger.DEFAULT_REACTOR_CONTEXT_MDC_KEY))
        }

        @JvmStatic
        suspend fun restore(mdcContextKey: String): MDC {
            return MDC(mdcContextKey, contextToMap(mdcContextKey))
        }

        @JvmStatic
        fun of(): MDC {
            return MDC(ReactiveLogger.DEFAULT_REACTOR_CONTEXT_MDC_KEY, emptyMap())
        }

        @JvmStatic
        fun of(contextMap: MutableMap<String, String>): MDC {
            return MDC(ReactiveLogger.DEFAULT_REACTOR_CONTEXT_MDC_KEY, contextMap)
        }

        @JvmStatic
        fun of(mdcContextKey: String, contextMap: MutableMap<String, String>): MDC {
            return MDC(mdcContextKey, contextMap)
        }

        @JvmStatic
        private fun emptyMap(): MutableMap<String, String> = mutableMapOf()
    }

    fun asMap(): MutableMap<String, String> = mutableMap
    
    fun put(key: String, value: String) {
        mutableMap[key] = value
    }
    
    fun get(key: String): String? {
        return mutableMap[key]
    }
    
    fun delete(key: String): String? {
        return mutableMap.remove(key)
    }
}