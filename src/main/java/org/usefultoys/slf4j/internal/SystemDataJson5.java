/*
 * Copyright 2026 Daniel Felix Ferber
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.usefultoys.slf4j.internal;

import lombok.experimental.UtilityClass;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.usefultoys.slf4j.internal.EventDataJson5.*;

/**
 * A package-private utility class responsible for serializing and deserializing
 * {@link SystemData} objects to and from a JSON5-like string format.
 *
 * @author Daniel Felix Ferber
 */
@UtilityClass
class SystemDataJson5 {

    private final String PROP_MEMORY = "m";
    private final String PROP_HEAP = "h";
    private final String PROP_NON_HEAP = "nh";
    private final String PROP_FINALIZATION_COUNT = "fc";
    private final String PROP_CLASS_LOADING = "cl";
    private final String PROP_COMPILATION_TIME = "ct";
    private final String PROP_GARBAGE_COLLECTOR = "gc";
    private final String PROP_SYSTEM_LOAD = "sl";

    private final Pattern patternMemory = Pattern.compile(REGEX_START + PROP_MEMORY + REGEX_3_TUPLE);
    private final Pattern patternHeap = Pattern.compile(REGEX_START + PROP_HEAP + REGEX_3_TUPLE);
    private final Pattern patternNonHeap = Pattern.compile(REGEX_START + PROP_NON_HEAP + REGEX_3_TUPLE);
    private final Pattern patternFinalizationCount = Pattern.compile(REGEX_START + PROP_FINALIZATION_COUNT + REGEX_WORD_VALUE);
    private final Pattern patternClassLoading = Pattern.compile(REGEX_START + PROP_CLASS_LOADING + REGEX_3_TUPLE);
    private final Pattern patternCompilationTime = Pattern.compile(REGEX_START + PROP_COMPILATION_TIME + REGEX_WORD_VALUE);
    private final Pattern patternGarbageCollector = Pattern.compile(REGEX_START + PROP_GARBAGE_COLLECTOR + REGEX_2_TUPLE);
    private final Pattern patternSystemLoad = Pattern.compile(REGEX_START + PROP_SYSTEM_LOAD + REGEX_WORD_VALUE);

    @SuppressWarnings("MagicCharacter")
    void write(final SystemData data, final StringBuilder sb) {
        if (data.runtime_usedMemory > 0 || data.runtime_totalMemory > 0 || data.runtime_maxMemory > 0) {
            sb.append(',').append(PROP_MEMORY).append(":[").append(data.runtime_usedMemory).append(',').append(data.runtime_totalMemory).append(',').append(data.runtime_maxMemory).append(']');
        }
        if (data.heap_commited > 0 || data.heap_max > 0 || data.heap_used > 0) {
            sb.append(',').append(PROP_HEAP).append(":[").append(data.heap_used).append(',').append(data.heap_commited).append(',').append(data.heap_max).append(']');
        }
        if (data.nonHeap_commited > 0 || data.nonHeap_max > 0 || data.nonHeap_used > 0) {
            sb.append(',').append(PROP_NON_HEAP).append(":[").append(data.nonHeap_used).append(',').append(data.nonHeap_commited).append(',').append(data.nonHeap_max).append(']');
        }
        if (data.objectPendingFinalizationCount > 0) {
            sb.append(',').append(PROP_FINALIZATION_COUNT).append(':').append(data.objectPendingFinalizationCount);
        }
        if (data.classLoading_loaded > 0 || data.classLoading_total > 0 || data.classLoading_unloaded > 0) {
            sb.append(',').append(PROP_CLASS_LOADING).append(":[").append(data.classLoading_total).append(',').append(data.classLoading_loaded).append(',').append(data.classLoading_unloaded).append(']');
        }
        if (data.compilationTime > 0) {
            sb.append(',').append(PROP_COMPILATION_TIME).append(':').append(data.compilationTime);
        }
        if (data.garbageCollector_count > 0 || data.garbageCollector_time > 0) {
            sb.append(',').append(PROP_GARBAGE_COLLECTOR).append(":[").append(data.garbageCollector_count).append(',').append(data.garbageCollector_time).append(']');
        }
        if (data.systemLoad > 0) {
            final long scaled = Math.round(data.systemLoad * 10);
            sb.append(',').append(PROP_SYSTEM_LOAD).append(':').append(scaled / 10).append('.').append(scaled % 10);
        }
    }

    void read(final SystemData data, final String json5) {
        final Matcher matcherMemory = patternMemory.matcher(json5);
        if (matcherMemory.find()) {
            data.runtime_usedMemory = Long.parseLong(matcherMemory.group(1));
            data.runtime_totalMemory = Long.parseLong(matcherMemory.group(2));
            data.runtime_maxMemory = Long.parseLong(matcherMemory.group(3));
        }
        final Matcher matcherHeap = patternHeap.matcher(json5);
        if (matcherHeap.find()) {
            data.heap_used = Long.parseLong(matcherHeap.group(1));
            data.heap_commited = Long.parseLong(matcherHeap.group(2));
            data.heap_max = Long.parseLong(matcherHeap.group(3));
        }
        final Matcher matcherNonHeap = patternNonHeap.matcher(json5);
        if (matcherNonHeap.find()) {
            data.nonHeap_used = Long.parseLong(matcherNonHeap.group(1));
            data.nonHeap_commited = Long.parseLong(matcherNonHeap.group(2));
            data.nonHeap_max = Long.parseLong(matcherNonHeap.group(3));
        }
        final Matcher matcherFinalizationCount = patternFinalizationCount.matcher(json5);
        if (matcherFinalizationCount.find()) {
            data.objectPendingFinalizationCount = Long.parseLong(matcherFinalizationCount.group(1));
        }
        final Matcher matcherClassLoading = patternClassLoading.matcher(json5);
        if (matcherClassLoading.find()) {
            data.classLoading_total = Long.parseLong(matcherClassLoading.group(1));
            data.classLoading_loaded = Long.parseLong(matcherClassLoading.group(2));
            data.classLoading_unloaded = Long.parseLong(matcherClassLoading.group(3));
        }
        final Matcher matcherCompilationTime = patternCompilationTime.matcher(json5);
        if (matcherCompilationTime.find()) {
            data.compilationTime = Long.parseLong(matcherCompilationTime.group(1));
        }
        final Matcher matcherGarbageCollector = patternGarbageCollector.matcher(json5);
        if (matcherGarbageCollector.find()) {
            data.garbageCollector_count = Long.parseLong(matcherGarbageCollector.group(1));
            data.garbageCollector_time = Long.parseLong(matcherGarbageCollector.group(2));
        }
        final Matcher matcherSystemLoad = patternSystemLoad.matcher(json5);
        if (matcherSystemLoad.find()) {
            data.systemLoad = Double.parseDouble(matcherSystemLoad.group(1));
        }
    }
}
