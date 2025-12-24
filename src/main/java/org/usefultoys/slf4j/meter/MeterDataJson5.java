/*
 * Copyright 2025 Daniel Felix Ferber
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
 */
@UtilityClass
class MeterDataJson5 {

    private final String PROP_DESCRIPTION = "d";
    private final String PROP_PATH_ID = "p";
    private final String PROP_REJECT_ID = "r";
    private final String PROP_FAIL_ID = "f";
    private final String PROP_FAIL_MESSAGE = "fm";
    private final String PROP_CREATE_TIME = "t0";
    private final String PROP_START_TIME = "t1";
    private final String PROP_STOP_TIME = "t2";
    private final String PROP_ITERATION = "i";
    private final String PROP_EXPECTED_ITERATION = "ei";
    private final String PROP_LIMIT_TIME = "tl";
    private final String PROP_CONTEXT = "ctx";
    private final String EVENT_CATEGORY = "c";
    private final String EVENT_NAME = "n";
    private final String EVENT_PARENT = "ep";

    private final String REGEX_START = "[{,]";
    private final String REGEX_STRING_VALUE = "\\s*:\\s*'([^']*)'";
    private final String REGEX_WORD_VALUE = "\\s*:\\s*([^,}\\s]+)";

    private final Pattern patternDescription = Pattern.compile(REGEX_START + PROP_DESCRIPTION + REGEX_STRING_VALUE);
    private final Pattern patternPathId = Pattern.compile(REGEX_START + PROP_PATH_ID + REGEX_WORD_VALUE);
    private final Pattern patternRejectId = Pattern.compile(REGEX_START + PROP_REJECT_ID + REGEX_WORD_VALUE);
    private final Pattern patternFailId = Pattern.compile(REGEX_START + PROP_FAIL_ID + REGEX_WORD_VALUE);
    private final Pattern patternFailMessage = Pattern.compile(REGEX_START + PROP_FAIL_MESSAGE + REGEX_STRING_VALUE);
    private final Pattern patternCreateTime = Pattern.compile(REGEX_START + PROP_CREATE_TIME + REGEX_WORD_VALUE);
    private final Pattern patternStartTime = Pattern.compile(REGEX_START + PROP_START_TIME + REGEX_WORD_VALUE);
    private final Pattern patternStopTime = Pattern.compile(REGEX_START + PROP_STOP_TIME + REGEX_WORD_VALUE);
    private final Pattern patternIteration = Pattern.compile(REGEX_START + PROP_ITERATION + REGEX_WORD_VALUE);
    private final Pattern patternExpectedIteration = Pattern.compile(REGEX_START + PROP_EXPECTED_ITERATION + REGEX_WORD_VALUE);
    private final Pattern patternLimitTime = Pattern.compile(REGEX_START + PROP_LIMIT_TIME + REGEX_WORD_VALUE);
    private final Pattern patternEventCategory = Pattern.compile(REGEX_START + EVENT_CATEGORY + REGEX_WORD_VALUE);
    private final Pattern patternEventName = Pattern.compile(REGEX_START + EVENT_NAME + REGEX_WORD_VALUE);
    private final Pattern patternEventParent = Pattern.compile(REGEX_START + EVENT_PARENT + REGEX_WORD_VALUE);
    private final Pattern patternContext = Pattern.compile(REGEX_START + PROP_CONTEXT + "\\s*:\\s*\\{([^}]*)\\}");

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

    @SuppressWarnings("CallToSuspiciousStringMethod")
    void read(final MeterData data, final String json5) {
        final Matcher matcherDescription = patternDescription.matcher(json5);
        if (matcherDescription.find()) {
            data.description = matcherDescription.group(1);
        }
        final Matcher matcherPathId = patternPathId.matcher(json5);
        if (matcherPathId.find()) {
            data.okPath = matcherPathId.group(1);
        }
        final Matcher matcherRejectId = patternRejectId.matcher(json5);
        if (matcherRejectId.find()) {
            data.rejectPath = matcherRejectId.group(1);
        }
        final Matcher matcherFailId = patternFailId.matcher(json5);
        if (matcherFailId.find()) {
            data.failPath = matcherFailId.group(1);
        }
        final Matcher matcherFailMessage = patternFailMessage.matcher(json5);
        if (matcherFailMessage.find()) {
            data.failMessage = matcherFailMessage.group(1);
        }
        final Matcher matcherCreateTime = patternCreateTime.matcher(json5);
        if (matcherCreateTime.find()) {
            data.createTime = Long.parseLong(matcherCreateTime.group(1));
        }
        final Matcher matcherStartTime = patternStartTime.matcher(json5);
        if (matcherStartTime.find()) {
            data.startTime = Long.parseLong(matcherStartTime.group(1));
        }
        final Matcher matcherStopTime = patternStopTime.matcher(json5);
        if (matcherStopTime.find()) {
            data.stopTime = Long.parseLong(matcherStopTime.group(1));
        }
        final Matcher matcherIteration = patternIteration.matcher(json5);
        if (matcherIteration.find()) {
            data.currentIteration = Long.parseLong(matcherIteration.group(1));
        }
        final Matcher matcherExpectedIteration = patternExpectedIteration.matcher(json5);
        if (matcherExpectedIteration.find()) {
            data.expectedIterations = Long.parseLong(matcherExpectedIteration.group(1));
        }
        final Matcher matcherLimitTime = patternLimitTime.matcher(json5);
        if (matcherLimitTime.find()) {
            data.timeLimit = Long.parseLong(matcherLimitTime.group(1));
        }
        final Matcher matcherEventCategory = patternEventCategory.matcher(json5);
        if (matcherEventCategory.find()) {
            data.category = matcherEventCategory.group(1);
        }
        final Matcher matcherEventName = patternEventName.matcher(json5);
        if (matcherEventName.find()) {
            data.operation = matcherEventName.group(1);
        }
        final Matcher matcherEventParent = patternEventParent.matcher(json5);
        if (matcherEventParent.find()) {
            data.parent = matcherEventParent.group(1);
        }
        final Matcher matcherContext = patternContext.matcher(json5);
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
