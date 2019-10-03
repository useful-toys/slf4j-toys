/*
 * Copyright 2019 Daniel Felix Ferber
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.usefultoys.slf4j.meter;

import org.slf4j.Logger;
import org.usefultoys.slf4j.LoggerFactory;

/**
 * The MeterFactory is a utility class producing Meters.
 * The methods are shortcuts for calling the Meter constructor.
 *
 * @author Daniel Felix Ferber
 */
public class MeterFactory {

    /**
     * Returns a Meter with category equal to the logger name and as meter category.
     *
     * @param logger The logger used to report messages.
     * @return the Meter
     */
    public static Meter getMeter(final Logger logger) {
        return new Meter(logger);
    }

    /**
     * Returns a Meter with given category, which will also be also the logger name.
     *
     * @param category The Meter category and logger name.
     * @return the Meter
     */
    public static Meter getMeter(final String category) {
        return new Meter(LoggerFactory.getLogger(category));
    }

    /**
     * Returns a Meter with given category equal to the class name, which will also be the logger name.
     *
     * @param clazz The class which name is used as category and logger name.
     * @return the Meter
     */
    public static Meter getMeter(final Class<?> clazz) {
        return new Meter(LoggerFactory.getLogger(clazz));
    }

    /**
     * Returns a Meter with given category equal to the class name, which will also be the logger name.
     *
     * @param clazz         The class which name is used as category and logger name.
     * @param operationName Additional identification to distinguish operations reported on the same logger.
     * @return the Meter
     */
    public static Meter getMeter(final Class<?> clazz, final String operationName) {
        return new Meter(LoggerFactory.getLogger(clazz), operationName);
    }

    /**
     * Returns a Meter with given category equal to the logger name.
     *
     * @param logger        The logger used to report messages and as meter category.
     * @param operationName Additional identification to distinguish operations reported on the same logger.
     * @return the Meter
     */
    public static Meter getMeter(final Logger logger, final String operationName) {
        return new Meter(logger, operationName);
    }

    /**
     * Returns the most recently started Meter on the current thread.
     *
     * @return the Meter
     */
    public static Meter getCurrentMeter() {
        return Meter.getCurrentInstance();
    }

    /**
     * Creates a new Meter whose name is subordinated under the hierarchy last started meter onthe current thread.
     * Useful if a large task may be subdivided into smaller task and reported individually.
     * The new meter uses the category of this meter. The new meter uses a name of the this meter, appended by slash and its own name.
     *
     * @param suboperationName Additional identification appended to the subordinated meter name.
     * @return the  Meter
     */
    public static Meter getCurrentSubMeter(String suboperationName) {
        return Meter.getCurrentInstance().sub(suboperationName);
    }

}
