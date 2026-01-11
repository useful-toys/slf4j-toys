# ArchUnit Test Recommendations for slf4j-toys

**Date**: 2026-01-11
**Status**: Proposal

## Overview

This document outlines recommended ArchUnit tests to enforce and validate the architectural decisions and conventions established in this project. ArchUnit tests serve as executable documentation that prevents architectural drift and ensures consistency across the codebase.

## Benefits

*   **Enforces TDR compliance**: Validates that code follows documented Technical Decision Records
*   **Prevents regression**: Automatically catches violations of established patterns
*   **Living documentation**: Tests serve as executable specifications of architectural rules
*   **Facilitates onboarding**: New developers immediately understand architectural constraints through test failures
*   **Continuous validation**: CI/CD pipeline automatically verifies architectural integrity

## Dependencies

Add ArchUnit to the test dependencies in `pom.xml`:

```xml
<dependency>
    <groupId>com.tngtech.archunit</groupId>
    <artifactId>archunit-junit5</artifactId>
    <version>${archunit.version}</version>
    <scope>test</scope>
</dependency>
```

The version is already defined in the POM as `<archunit.version>1.4.1</archunit.version>`.

---

## Priority 1: Naming Conventions

### Rationale
The project follows clear naming patterns for different types of classes. Enforcing these conventions ensures consistency and helps developers quickly understand class responsibilities.

### Rules

#### 1.1 Data Classes Must End with "Data"

Classes representing Value Objects or DTOs must follow the `*Data` naming convention and be immutable.

```java
@ArchTest
static final ArchRule dataClassesNamingRule = classes()
    .that().haveSimpleNameEndingWith("Data")
    .and().resideInAPackage("org.usefultoys.slf4j..")
    .should().bePackagePrivate().orShould().bePublic()
    .andShould().haveModifier(JavaModifier.FINAL)
    .because("Data classes follow the VO pattern and must be immutable (TDR-0007)");
```

**Applies to**: `MeterData`, `WatcherData`, `SystemData`, `EventData`

#### 1.2 Formatter Classes Must End with "Formatter"

Formatter classes handle string representation and must be package-private utilities.

```java
@ArchTest
static final ArchRule formatterClassesNamingRule = classes()
    .that().haveSimpleNameEndingWith("Formatter")
    .and().resideInAPackage("org.usefultoys.slf4j..")
    .should().bePackagePrivate()
    .andShould().haveModifier(JavaModifier.FINAL)
    .because("Formatter classes are internal utilities and should not be exposed (TDR-0007)");
```

**Applies to**: `MeterDataFormatter`, `WatcherDataFormatter`

#### 1.3 Config Classes Must End with "Config"

Configuration classes must use Lombok's `@UtilityClass` annotation.

```java
@ArchTest
static final ArchRule configClassesNamingRule = classes()
    .that().haveSimpleNameEndingWith("Config")
    .and().resideInAPackage("org.usefultoys.slf4j..")
    .should().beAnnotatedWith(UtilityClass.class)
    .because("Config classes are static utility classes for centralized configuration (TDR-0005)");
```

**Applies to**: `SystemConfig`, `SessionConfig`, `MeterConfig`, `WatcherConfig`, `ReporterConfig`

#### 1.4 Json5 Classes Must End with "Json5"

Classes handling JSON5 serialization must follow the `*Json5` naming convention.

```java
@ArchTest
static final ArchRule json5ClassesNamingRule = classes()
    .that().haveSimpleNameEndingWith("Json5")
    .and().resideInAPackage("org.usefultoys.slf4j..")
    .should().bePackagePrivate()
    .andShould().beAnnotatedWith(UtilityClass.class)
    .because("JSON5 serialization utilities are internal and follow manual implementation (TDR-0004)");
```

**Applies to**: `MeterDataJson5`, `SystemDataJson5`, `EventDataJson5`

#### 1.5 Validator Classes Must End with "Validator"

Validator classes must be package-private utilities.

```java
@ArchTest
static final ArchRule validatorClassesNamingRule = classes()
    .that().haveSimpleNameEndingWith("Validator")
    .and().resideInAPackage("org.usefultoys.slf4j..")
    .should().bePackagePrivate()
    .andShould().haveModifier(JavaModifier.FINAL)
    .because("Validator classes are internal utilities for non-intrusive validation (TDR-0017)");
```

**Applies to**: `MeterValidator`

---

## Priority 1: Package Dependencies

### Rationale
Clear package boundaries prevent tight coupling and ensure modules remain independently testable and maintainable.

### Rules

#### 2.1 Internal Package Must Not Be Accessed From Outside

The `internal` package contains implementation details that should never be directly accessed.

```java
@ArchTest
static final ArchRule internalPackageIsolationRule = noClasses()
    .that().resideOutsideOfPackage("..internal..")
    .should().dependOnClassesThat().resideInAPackage("..internal..")
    .because("Internal package contains implementation details that must not leak (encapsulation principle)");
```

**Applies to**: `org.usefultoys.slf4j.internal` package

#### 2.2 Report Module Independence

The `report` module must be independent of `meter` and `watcher` modules.

```java
@ArchTest
static final ArchRule reportModuleIndependenceRule = noClasses()
    .that().resideInAPackage("..report..")
    .should().dependOnClassesThat().resideInAnyPackage("..meter..", "..watcher..")
    .because("Report module is decoupled from other diagnostic modules (TDR-0014)");
```

**Applies to**: `org.usefultoys.slf4j.report` package

#### 2.3 Utils Package Independence

The `utils` package must not depend on any domain-specific packages.

```java
@ArchTest
static final ArchRule utilsPackageIndependenceRule = noClasses()
    .that().resideInAPackage("..utils..")
    .should().dependOnClassesThat().resideInAnyPackage("..meter..", "..watcher..", "..report..")
    .because("Utils package provides reusable utilities and must remain domain-agnostic");
```

**Applies to**: `org.usefultoys.slf4j.utils` package

#### 2.4 Meter and Watcher Independence

`meter` and `watcher` modules should not depend on each other.

```java
@ArchTest
static final ArchRule meterWatcherIndependenceRule = noClasses()
    .that().resideInAPackage("..meter..")
    .should().dependOnClassesThat().resideInAPackage("..watcher..")
    .andShould().notBeDependentOn(classes().that().resideInAPackage("..watcher.."))
    .because("Meter and Watcher are independent diagnostic modules");

@ArchTest
static final ArchRule watcherMeterIndependenceRule = noClasses()
    .that().resideInAPackage("..watcher..")
    .should().dependOnClassesThat().resideInAPackage("..meter..")
    .because("Meter and Watcher are independent diagnostic modules");
```

**Applies to**: `org.usefultoys.slf4j.meter` and `org.usefultoys.slf4j.watcher` packages

---

## Priority 1: Data and Behavior Separation (VO Pattern)

### Rationale
The project follows the VO Pattern with behavior externalized via mixins and utility classes (TDR-0007). This ensures clean serialization and separation of concerns.

### Rules

#### 3.1 Data Classes Must Be Immutable

Data classes should have only final fields (enforced by Lombok's `@Value` or manual implementation).

```java
@ArchTest
static final ArchRule dataClassesImmutabilityRule = classes()
    .that().haveSimpleNameEndingWith("Data")
    .and().resideInAPackage("org.usefultoys.slf4j..")
    .should().haveOnlyFinalFields()
    .because("Data classes follow the VO pattern and must be immutable (TDR-0007)");
```

#### 3.2 Data Classes Must Not Have Business Logic Methods

Data classes should primarily contain getters and simple calculated properties.

```java
@ArchTest
static final ArchRule dataClassesNoBusinessLogicRule = noMethods()
    .that().areDeclaredInClassesThat().haveSimpleNameEndingWith("Data")
    .and().areDeclaredInClassesThat().resideInAPackage("org.usefultoys.slf4j..")
    .should().haveNameMatching("(set|update|process|execute|perform).*")
    .because("Data classes should not contain business logic - use mixins or utilities (TDR-0007)");
```

#### 3.3 Formatter Classes Must Not Be Instantiated

Formatter classes are utilities and should have private constructors.

```java
@ArchTest
static final ArchRule formatterClassesUtilityRule = classes()
    .that().haveSimpleNameEndingWith("Formatter")
    .and().resideInAPackage("org.usefultoys.slf4j..")
    .should().haveOnlyPrivateConstructors()
    .because("Formatter classes are utilities and should not be instantiated (TDR-0007)");
```

---

## Priority 2: Production Dependencies

### Rationale
The library must maintain minimal dependencies (only SLF4J and Lombok for production code) to avoid dependency conflicts in client applications.

### Rules

#### 4.1 Production Code Dependencies

Production code must only depend on allowed libraries.

```java
@ArchTest
static final ArchRule productionDependenciesRule = classes()
    .that().resideInAPackage("org.usefultoys.slf4j..")
    .and().areNotAnnotatedWith(Test.class)
    .should().onlyDependOnClassesThat(
        resideInAnyPackage(
            "org.usefultoys.slf4j..",
            "org.slf4j..",
            "lombok..",
            "java..",
            "javax.servlet..",
            "jakarta.servlet.."
        )
    )
    .because("Production code must maintain minimal dependencies (AI-INSTRUCTIONS.md)");
```

#### 4.2 No Test Code in Production Classes

Test utilities must not leak into production code.

```java
@ArchTest
static final ArchRule noTestDependenciesRule = noClasses()
    .that().resideInAPackage("org.usefultoys.slf4j..")
    .and().resideOutsideOfPackages("..test..")
    .should().dependOnClassesThat().resideInAnyPackage(
        "org.junit..",
        "org.mockito..",
        "..test.."
    )
    .because("Production code must not depend on test frameworks or test utilities");
```

---

## Priority 2: Marker Classes

### Rationale
Marker classes contain SLF4J `Marker` constants used for log channel separation (TDR-0003). They must be public and contain only constants.

### Rules

#### 5.1 Markers Class Structure

```java
@ArchTest
static final ArchRule markersClassStructureRule = classes()
    .that().haveSimpleName("Markers")
    .and().resideInAPackage("org.usefultoys.slf4j..")
    .should().bePublic()
    .andShould().haveModifier(JavaModifier.FINAL)
    .andShould().haveOnlyFinalFields()
    .because("Markers classes contain constants for log channel separation (TDR-0003)");
```

**Applies to**: `org.usefultoys.slf4j.meter.Markers`, `org.usefultoys.slf4j.watcher.Markers`

#### 5.2 Markers Must Be Public Static Final

```java
@ArchTest
static final ArchRule markersFieldsRule = fields()
    .that().areDeclaredInClassesThat().haveSimpleName("Markers")
    .and().areDeclaredInClassesThat().resideInAPackage("org.usefultoys.slf4j..")
    .should().bePublic()
    .andShould().beStatic()
    .andShould().beFinal()
    .because("Marker constants must be accessible and immutable (TDR-0003)");
```

---

## Priority 2: Lombok Usage

### Rationale
The project uses Lombok to reduce boilerplate (TDR-0002). Utility classes must follow the `@UtilityClass` pattern.

### Rules

#### 6.1 UtilityClass Annotation Requires Private Constructor

```java
@ArchTest
static final ArchRule utilityClassConstructorRule = classes()
    .that().areAnnotatedWith(UtilityClass.class)
    .should().haveOnlyPrivateConstructors()
    .because("@UtilityClass annotation ensures private constructor (TDR-0002)");
```

#### 6.2 Config Classes Must Use UtilityClass

```java
@ArchTest
static final ArchRule configClassesLombokRule = classes()
    .that().haveSimpleNameEndingWith("Config")
    .and().resideInAPackage("org.usefultoys.slf4j..")
    .and().resideOutsideOfPackages("..test..")
    .should().beAnnotatedWith(UtilityClass.class)
    .because("Config classes are static utility classes (TDR-0005)");
```

---

## Priority 3: Test Structure

### Rationale
Tests must follow consistent conventions for maintainability and readability (AI-PROJECT-INSTRUCTIONS.md).

### Rules

#### 7.1 Test Classes Must Have DisplayName

```java
@ArchTest
static final ArchRule testClassDisplayNameRule = classes()
    .that().areAnnotatedWith(Test.class)
    .or().areMetaAnnotatedWith(Test.class)
    .should().beAnnotatedWith(DisplayName.class)
    .because("All test classes must have descriptive @DisplayName annotations (AI-PROJECT-INSTRUCTIONS.md)");
```

#### 7.2 Test Methods Must Have DisplayName

```java
@ArchTest
static final ArchRule testMethodDisplayNameRule = methods()
    .that().areAnnotatedWith(Test.class)
    .should().beAnnotatedWith(DisplayName.class)
    .because("All test methods must have descriptive @DisplayName annotations (AI-PROJECT-INSTRUCTIONS.md)");
```

#### 7.3 Test Class Naming Convention

```java
@ArchTest
static final ArchRule testClassNamingRule = classes()
    .that().resideInAPackage("..test..")
    .and().areAnnotatedWith(Test.class)
    .or().areMetaAnnotatedWith(Test.class)
    .should().haveSimpleNameEndingWith("Test")
    .because("Test classes must end with 'Test' suffix");
```

---

## Priority 3: Logback Integration

### Rationale
The `logback` package contains Logback-specific implementations that must not leak into the core library.

### Rules

#### 8.1 Logback Package Isolation

```java
@ArchTest
static final ArchRule logbackPackageIsolationRule = noClasses()
    .that().resideOutsideOfPackage("..logback..")
    .should().dependOnClassesThat().resideInAPackage("..logback..")
    .because("Logback-specific code must not leak into core library (TDR-0010, TDR-0028)");
```

#### 8.2 No ch.qos.logback Dependencies in Core

```java
@ArchTest
static final ArchRule noLogbackDependenciesInCoreRule = noClasses()
    .that().resideInAPackage("org.usefultoys.slf4j..")
    .and().resideOutsideOfPackages("..logback..", "..test..")
    .should().dependOnClassesThat().resideInAPackage("ch.qos.logback..")
    .because("Core library must only depend on SLF4J API, not implementations (TDR-0010)");
```

---

## Priority 3: Servlet Support

### Rationale
The project supports both javax and jakarta servlet specifications (TDR-0009). Implementations must be properly isolated.

### Rules

#### 9.1 No Direct javax.servlet in jakarta Classes

```java
@ArchTest
static final ArchRule noJavaxInJakartaRule = noClasses()
    .that().haveSimpleNameContaining("Jakarta")
    .and().resideInAPackage("org.usefultoys.slf4j..")
    .should().dependOnClassesThat().resideInAPackage("javax.servlet..")
    .because("Jakarta servlet implementations must not reference javax servlet (TDR-0009)");
```

#### 9.2 No Direct jakarta.servlet in javax Classes

```java
@ArchTest
static final ArchRule noJakartaInJavaxRule = noClasses()
    .that().haveSimpleNameContaining("Javax")
    .and().resideInAPackage("org.usefultoys.slf4j..")
    .should().dependOnClassesThat().resideInAPackage("jakarta.servlet..")
    .because("javax servlet implementations must not reference jakarta servlet (TDR-0009)");
```

---

## Implementation Plan

### Phase 1: Core Architecture (Priority 1)
1. Create `ArchitectureTest.java` in `src/test/java/org/usefultoys/slf4j/`
2. Implement naming convention rules (1.x)
3. Implement package dependency rules (2.x)
4. Implement VO pattern rules (3.x)
5. Run tests and fix violations
6. Integrate into CI/CD pipeline

### Phase 2: Dependencies and Conventions (Priority 2)
1. Implement production dependency rules (4.x)
2. Implement marker class rules (5.x)
3. Implement Lombok rules (6.x)
4. Run tests and document any necessary exceptions

### Phase 3: Testing and Integration (Priority 3)
1. Implement test structure rules (7.x)
2. Implement Logback isolation rules (8.x)
3. Implement servlet isolation rules (9.x)
4. Document all rules in code comments
5. Update README.md with ArchUnit information

---

## Suggested Test Class Structure

```java
package org.usefultoys.slf4j;

import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import lombok.experimental.UtilityClass;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

/**
 * ArchUnit tests to enforce architectural decisions and conventions.
 * <p>
 * These tests validate compliance with TDRs (Technical Decision Records) and
 * coding standards documented in AI-INSTRUCTIONS.md and AI-PROJECT-INSTRUCTIONS.md.
 */
@AnalyzeClasses(packages = "org.usefultoys.slf4j")
@DisplayName("Architecture Tests")
class ArchitectureTest {

    @Nested
    @DisplayName("Naming Conventions")
    class NamingConventionsTests {
        // Rules 1.1 to 1.5
    }

    @Nested
    @DisplayName("Package Dependencies")
    class PackageDependenciesTests {
        // Rules 2.1 to 2.4
    }

    @Nested
    @DisplayName("Data and Behavior Separation")
    class DataBehaviorSeparationTests {
        // Rules 3.1 to 3.3
    }

    @Nested
    @DisplayName("Production Dependencies")
    class ProductionDependenciesTests {
        // Rules 4.1 to 4.2
    }

    @Nested
    @DisplayName("Marker Classes")
    class MarkerClassesTests {
        // Rules 5.1 to 5.2
    }

    @Nested
    @DisplayName("Lombok Usage")
    class LombokUsageTests {
        // Rules 6.1 to 6.2
    }

    @Nested
    @DisplayName("Test Structure")
    class TestStructureTests {
        // Rules 7.1 to 7.3
    }

    @Nested
    @DisplayName("Logback Integration")
    class LogbackIntegrationTests {
        // Rules 8.1 to 8.2
    }

    @Nested
    @DisplayName("Servlet Support")
    class ServletSupportTests {
        // Rules 9.1 to 9.2
    }
}
```

---

## Exception Handling

Some rules may have legitimate exceptions. Document them using:

```java
@ArchTest
static final ArchRule ruleWithExceptions = classes()
    .that()./* condition */
    .should()./* assertion */
    .allowEmptyShould(true)  // Allow empty results if no classes match
    .as("Descriptive rule name")
    .because("Rationale with TDR reference");
```

For specific class exceptions:

```java
@ArchTest
static final ArchRule ruleWithSpecificExceptions = noClasses()
    .that()./* condition */
    .and().areNotAssignableTo(SpecificException.class)  // Exclude specific class
    .should()./* assertion */;
```

---

## Maintenance

*   **Review frequency**: Every major release or when new TDRs are added
*   **Update trigger**: When architectural patterns change or new modules are added
*   **Ownership**: Lead developer or architect
*   **Documentation**: Keep this document synchronized with TDRs

---

## References

*   [TDR-0002: Use of Lombok](./TDR-0002-use-of-lombok.md)
*   [TDR-0003: Separation of Log Channels](./TDR-0003-separation-of-log-channels.md)
*   [TDR-0004: Minimalist Manual JSON Implementation](./TDR-0004-minimalist-manual-json-implementation.md)
*   [TDR-0005: Robust and Minimalist Configuration Mechanism](./TDR-0005-robust-and-minimalist-configuration-mechanism.md)
*   [TDR-0007: Separation of Data and Behavior (VO Pattern)](./TDR-0007-separation-of-data-and-behavior-vo-pattern.md)
*   [TDR-0009: Multi-Spec Servlet Support (javax vs. jakarta)](./TDR-0009-multi-spec-servlet-support-javax-vs-jakarta.md)
*   [TDR-0010: Simultaneous Support for SLF4J 1.7, 2.0, and Logback 1.2, 1.5](./TDR-0010-simultaneous-support-for-slf4j-1.7-2.0-and-logback-1.2-1.5.md)
*   [TDR-0014: Modular and Decoupled Reporting Architecture](./TDR-0014-modular-and-decoupled-reporting-architecture.md)
*   [TDR-0017: Non-Intrusive Validation and Error Handling](./TDR-0017-non-intrusive-validation-and-error-handling.md)
*   [TDR-0028: Logback Lifecycle, Status, and Color Highlighting](./TDR-0028-logback-lifecycle-status-and-color-highlighting.md)
*   [AI-INSTRUCTIONS.md](../AI-INSTRUCTIONS.md)
*   [AI-PROJECT-INSTRUCTIONS.md](../AI-PROJECT-INSTRUCTIONS.md)
*   [ArchUnit User Guide](https://www.archunit.org/userguide/html/000_Index.html)
