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

package org.usefultoys.slf4j.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Architecture tests for SLF4J API compatibility.
 * <p>
 * These tests ensure that the library maintains backward compatibility with SLF4J 1.7.x
 * by preventing the use of SLF4J 2.0-specific APIs that are not available in earlier versions.
 * <p>
 * <b>Coverage:</b>
 * <ul>
 *   <li><b>Fluent Logging API Prohibition:</b> Verifies that code does not use SLF4J 2.0's fluent logging API
 *       (e.g., {@code logger.atInfo()}, {@code logger.atDebug()})</li>
 *   <li><b>LoggingEventBuilder Prohibition:</b> Ensures that {@code org.slf4j.spi.LoggingEventBuilder} interface
 *       (introduced in SLF4J 2.0) is not used anywhere in the codebase</li>
 *   <li><b>Backward Compatibility:</b> Guarantees that the library can work with both SLF4J 1.7.x and 2.0.x
 *       by restricting code to the common API surface</li>
 * </ul>
 *
 * @author Co-authored-by: GitHub Copilot using claude-sonnet-4.5
 */
@DisplayName("SLF4J API compatibility architecture tests")
class Slf4jCompatibilityArchTest {

    private static final JavaClasses IMPORTED_CLASSES = new ClassFileImporter()
            .importPackages("org.usefultoys.slf4j");

    /**
     * Test that verifies no code uses SLF4J 2.0's fluent logging API.
     * <p>
     * The fluent logging API was introduced in SLF4J 2.0 and is not available in SLF4J 1.7.x.
     * Using these methods would break backward compatibility with applications still using SLF4J 1.7.x.
     * <p>
     * Prohibited methods include:
     * <ul>
     *   <li>{@code Logger.atTrace()}</li>
     *   <li>{@code Logger.atDebug()}</li>
     *   <li>{@code Logger.atInfo()}</li>
     *   <li>{@code Logger.atWarn()}</li>
     *   <li>{@code Logger.atError()}</li>
     * </ul>
     */
    @Test
    @DisplayName("should not use SLF4J 2.0 fluent logging API")
    void shouldNotUseSlf4j2FluentLoggingApi() {
        // Given: ArchUnit rule that prohibits fluent logging API methods
        final ArchRule rule = noClasses()
                .should().callMethod("org.slf4j.Logger", "atTrace")
                .orShould().callMethod("org.slf4j.Logger", "atDebug")
                .orShould().callMethod("org.slf4j.Logger", "atInfo")
                .orShould().callMethod("org.slf4j.Logger", "atWarn")
                .orShould().callMethod("org.slf4j.Logger", "atError")
                .because("SLF4J 2.0 fluent API is not available in SLF4J 1.7.x and breaks backward compatibility");

        // When/Then: rule is checked against all classes in the library
        rule.check(IMPORTED_CLASSES);
    }

    /**
     * Test that verifies no code uses the {@code LoggingEventBuilder} interface.
     * <p>
     * The {@code LoggingEventBuilder} interface was introduced in SLF4J 2.0 as part of the fluent logging API.
     * Direct usage of this interface would break backward compatibility with SLF4J 1.7.x.
     * <p>
     * This rule prevents:
     * <ul>
     *   <li>Implementing {@code LoggingEventBuilder}</li>
     *   <li>Declaring fields or parameters of type {@code LoggingEventBuilder}</li>
     *   <li>Method return types of {@code LoggingEventBuilder}</li>
     *   <li>Any other direct reference to this SLF4J 2.0-specific interface</li>
     * </ul>
     */
    @Test
    @DisplayName("should not use SLF4J 2.0 LoggingEventBuilder interface")
    void shouldNotUseSlf4j2LoggingEventBuilder() {
        // Given: ArchUnit rule that prohibits LoggingEventBuilder interface usage
        final ArchRule rule = noClasses()
                .should().dependOnClassesThat().haveFullyQualifiedName("org.slf4j.spi.LoggingEventBuilder")
                .because("LoggingEventBuilder interface is only available in SLF4J 2.0 and breaks backward compatibility with SLF4J 1.7.x");

        // When/Then: rule is checked against all classes in the library
        rule.check(IMPORTED_CLASSES);
    }
}
