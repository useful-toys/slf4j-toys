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
package org.usefultoys.slf4j.meter;

import lombok.experimental.UtilityClass;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A package-private utility class responsible for serializing and deserializing
 * {@link MeterData} objects to and from a JSON5-like string format.
 *
 * @author Daniel Felix Ferber
 * @author Co-authored-by: GitHub Copilot using Claude Sonnet 4.5
 */
@UtilityClass
class MeterDataJson5 {

    /** JSON property key for operation description. */
    private static final String PROP_DESCRIPTION = "d";
    /** JSON property key for success path identifier. */
    private static final String PROP_PATH_ID = "p";
    /** JSON property key for rejection path identifier. */
    private static final String PROP_REJECT_ID = "r";
    /** JSON property key for failure path identifier. */
    private static final String PROP_FAIL_ID = "f";
    /** JSON property key for failure message. */
    private static final String PROP_FAIL_MESSAGE = "fm";
    /** JSON property key for meter creation timestamp. */
    private static final String PROP_CREATE_TIME = "t0";
    /** JSON property key for operation start timestamp. */
    private static final String PROP_START_TIME = "t1";
    /** JSON property key for operation stop timestamp. */
    private static final String PROP_STOP_TIME = "t2";
    /** JSON property key for current iteration count. */
    private static final String PROP_ITERATION = "i";
    /** JSON property key for expected iteration count. */
    private static final String PROP_EXPECTED_ITERATION = "ei";
    /** JSON property key for time limit threshold. */
    private static final String PROP_LIMIT_TIME = "tl";
    /** JSON property key for context map. */
    private static final String PROP_CONTEXT = "ctx";
    /** JSON property key for event category. */
    private static final String EVENT_CATEGORY = "c";
    /** JSON property key for event name. */
    private static final String EVENT_NAME = "n";
    /** JSON property key for parent event identifier. */
    private static final String EVENT_PARENT = "ep";

    /** Regex pattern matching JSON property start (after '{' or ','). */
    private static final String REGEX_START = "[{,]";
    /** Regex pattern for extracting quoted string values. */
    private static final String REGEX_STRING_VALUE = "\\s*:\\s*'([^']*)'";
    /** Regex pattern for extracting unquoted word values. */
    private static final String REGEX_WORD_VALUE = "\\s*:\\s*([^,}\\s]+)";

    /** Pattern for parsing description property from JSON. */
    private static final Pattern PATTERN_DESCRIPTION = Pattern.compile(REGEX_START + PROP_DESCRIPTION + REGEX_STRING_VALUE);
    /** Pattern for parsing success path identifier from JSON. */
    private static final Pattern PATTERN_PATH_ID = Pattern.compile(REGEX_START + PROP_PATH_ID + REGEX_WORD_VALUE);
    /** Pattern for parsing rejection path identifier from JSON. */
    private static final Pattern PATTERN_REJECT_ID = Pattern.compile(REGEX_START + PROP_REJECT_ID + REGEX_WORD_VALUE);
    /** Pattern for parsing failure path identifier from JSON. */
    private static final Pattern PATTERN_FAIL_ID = Pattern.compile(REGEX_START + PROP_FAIL_ID + REGEX_WORD_VALUE);
    /** Pattern for parsing failure message from JSON. */
    private static final Pattern PATTERN_FAIL_MESSAGE = Pattern.compile(REGEX_START + PROP_FAIL_MESSAGE + REGEX_STRING_VALUE);
    /** Pattern for parsing creation timestamp from JSON. */
    private static final Pattern PATTERN_CREATE_TIME = Pattern.compile(REGEX_START + PROP_CREATE_TIME + REGEX_WORD_VALUE);
    /** Pattern for parsing start timestamp from JSON. */
    private static final Pattern PATTERN_START_TIME = Pattern.compile(REGEX_START + PROP_START_TIME + REGEX_WORD_VALUE);
    /** Pattern for parsing stop timestamp from JSON. */
    private static final Pattern PATTERN_STOP_TIME = Pattern.compile(REGEX_START + PROP_STOP_TIME + REGEX_WORD_VALUE);
    /** Pattern for parsing current iteration from JSON. */
    private static final Pattern PATTERN_ITERATION = Pattern.compile(REGEX_START + PROP_ITERATION + REGEX_WORD_VALUE);
    /** Pattern for parsing expected iterations from JSON. */
    private static final Pattern PATTERN_EXPECTED_ITERATION = Pattern.compile(REGEX_START + PROP_EXPECTED_ITERATION + REGEX_WORD_VALUE);
    /** Pattern for parsing time limit from JSON. */
    private static final Pattern PATTERN_LIMIT_TIME = Pattern.compile(REGEX_START + PROP_LIMIT_TIME + REGEX_WORD_VALUE);
    /** Pattern for parsing event category from JSON. */
    private static final Pattern PATTERN_EVENT_CATEGORY = Pattern.compile(REGEX_START + EVENT_CATEGORY + REGEX_WORD_VALUE);
    /** Pattern for parsing event name from JSON. */
    private static final Pattern PATTERN_EVENT_NAME = Pattern.compile(REGEX_START + EVENT_NAME + REGEX_WORD_VALUE);
    /** Pattern for parsing parent event identifier from JSON. */
    private static final Pattern PATTERN_EVENT_PARENT = Pattern.compile(REGEX_START + EVENT_PARENT + REGEX_WORD_VALUE);
    /** Pattern for parsing context map from JSON. */
    private static final Pattern PATTERN_CONTEXT = Pattern.compile(REGEX_START + PROP_CONTEXT + "\\s*:\\s*\\{([^}]*)\\}");

    /**
     * Serializes MeterData attributes to JSON5-like format and appends them to the provided StringBuilder.
     * Only non-null and non-zero values are serialized to minimize output size.
     *
     * @param data The MeterData object to serialize.
     * @param sb   The StringBuilder to append the serialized data to.
     */
    
    @SuppressWarnings("MagicCharacter")
    void write(final MeterData data, final StringBuilder sb) {
        if (data.description != null) {
            sb.append(String.format(",%s:'%s'", PROP_DESCRIPTION, data.description));
        }
        if (data.rejectPath != null) {
            sb.append(String.format(",%s:%s", PROP_REJECT_ID, data.rejectPath));
        }
        if (data.okPath != null) {
            sb.append(String.format(",%s:%s", PROP_PATH_ID, data.okPath));
        }
        if (data.failPath != null) {
            sb.append(String.format(",%s:%s", PROP_FAIL_ID, data.failPath));
        }
        if (data.failMessage != null) {
            sb.append(String.format(",%s:'%s'", PROP_FAIL_MESSAGE, data.failMessage));
        }
        if (data.category != null) {
            sb.append(String.format(",%s:%s", EVENT_CATEGORY, data.category));
        }
        if (data.operation != null) {
            sb.append(String.format(",%s:%s", EVENT_NAME, data.operation));
        }
        if (data.parent != null) {
            sb.append(String.format(",%s:%s", EVENT_PARENT, data.parent));
        }
        if (data.createTime != 0) {
            sb.append(String.format(",%s:%d", PROP_CREATE_TIME, data.createTime));
        }
        if (data.startTime != 0) {
            sb.append(String.format(",%s:%d", PROP_START_TIME, data.startTime));
        }
        if (data.stopTime != 0) {
            sb.append(String.format(",%s:%d", PROP_STOP_TIME, data.stopTime));
        }
        if (data.currentIteration != 0) {
            sb.append(String.format(",%s:%d", PROP_ITERATION, data.currentIteration));
        }
        if (data.expectedIterations != 0) {
            sb.append(String.format(",%s:%d", PROP_EXPECTED_ITERATION, data.expectedIterations));
        }
        if (data.timeLimit != 0) {
            sb.append(String.format(",%s:%d", PROP_LIMIT_TIME, data.timeLimit));
        }
        if (data.context != null && !data.context.isEmpty()) {
            sb.append(',');
            sb.append(PROP_CONTEXT);
            sb.append(":{");
            boolean separatorNeeded = false;
            for (final Map.Entry<String, String> entry : data.context.entrySet()) {
                if (separatorNeeded) {
                    sb.append(',');
                } else {
                    separatorNeeded = true;
                }
                sb.append(entry.getKey());
                sb.append(':');
                if (entry.getValue() != null) {
                    sb.append(entry.getValue());
                }
            }
            sb.append('}');
        }
    }

    /**
     * Deserializes a JSON5-like string and populates the provided MeterData object.
     * Only properties present in the JSON string are set; absent properties remain unchanged.
     *
     * @param data  The MeterData object to populate with deserialized values.
     * @param json5 The JSON5-like string to parse.
     */
    /* Suppressed warning: String.equals() is intentionally used for pattern matching */
    @SuppressWarnings("CallToSuspiciousStringMethod")
    void read(final MeterData data, final String json5) {
        final Matcher matcherDescription = PATTERN_DESCRIPTION.matcher(json5);
        if (matcherDescription.find()) {
            data.description = matcherDescription.group(1);
        }
        final Matcher matcherPathId = PATTERN_PATH_ID.matcher(json5);
        if (matcherPathId.find()) {
            data.okPath = matcherPathId.group(1);
        }
        final Matcher matcherRejectId = PATTERN_REJECT_ID.matcher(json5);
        if (matcherRejectId.find()) {
            data.rejectPath = matcherRejectId.group(1);
        }
        final Matcher matcherFailId = PATTERN_FAIL_ID.matcher(json5);
        if (matcherFailId.find()) {
            data.failPath = matcherFailId.group(1);
        }
        final Matcher matcherFailMessage = PATTERN_FAIL_MESSAGE.matcher(json5);
        if (matcherFailMessage.find()) {
            data.failMessage = matcherFailMessage.group(1);
        }
        final Matcher matcherCreateTime = PATTERN_CREATE_TIME.matcher(json5);
        if (matcherCreateTime.find()) {
            data.createTime = Long.parseLong(matcherCreateTime.group(1));
        }
        final Matcher matcherStartTime = PATTERN_START_TIME.matcher(json5);
        if (matcherStartTime.find()) {
            data.startTime = Long.parseLong(matcherStartTime.group(1));
        }
        final Matcher matcherStopTime = PATTERN_STOP_TIME.matcher(json5);
        if (matcherStopTime.find()) {
            data.stopTime = Long.parseLong(matcherStopTime.group(1));
        }
        final Matcher matcherIteration = PATTERN_ITERATION.matcher(json5);
        if (matcherIteration.find()) {
            data.currentIteration = Long.parseLong(matcherIteration.group(1));
        }
        final Matcher matcherExpectedIteration = PATTERN_EXPECTED_ITERATION.matcher(json5);
        if (matcherExpectedIteration.find()) {
            data.expectedIterations = Long.parseLong(matcherExpectedIteration.group(1));
        }
        final Matcher matcherLimitTime = PATTERN_LIMIT_TIME.matcher(json5);
        if (matcherLimitTime.find()) {
            data.timeLimit = Long.parseLong(matcherLimitTime.group(1));
        }
        final Matcher matcherEventCategory = PATTERN_EVENT_CATEGORY.matcher(json5);
        if (matcherEventCategory.find()) {
            data.category = matcherEventCategory.group(1);
        }
        final Matcher matcherEventName = PATTERN_EVENT_NAME.matcher(json5);
        if (matcherEventName.find()) {
            data.operation = matcherEventName.group(1);
        }
        final Matcher matcherEventParent = PATTERN_EVENT_PARENT.matcher(json5);
        if (matcherEventParent.find()) {
            data.parent = matcherEventParent.group(1);
        }
        final Matcher matcherContext = PATTERN_CONTEXT.matcher(json5);
        if (matcherContext.find()) {
            final String contextString = matcherContext.group(1);
            if (contextString != null && !contextString.isEmpty()) {
                final String[] contextEntries = contextString.split(",");
                data.context = new HashMap<>(10);
                for (final String entry : contextEntries) {
                    final String[] keyValue = entry.split(":");
                    if (keyValue.length >= 1) {
                        final String key = keyValue[0].trim();
                        final String value = (keyValue.length > 1) ? keyValue[1].trim() : null;
                        data.context.put(key, value);
                    }
                }
            }
        }
    }
}
