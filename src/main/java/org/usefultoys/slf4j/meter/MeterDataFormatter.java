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

import org.usefultoys.slf4j.utils.UnitFormatter;

import java.util.Map;

/**
 * Formats {@link MeterData} into a human-readable string representation.
 * This class centralizes the logic for building log messages based on {@link MeterConfig} settings.
 *
 * @author Daniel Felix Ferber
 */
final class MeterDataFormatter {

    private MeterDataFormatter() {
        // Utility class
    }

    /**
     * Appends a human-readable string representation of the MeterData to the provided {@link StringBuilder}.
     * This method formats the operation's status, identification, timing, and context based on {@link MeterConfig}.
     *
     * @param data    The MeterData object to format.
     * @param builder The StringBuilder that receives the string representation.
     */
    @SuppressWarnings("MagicCharacter")
    public static void readableStringBuilder(final MeterData data, final StringBuilder builder) {
        final long executionTime = data.getExecutionTime();
        final boolean slow = data.isSlow();
        final boolean progressInfoRequired = executionTime > MeterConfig.progressPeriodMilliseconds;

        if (MeterConfig.printStatus) {
            if (data.isStopped()) {
                if (data.isReject()) {
                    builder.append("REJECT");
                } else if (data.isFail()) {
                    builder.append("FAIL");
                } else if (slow) {
                    builder.append("OK (Slow)");
                } else {
                    builder.append("OK");
                }
            } else if (data.isStarted()) {
                if (data.getCurrentIteration() == 0) {
                    builder.append("STARTED");
                } else if (slow) {
                    builder.append("PROGRESS (Slow)");
                } else {
                    builder.append("PROGRESS");
                }
            } else {
                builder.append("SCHEDULED");
            }
            builder.append(": ");
        }

        /* Identification. */
        boolean hasId = false;
        if (MeterConfig.printCategory) {
            final int index = data.getCategory().lastIndexOf('.') + 1;
            builder.append(data.getCategory().substring(index));
            hasId = true;
        }
        if (data.getOperation() != null) {
            if (MeterConfig.printCategory) {
                builder.append('/');
            }
            builder.append(data.getOperation());
            hasId = true;
        }
        if (MeterConfig.printPosition) {
            builder.append('#');
            builder.append(data.getPosition());
            hasId = true;
        }

        /* Execution path, if any. */
        final String path = data.getPath();
        if (path != null) {
            builder.append("[");
            builder.append(path);
            if (data.isFail() && data.getFailMessage() != null) {
                builder.append("; ");
                builder.append(data.getFailMessage());
            }
            builder.append(']');
            hasId = true;
        }
        if (hasId) {
            builder.append(' ');
        }

        /* Number of iterations. */
        boolean hasPrevious = false;
        if (data.isStarted() && data.getCurrentIteration() > 0) {
            hasPrevious = separator(builder, hasPrevious);
            builder.append(UnitFormatter.iterations(data.getCurrentIteration()));
            if (data.getExpectedIterations() > 0) {
                builder.append('/');
                builder.append(UnitFormatter.iterations(data.getExpectedIterations()));
            }
        }

        /* Timing. */
        if (!data.isStarted()) {
            hasPrevious = separator(builder, hasPrevious);
            builder.append(UnitFormatter.nanoseconds(data.getWaitingTime()));
        } else {
            if (data.isStopped() || progressInfoRequired) {
                hasPrevious = separator(builder, hasPrevious);
                builder.append(UnitFormatter.nanoseconds(executionTime));
            }

            if (data.getCurrentIteration() > 0 && (data.isStopped() || progressInfoRequired)) {
                hasPrevious = separator(builder, hasPrevious);
                final double iterationsPerSecond = data.getIterationsPerSecond();
                builder.append(UnitFormatter.iterationsPerSecond(iterationsPerSecond));
                builder.append(' ');
                final double nanoSecondsPerIteration = 1.0F / iterationsPerSecond * 1000000000;
                builder.append(UnitFormatter.nanoseconds(nanoSecondsPerIteration));
            }
        }

        /* Meta data. */
        if (data.getDescription() != null) {
            hasPrevious = separator(builder, hasPrevious);
            builder.append('\'');
            builder.append(data.getDescription());
            builder.append('\'');
        }
        final Map<String, String> context = data.getContext();
        if (context != null && !context.isEmpty()) {
            for (final Map.Entry<String, String> entry : context.entrySet()) {
                hasPrevious = separator(builder, hasPrevious);
                builder.append(entry.getKey());
                if (entry.getValue() != null) {
                    builder.append("=");
                    builder.append(entry.getValue());
                }
            }
        }

        /* System Info */
        if (MeterConfig.printMemory && data.getRuntime_maxMemory() > 0) {
            hasPrevious = MeterDataFormatter.separator(builder, hasPrevious);
            builder.append(UnitFormatter.bytes(data.getRuntime_usedMemory()));
        }
        if (MeterConfig.printLoad && data.getSystemLoad() > 0) {
            hasPrevious = MeterDataFormatter.separator(builder, hasPrevious);
            builder.append(Math.round(data.getSystemLoad() * 100));
            builder.append("%");
        }
        if (data.getSessionUuid() != null) {
            hasPrevious = MeterDataFormatter.separator(builder, hasPrevious);
            builder.append(data.getSessionUuid());
        }
    }

    private static boolean separator(final StringBuilder sb, final boolean hasPrevious) {
        if (hasPrevious) {
            sb.append("; ");
        }
        return true;
    }
}