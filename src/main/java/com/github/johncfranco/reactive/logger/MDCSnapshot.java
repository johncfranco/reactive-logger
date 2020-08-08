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

import org.slf4j.MDC;
import reactor.util.annotation.Nullable;
import reactor.util.context.Context;

import java.util.Map;

/**
 * A closeable object encapsulating the operations of populating the {@link MDC} upon creation
 * and then clearing it upon closing.
 *
 * <p>
 * Similar to {@link org.slf4j.MDC.MDCCloseable}, but for the entire contents of the MDC
 * rather than a single key-value pair.
 * </p>
 *
 * <p>
 * Intended for use in <code>try-with-resources</code> blocks
 * when the desired contents of the {@link MDC} within the block
 * have come from somewhere else, such as from an entry in a subscriber {@link reactor.util.context.Context}.
 * </p>
 *
 * <p>
 * This class is specific to reactive code in that it assumes anything already in the {@link MDC}
 * is there by mistake, and so erases it.
 * Because different parts of a reactive chain can run on different threads, relying on the MDC's
 * thread-local storage for propagation through the chain would be a mistake.
 * </p>
 *
 * @see ReactiveLogger#readMDC(Context)
 *
 * @author John C. Franco
 * @since 1.0
 */
public class MDCSnapshot implements AutoCloseable {
    private MDCSnapshot(@Nullable final Map<String, String> context) {
        if (context == null) {
            MDC.clear();
        } else {
            MDC.setContextMap(context);
        }
    }

    /**
     * Copy the given {@link Map} to the {@link MDC}, erasing anything already in the MDC,
     * and return an {@link AutoCloseable} object that clears the MDC when closed.
     *
     * @param context The map to copy to the MDC.
     * @return A new {@link MDCSnapshot} instance.
     * @since 1.0
     */
    public static MDCSnapshot of(@Nullable final Map<String, String> context) {
        return new MDCSnapshot(context);
    }

    /**
     * Erase the entire contents ot the {@link MDC}.
     * @since 1.0
     */
    @Override
    public void close() {
        MDC.clear();
    }
}
