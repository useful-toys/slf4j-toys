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

package org.usefultoys.test;

import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Container annotation for multiple {@link ResetSystemProperty} annotations.
 * <p>
 * This annotation is automatically used when multiple {@code @ResetSystemProperty}
 * annotations are declared on the same test class or method. It should not be
 * used directly by developers.
 *
 * @see ResetSystemProperty
 * @author Daniel Felix Ferber
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
@ExtendWith(ResetSystemPropertyExtension.class)
public @interface ResetSystemProperties {

    /**
     * Array of {@link ResetSystemProperty} annotations.
     *
     * @return array of reset system property annotations
     */
    ResetSystemProperty[] value();
}

