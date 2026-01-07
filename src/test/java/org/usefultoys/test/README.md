# Test Utilities

This package provides JUnit 5 extensions and annotations to support test isolation and consistent test execution.

## Overview

All utilities are designed to ensure **test isolation** by resetting state between tests or controlling test environment conditions.

## Available Utilities

### Configuration Reset Extensions

These extensions reset configuration state before and after each test to prevent test interference.

#### `@ResetSystem` / `ResetSystemConfigExtension`

Resets `SystemConfig` and `SessionConfig` before and after each test.

**Usage:**
```java
@ResetSystem
class SystemConfigTest {
    @Test
    void testCustomConfig() {
        SystemConfig.someProperty = "custom";
        // Config is automatically reset after this test
    }
}
```

#### `@ResetSession` / `ResetSessionConfigExtension`

Resets `SessionConfig` and `SystemConfig` before and after each test.

**Usage:**
```java
@ResetSession
class SessionConfigTest {
    @Test
    void testCustomSession() {
        SessionConfig.someProperty = "custom";
        // Config is automatically reset after this test
    }
}
```

#### `@ResetReporter` / `ResetReporterConfigExtension`

Resets `ReporterConfig`, `SessionConfig`, and `SystemConfig` before and after each test.
This is the most comprehensive reset, covering all three configuration levels.

**Usage:**
```java
@ResetReporter
class ReporterConfigTest {
    @Test
    void testCustomReporter() {
        ReporterConfig.someProperty = "custom";
        // All configs are automatically reset after this test
    }
}
```

#### `@ResetMeterConfig` / `ResetMeterConfigExtension`

Resets `MeterConfig`, `SessionConfig`, and `SystemConfig` before and after each test.
This extension covers all meter-related configuration state.

**Usage:**
```java
@ResetMeterConfig
class MeterConfigTest {
    @Test
    void testCustomMeter() {
        MeterConfig.someProperty = "custom";
        // All configs are automatically reset after this test
    }
}
```

#### `@ResetWatcherConfig` / `ResetWatcherConfigExtension`

Resets `WatcherConfig`, `SessionConfig`, and `SystemConfig` before and after each test.
This extension covers all watcher-related configuration state.

**Usage:**
```java
@ResetWatcherConfig
class WatcherConfigTest {
    @Test
    void testCustomWatcher() {
        WatcherConfig.someProperty = "custom";
        // All configs are automatically reset after this test
    }
}
```

#### `@ResetSystemProperty` / `ResetSystemPropertyExtension`

Resets specific system properties before and after each test.
This annotation is repeatable, allowing multiple properties to be reset.

**Usage:**
```java
@ResetSystemProperty("my.custom.property")
@ResetSystemProperty("another.property")
class SystemPropertyTest {
    @Test
    void testCustomProperty() {
        System.setProperty("my.custom.property", "custom");
        // Property is automatically removed after this test
    }
}
```

#### `@ClearParserErrors` / `ClearConfigParserExtension`

Clears `ConfigParser` initialization errors before and after each test.

**Usage:**
```java
@ClearParserErrors
class ConfigParserTest {
    @Test
    void testInvalidConfig() {
        // ConfigParser starts with no accumulated errors
    }
}
```

### Environment Control Extensions

These extensions control test execution environment for consistent results.

#### `@WithLocale`

Temporarily sets the default `Locale` for a test class or method.

**Usage:**
```java
@WithLocale("en-US")
class LocaleSensitiveTest {
    @Test
    void testNumberFormatting() {
        // All tests run with en-US locale
    }
    
    @Test
    @WithLocale("pt-BR")  // Overrides class-level annotation
    void testWithPortugueseLocale() {
        // This test uses pt-BR locale
    }
}
```

**Common locale tags:**
- `"en"` - English
- `"en-US"` - English (United States)
- `"pt-BR"` - Portuguese (Brazil)
- `"fr-FR"` - French (France)
- `"de-DE"` - German (Germany)

#### `@ValidateCharset` / `CharsetConsistencyExtension`

Validates that the JVM's default charset matches `SessionConfig.charset` before running tests.

**Usage:**
```java
@ValidateCharset
class CharsetSensitiveTest {
    @Test
    void testStringEncoding() {
        // Test runs only if charset is consistent
    }
}
```

#### `@ValidateCleanMeter` / `ValidateCleanMeterExtension`

Validates that the Meter thread-local stack is clean before and after each test.
Ensures that `Meter.getCurrentInstance()` returns the "unknown" Meter (with category `"???"`)
both before and after each test method. This prevents Meter instances from leaking between tests.

**Behavior:**
- **Before each test:** Automatically cleans any leftover Meter instances from previous tests
- **After each test:**
  - **If test failed:** Cleans the stack to prevent cascade failures in subsequent tests
  - **If test passed:**
    - **Default (`expectDirtyStack = false`):** Validates stack is clean, fails if not
    - **With `expectDirtyStack = true`:** Cleans stack without validation (for tests intentionally leaving Meters)

**Basic Usage:**
```java
@ValidateCleanMeter
class MeterOperationTest {
    @Test
    void testMeterOperation() {
        // Meter stack is validated before and after this test
        Meter meter = new Meter(logger);
        try (Meter m = meter) {
            m.start();
            m.ok();
        }
        // Stack must be clean after test execution
    }
}
```

**Advanced Usage - Allowing Dirty Stack:**
```java
@ValidateCleanMeter
class MeterStackTest {
    @Test
    void testNormalCleanup() {
        // Must leave stack clean - test fails if dirty
        Meter meter = new Meter(logger);
        meter.start();
        meter.ok();
        meter.close();
    }
    
    @Test
    @ValidateCleanMeter(expectDirtyStack = true)
    void testStackContamination() {
        // Allowed to leave Meters on stack
        // Stack is cleaned automatically without test failure
        Meter meter = new Meter(logger);
        meter.start();
        // Intentionally not closing - testing contamination scenario
    }
}
```

**Common use cases:**
- Tests that create and use Meter instances
- Tests that verify Meter thread-local stack management
- Tests that intentionally leave Meters on stack (use `expectDirtyStack = true`)
- Integration tests that need to ensure Meter cleanup

## Choosing the Right Extension

### For Configuration Tests

- **Testing `SystemConfig` only** → Use `@ResetSystemConfig`
- **Testing `SessionConfig` only** → Use `@ResetSessionConfig`
- **Testing `ReporterConfig`** → Use `@ResetReporterConfig` (resets all levels)
- **Testing `MeterConfig`** → Use `@ResetMeterConfig` (resets all levels)
- **Testing `WatcherConfig`** → Use `@ResetWatcherConfig` (resets all levels)
- **Testing custom system properties** → Use `@ResetSystemProperty("property.name")`
- **Testing `ConfigParser` error handling** → Use `@ClearParserErrors`

### For Locale-Sensitive Tests

- **Number formatting, date formatting, string comparisons** → Use `@WithLocale("en-US")`
- Ensures consistent results across different OS environments

### For Charset-Sensitive Tests

- **File I/O, character encoding** → Use `@ValidateCharset`
- Fails fast if charset mismatch detected

### For Meter-Based Tests

- **Tests that use Meter** → Use `@ValidateCleanMeter`
- Ensures Meter thread-local stack doesn't leak between tests
- Validates stack is clean both before and after test execution
- **Tests that intentionally leave Meters on stack** → Use `@ValidateCleanMeter(expectDirtyStack = true)`
- Stack is cleaned automatically without causing test failure

## Design Notes

### Why Both Annotations and Extension Classes?

- **Annotations** (e.g., `@ResetSystem`) provide a cleaner, more declarative API
- **Extension classes** (e.g., `ResetSystemConfigExtension`) are used internally by annotations
- Both approaches are supported for flexibility

### Annotation vs. @ExtendWith
Config
```java
// Clean, declarative (recommended)
@ResetSystem
@ValidateCharset
class MyTest { }

// Verbose, but equivalent
@ExtendWith(ResetSystemConfigExtension.class)
@ExtendWith(CharsetConsistencyExtension.class)
class MyTest { }
```

### Method-Level vs. Class-Level

All annotations can be used at both levels:

```java
@ResetSystemConfig  // Applies to all tests in this class
class MyTest {
    
    @Test
    void test1() { }
    
    @Test
    @ResetSessionConfig  // Additional reset for this test only
    void test2() { }
}
```

## Best Practices

1. **Use the most specific reset needed** - Don't use `@ResetReporterConfig` if you only need `@ResetSessionConfig`
2. **Combine multiple extensions** - You can use multiple annotations on the same test
3. **Prefer annotations** - Use `@ResetSystemConfig` instead of `@ExtendWith(ResetSystemConfigExtension.class)`
4. **Use `@WithLocale` for cross-platform tests** - Ensures consistent number/date formatting
5. **Use `@ValidateCharset` for encoding tests** - Prevents charset-related bugs
6. **Use `@ResetSystemProperty` for custom properties** - Ensures custom system properties don't leak between tests
7. **Document why you need the extension** - Add a comment explaining locale-sensitive or configuration-dependent operations

## Examples

### Testing with Multiple Extensions

```java
@WithLocale(Config
@ValidateCharset
class ComprehensiveTest {
    @Test
    void testWithAllGuarantees() {
        // - Runs with en-US locale
        // - SystemConfig is reset
        // - Charset is validated
    }
}
```

### Method-Specific Configuration Reset

```java
class MixedTest {
    @Test
    void normalTest() {
        // No special reset
    }
    
    @Test
    @ResetSessionConfig
    void testThatModifiesSession() {
        SessionConfig.someProperty = "modified";
        // Only this test gets reset
    }
}
```

### Testing with Custom System Properties

```java
@ResetSystemProperty("my.feature.enabled")
@ResetSystemProperty("my.feature.timeout")
class FeatureTest {
    @TestConfig.java               - SystemConfig reset annotation
├── ResetSystemConfigExtension.java      - SystemConfig reset implementation
├── ResetSessionConfig.java              - SessionConfig reset annotation
├── ResetSessionConfigExtension.java     - SessionConfig reset implementation
├── ResetReporterConfig.java             - ReporterConfig reset annotation
├── ResetReporterConfigExtension.java    - ReporterConfig reset implementation
├── ResetMeterConfig.java                - MeterConfig reset annotation
├── ResetMeterConfigExtension.java       - MeterConfig reset implementation
├── ResetWatcherConfig.java              - WatcherConfig reset annotation
├── ResetWatcherConfigExtension.java     - WatcherConfig reset implementation
├── ResetSystemProperty.java             - System property reset annotation (repeatable)
├── ResetSystemPropertyExtension.java    - System property reset implementation
├── ClearParserErrors.java               - ConfigParser error clearing annotation
├── ClearConfigParserExtension.java      - ConfigParser error clearing implementation
├── ClearConfigParser.java               - Container for ClearParserErrors annotations
```

## Package Structure

```
org.usefultoys.test/
├── WithLocale.java                      - Locale control annotation
├── WithLocaleExtension.java             - Locale control implementation
├── ResetSystemConfig.java               - SystemConfig reset annotation
├── ResetSystemConfigExtension.java      - SystemConfig reset implementation
├── ResetSessionConfig.java              - SessionConfig reset annotation
├── ResetSessionConfigExtension.java     - SessionConfig reset implementation
├── ResetReporterConfig.java             - ReporterConfig reset annotation
├── ResetReporterConfigExtension.java    - ReporterConfig reset implementation
├── ResetMeterConfig.java                - MeterConfig reset annotation
├── ResetMeterConfigExtension.java       - MeterConfig reset implementation
├── ResetWatcherConfig.java              - WatcherConfig reset annotation
├── ResetWatcherConfigExtension.java     - WatcherConfig reset implementation
├── ResetSystemProperty.java             - System property reset annotation (repeatable)
├── ResetSystemPropertyExtension.java    - System property reset implementation
├── ClearParserErrors.java               - ConfigParser error clearing annotation
├── ClearConfigParserExtension.java      - ConfigParser error clearing implementation
├── ValidateCharset.java                 - Charset validation annotation
├── CharsetConsistencyExtension.java     - Charset validation implementation
├── ValidateCleanMeter.java              - Meter stack validation annotation
├── ValidateCleanMeterExtension.java     - Meter stack validation implementation
└── README.md                            - This documentation
```

## See Also

- [JUnit 5 Extension Model](https://junit.org/junit5/docs/current/user-guide/#extensions)
- [Copilot Instructions](../../../.github/copilot-instructions.md) - Test conventions
- [AI Instructions](../../../AI-INSTRUCTIONS.md) - Project guidelines
