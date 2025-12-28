# Test Utilities

This package provides JUnit 5 extensions and annotations to support test isolation and consistent test execution.

## Overview

All utilities are designed to ensure **test isolation** by resetting state between tests or controlling test environment conditions.

## Available Utilities

### Configuration Reset Extensions

These extensions reset configuration state before and after each test to prevent test interference.

#### `@ResetSystem` / `ResetSystemConfig`

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

#### `@ResetSession` / `ResetSessionConfig`

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

#### `@ResetReporter` / `ResetReporterConfig`

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

#### `ClearConfigParser`

Clears `ConfigParser` initialization errors before and after each test.

**Usage:**
```java
@ExtendWith(ClearConfigParser.class)
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

#### `CharsetConsistency`

Validates that the JVM's default charset matches `SessionConfig.charset` before running tests.

**Usage:**
```java
@ExtendWith(CharsetConsistency.class)
class CharsetSensitiveTest {
    @Test
    void testStringEncoding() {
        // Test runs only if charset is consistent
    }
}
```

## Choosing the Right Extension

### For Configuration Tests

- **Testing `SystemConfig` only** → Use `@ResetSystem`
- **Testing `SessionConfig` only** → Use `@ResetSession`
- **Testing `ReporterConfig`** → Use `@ResetReporter` (resets all levels)
- **Testing `ConfigParser` error handling** → Add `@ExtendWith(ClearConfigParser.class)`

### For Locale-Sensitive Tests

- **Number formatting, date formatting, string comparisons** → Use `@WithLocale("en-US")`
- Ensures consistent results across different OS environments

### For Charset-Sensitive Tests

- **File I/O, character encoding** → Add `@ExtendWith(CharsetConsistency.class)`
- Fails fast if charset mismatch detected

## Design Notes

### Why Both Annotations and Extension Classes?

- **Annotations** (e.g., `@ResetSystem`) provide a cleaner, more declarative API
- **Extension classes** (e.g., `ResetSystemConfig`) are used internally by annotations
- Both approaches are supported for flexibility

### Annotation vs. @ExtendWith

```java
// Clean, declarative (recommended)
@ResetSystem
class MyTest { }

// Verbose, but equivalent
@ExtendWith(ResetSystemConfig.class)
class MyTest { }
```

### Method-Level vs. Class-Level

All annotations can be used at both levels:

```java
@ResetSystem  // Applies to all tests in this class
class MyTest {
    
    @Test
    void test1() { }
    
    @Test
    @ResetSession  // Additional reset for this test only
    void test2() { }
}
```

## Best Practices

1. **Use the most specific reset needed** - Don't use `@ResetReporter` if you only need `@ResetSession`
2. **Combine multiple extensions** - You can use multiple annotations on the same test
3. **Prefer annotations** - Use `@ResetSystem` instead of `@ExtendWith(ResetSystemConfig.class)`
4. **Use `@WithLocale` for cross-platform tests** - Ensures consistent number/date formatting
5. **Document why you need the extension** - Add a comment explaining locale-sensitive operations

## Examples

### Testing with Multiple Extensions

```java
@WithLocale("en-US")
@ResetSystem
@ExtendWith(CharsetConsistency.class)
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
    @ResetSession
    void testThatModifiesSession() {
        SessionConfig.someProperty = "modified";
        // Only this test gets reset
    }
}
```

## Package Structure

```
org.usefultoys.test/
├── WithLocale.java              - Locale control annotation
├── WithLocaleExtension.java     - Locale control implementation
├── ResetSystem.java             - SystemConfig reset annotation
├── ResetSystemConfig.java       - SystemConfig reset implementation
├── ResetSession.java            - SessionConfig reset annotation
├── ResetSessionConfig.java      - SessionConfig reset implementation
├── ResetReporter.java           - ReporterConfig reset annotation
├── ResetReporterConfig.java     - ReporterConfig reset implementation
├── ClearConfigParser.java       - ConfigParser error clearing
├── CharsetConsistency.java      - Charset validation
└── CallerStackTraceThrowableTest.java - Stack trace test utilities
```

## See Also

- [JUnit 5 Extension Model](https://junit.org/junit5/docs/current/user-guide/#extensions)
- [Copilot Instructions](../../../.github/copilot-instructions.md) - Test conventions
- [AI Instructions](../../../AI-INSTRUCTIONS.md) - Project guidelines

