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

import lombok.NonNull;
import org.slf4j.Logger;
import org.usefultoys.slf4j.LoggerFactory;

/**
 * Factory class for creating {@link Meter} instances with various configurations.
 * Provides convenient factory methods as shortcuts for calling the Meter constructor.
 * 
 * <p>This class follows the static factory pattern and cannot be instantiated.</p>
 *
 * @author Daniel Felix Ferber
 * @author Co-authored-by: GitHub Copilot using Claude Sonnet 4.5
 */
public final class MeterFactory {

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private MeterFactory() {
    }

    /**
     * Creates a Meter using the provided logger's name as the meter category.
     *
     * @param logger The logger used to report messages.
     * @return A new Meter instance.
     */
    @NonNull
    public static Meter getMeter(@NonNull final Logger logger) {
        return new Meter(logger);
    }

    /**
     * Creates a Meter with the specified category, which is also used as the logger name.
     *
     * @param category The meter category and logger name.
     * @return A new Meter instance.
     */
    @NonNull
    public static Meter getMeter(@NonNull final String category) {
        return new Meter(LoggerFactory.getLogger(category));
    }

    /**
     * Creates a Meter using the class name as both the category and logger name.
     *
     * @param clazz The class whose name is used as category and logger name.
     * @return A new Meter instance.
     */
    @NonNull
    public static Meter getMeter(@NonNull final Class<?> clazz) {
        return new Meter(LoggerFactory.getLogger(clazz));
    }

    /**
     * Creates a Meter using the class name as category and logger name, with an operation name.
     *
     * @param clazz         The class whose name is used as category and logger name.
     * @param operationName Additional identification to distinguish operations reported on the same logger.
     * @return A new Meter instance.
     */
    @NonNull
    public static Meter getMeter(@NonNull final Class<?> clazz, final String operationName) {
        return new Meter(LoggerFactory.getLogger(clazz), operationName);
    }

    /**
     * Creates a Meter using the provided logger, with an operation name.
     *
     * @param logger        The logger used to report messages.
     * @param operationName Additional identification to distinguish operations reported on the same logger.
     * @return A new Meter instance.
     */
    @NonNull
    public static Meter getMeter(@NonNull final Logger logger, final String operationName) {
        return new Meter(logger, operationName);
    }

    /**
     * Retrieves the most recently started Meter on the current thread.
     *
     * @return The current thread-local Meter instance.
     */
    @NonNull
    public static Meter getCurrentMeter() {
        return Meter.getCurrentInstance();
    }

    /**
     * Creates a subordinated Meter under the current thread's most recently started Meter.
     * The new meter inherits the category from the parent and appends the suboperation name to its hierarchy.
     * Useful for subdividing large tasks into smaller, individually reported tasks.
     *
     * @param suboperationName Additional identification appended to the subordinated meter name.
     * @return A new subordinated Meter instance.
     */
    @NonNull
    public static Meter getCurrentSubMeter(final String suboperationName) {
        return Meter.getCurrentInstance().sub(suboperationName);
    }

}
