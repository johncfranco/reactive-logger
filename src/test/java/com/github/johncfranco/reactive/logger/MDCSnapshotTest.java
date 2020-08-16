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
import org.slf4j.MDC;

import java.util.Map;

import static com.github.johncfranco.reactive.logger.TestHelpers.randomText;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anEmptyMap;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

public class MDCSnapshotTest {
    @Test
    public void populateAndClear() {
        final Map<String, String> expected = ImmutableMap.of(randomText(), randomText(), randomText(), randomText());
        try (final MDCSnapshot snapshot = MDCSnapshot.of(expected)) {
            final Map<String, String> actual = MDC.getCopyOfContextMap();
            assertThat(actual, equalTo(expected));
        }

        assertThat(MDC.getCopyOfContextMap(), anyOf(anEmptyMap(), nullValue()));
    }

    @Test
    public void createEmptyInstance() {
        MDC.put(randomText(), randomText());
        try (final MDCSnapshot snapshot = MDCSnapshot.empty()) {
            assertThat(MDC.getCopyOfContextMap(), anyOf(anEmptyMap(), nullValue()));
        }
    }
}
