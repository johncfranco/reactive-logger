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
import org.junit.jupiter.api.Test;
import reactor.util.context.Context;

import java.util.Map;
import java.util.Optional;

import static com.github.johncfranco.reactive.logger.TestHelpers.randomText;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ReactiveLoggerBuilderTest {
    @Test
    public void rejectNullContextKey() {
        assertThrows(NullPointerException.class, () -> ReactiveLogger.builder().withMDCContextKey(null));
    }

    @Test
    public void rejectBlankContextKey() {
        assertThrows(IllegalArgumentException.class, () -> ReactiveLogger.builder().withMDCContextKey("  "));
    }

    @Test
    public void customContextKey() {
        final String contextKey = randomText();
        final Map<String, String> mdc = ImmutableMap.of(randomText(), randomText());
        final ReactiveLogger logger = ReactiveLogger.builder().withMDCContextKey(contextKey).build();
        final Context context = Context.of(contextKey, mdc);
        assertThat(logger.readMDC(context), equalTo(Optional.of(mdc)));
    }
}
