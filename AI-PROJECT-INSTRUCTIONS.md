# Instructions for AI Assistants (Gemini & GitHub Copilot)

## Testing Standards

### Test Structure & Organization
- Test method names should be descriptive and follow the pattern `shouldDoSomethingWhenCondition`
- **Use Given-When-Then structure in test comments** for clarity and documentation:
  ```java
  @Test
  @DisplayName("should do something when condition is met")
  void shouldDoSomethingWhenConditionIsMet() {
      // Given: initial setup describing the test preconditions
      // When: the action being tested
      // Then: the expected outcome or assertion
      assertEquals(expected, actual, "should match expected value");
  }
  ```
  This structure makes tests self-documenting and easier to understand and maintain.

### Test Validation - Charset Consistency

**All test classes must declare `@ValidateCharset`** to ensure the test runs with the expected default charset. This annotation automatically validates that `SessionConfig.charset` matches `Charset.defaultCharset()`.

**Important**: When using `@ValidateCharset`, **do NOT include manual charset validation assertions** like:
```java
// ❌ WRONG: Redundant when @ValidateCharset is present
@ValidateCharset
class MyTest {
    @BeforeAll
    static void validateCharset() {
        assertEquals(Charset.defaultCharset().name(), SessionConfig.charset, 
            "Test requires SessionConfig.charset = default charset");
    }
}

// ✅ CORRECT: Just use the annotation
@ValidateCharset
class MyTest {
    // No manual charset validation needed - @ValidateCharset handles it
}
```

**When adjusting legacy tests**: Remove any manual charset validation methods or assertions that check `SessionConfig.charset == Charset.defaultCharset()` if `@ValidateCharset` is already present on the class.

### Configuration Reset in Tests
If a test uses a configuration class (e.g., `SessionConfig`), the test class must include the appropriate reset annotation:
- Use `@ResetSystemConfig` for tests modifying `SystemConfig`
- Use `@ResetSessionConfig` for tests modifying `SessionConfig`
- Use `@ResetReporterConfig` for tests modifying `ReporterConfig` (resets all three levels, including SystemConfig and SessionConfig)
- Use `@ResetMeterConfig` for tests modifying `MeterConfig` (resets all three levels, including SystemConfig and SessionConfig)
- Use `@ResetWatcherConfig` for tests modifying `WatcherConfig` (resets all three levels, including SystemConfig and SessionConfig)
- Use `@ClearParserErrors` for tests involving `ConfigParser` error handling

This eliminates the need for `@BeforeAll` and `@AfterAll` methods that reset the configuration. This applies to all configuration sources, including `SystemConfig`, `MeterConfig`, `WatcherConfig`, and `ReporterConfig`.

### System Property Reset in Tests
**If a test class uses `System.setProperty()` to set system properties that are NOT part of `SystemConfig`, `SessionConfig`, `ReporterConfig`, `MeterConfig`, or `WatcherConfig`, the test class must declare a `@ResetSystemProperty` annotation for each custom property being set.**

Note: If the test class already uses `@ResetSystemConfig`, `@ResetSessionConfig`, `@ResetReporterConfig`, `@ResetMeterConfig`, or `@ResetWatcherConfig`, then `@ResetSystemProperty` is NOT needed for properties belonging to those configuration classes, as the reset is already covered.

Example with custom property:
```java
@ValidateCharset
@ResetSessionConfig
@ResetSystemProperty("my.custom.property")  // Only needed for custom properties
class MyCustomPropertyTest {
    @Test
    @DisplayName("should handle custom property")
    void shouldHandleCustomProperty() {
        // Given: custom system property set
        System.setProperty("my.custom.property", "value");
        // When: code uses the property
        // Then: property is automatically cleared after test
        assertEquals("value", System.getProperty("my.custom.property"));
    }
}
```

Example WITHOUT @ResetSystemProperty (covered by @ResetSessionConfig):
```java
@ValidateCharset
@ResetSessionConfig  // This already covers PROP_PRINT_UUID_SIZE reset
class SessionConfigTest {
    @Test
    @DisplayName("should parse uuidSize property correctly")
    void shouldParseUuidSizePropertyCorrectly() {
        // Given: system property PROP_PRINT_UUID_SIZE set to "10"
        System.setProperty(SessionConfig.PROP_PRINT_UUID_SIZE, "10");  // Covered by @ResetSessionConfig
        // When: init() is called
        SessionConfig.init();
        // Then: uuidSize should reflect the system property value
        assertEquals(10, SessionConfig.uuidSize);
    }
}
```

### Charset Validation in Tests
**All test classes must declare the `@ValidateCharset` annotation** to ensure the JVM uses the expected charset.

### Locale-Sensitive Tests
**Test classes that perform string comparisons involving number formatting or date formatting must use `@WithLocale("en")`** to ensure consistent behavior across different environments and operating systems.
This is especially important for:
- Numbers with decimal places (e.g., "123.45" vs "123,45" in different locales)
- Date/time formatting (e.g., "12/31/2025" vs "31/12/2025")
- Currency formatting
- Sorting and collation of strings

Example:
```java
@ValidateCharset
@WithLocale("en")  // Ensures decimal points, date format, etc. are consistent
class NumberFormattingTest {
    @Test
    @DisplayName("should format decimal number with point separator")
    void shouldFormatDecimalNumberCorrectly() {
        // Given: a decimal number
        // When: formatted as string
        // Then: should use dot (.) not comma (,) as decimal separator
        assertEquals("123.45", String.format("%.2f", 123.45));
    }
}
```

### Replace Legacy extensions
-  If you find `@ExtendWith(FeaturedExtension.class)` in test code, replace it with `@Featured` for consistency and clarity.
- Example: If you find `@ExtendWith(CharsetConsistencyExtension.class)` in test code, replace it with `@ValidateCharset`.

### Test Assertions with MockLogger - Anti-Patterns

**Never make assertions directly on MockLogger attributes or methods.** This is considered an anti-pattern. Instead, **always use `AssertLogger` methods** to validate logging behavior.

**Why this matters:**
- Direct assertions on MockLogger are tightly coupled to implementation details
- `AssertLogger` provides semantic, intent-driven assertions that are more maintainable
- The code becomes more compact and readable
- Changes to MockLogger internals won't break assertions

**Anti-Pattern Examples (❌ DON'T DO THIS):**
```java
// ❌ WRONG: Direct assertions on MockLogger attributes/methods
assertEquals(1, mockLogger.getEventCount(), "should have logged one event");
assertEquals("Trace message", mockLogger.getEvent(0).getFormattedMessage(), "should log message");
assertEquals(MockLoggerEvent.Level.TRACE, mockLogger.getEvent(0).getLevel(), "should be trace level");
assertEquals(0, mockLogger.getEventCount(), "should not log any events");
```

**Recommended Patterns (✅ DO THIS):**
```java
// ✅ CORRECT: Use AssertLogger semantic assertions
// For validating logged event: level + message
AssertLogger.assertEvent(mockLogger, 0, MockLoggerEvent.Level.TRACE, "Trace message");

// For validating event count
AssertLogger.assertEventCount(mockLogger, 0);  // Asserts exactly 0 events
AssertLogger.assertEventCount(mockLogger, 1);  // Asserts exactly 1 event
```

**When analyzing legacy test code:** If you find direct assertions on MockLogger attributes (like `assertEquals(mockLogger.getEventCount(), ...)` or `mockLogger.getEvent(0).getFormattedMessage()`), **propose replacing them with `AssertLogger` equivalents**. This improves code quality and readability significantly.

**Benefits of this refactoring:**
- **More concise**: 3 separate assertions become 1 semantic call
- **More readable**: Intent is clear at a glance
- **More maintainable**: Changes to MockLogger don't require test updates
- **Better separation of concerns**: Tests focus on behavior, not implementation


### MockLogger for Log Output Validation

**Test classes that validate logger output must use `MockLogger` via the `@Slf4jMock` annotation and `MockLoggerExtension`.** This eliminates the need for manual MockLogger management.

1. **Use `@Slf4jMock` annotation** instead of manually instantiating `MockLogger`:
   ```java
   @Slf4jMock  // Omit the name - uses FQN of test class automatically
   private Logger logger;
   ```
    - **Preferred**: Omit the logger name to automatically use the fully qualified class name
    - **Reason**: Prevents accidental logger reuse between test classes
    - **Legacy tests only**: If adjusting legacy tests where the logger name is already established, use the existing logger name:
      ```java
      @Slf4jMock("org.usefultoys.slf4j.report.LegacyTest")  // Only if this name was already in use
      private Logger logger;
      ```

2. **Use `@WithMockLogger` annotation** on the test class:
    - Simplifies `@ExtendWith({MockLoggerExtension.class})`
    - The extension automatically:
        - Initializes the MockLogger instance
        - Sets `setEnabled(true)` before each test
        - Resets (clears events) before and after each test
    - **No manual `clearEvents()` calls needed**
    - **No manual `setEnabled(true)` calls needed**
    - **No manual @AfterEach, @AfterAll, @BeforeEach, @BeforeAll calls needed to handle MockLogger state**
    - If a logger-output test cannot rely on `MockLoggerExtension` (see legacy scenarios or special setup requirements), add an inline comment explaining why the extension is inapplicable and how the manual handling differs.

3. **Use `AssertLogger` for validations** instead of direct `MockLogger` assertions:
   ```java
   // ✅ CORRECT: Use AssertLogger methods
   AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.INFO,
       "expected substring 1",
       "expected substring 2");
   
   AssertLogger.assertEventNot(logger, 0, MockLoggerEvent.Level.INFO,
       "unexpected substring");
   
   // ❌ LEGACY (to be replaced):
   // mockLogger.assertEvent(0, MockLoggerEvent.Level.INFO, Markers.SOME_MARKER);
   // ✅ REPLACED WITH:
   // AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.INFO, "substring");
   ```
    - When adjusting legacy tests, **replace all `MockLogger.assert***` calls with `AssertLogger.assert***` equivalents**
    - Example migration:
      ```java
      // ❌ BEFORE (legacy):
      mockLogger.assertEvent(0, MockLoggerEvent.Level.INFO, Markers.MSG_WATCHER);
      
      // ✅ AFTER (new pattern):
      AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.INFO, "expected content");
      ```

**Example of correct test structure:**
```java
@ValidateCharset
@ResetReporterConfig
@WithLocale("en")
@WithMockLogger  // Enables MockLoggerExtension automatically
class MyLogValidationTest {
    
    @Slf4jMock  // Omit name - automatically uses org.usefultoys.slf4j.report.MyLogValidationTest
    private Logger logger;
    
    @Test
    @DisplayName("should log message with correct format")
    void shouldLogMessageWithCorrectFormat() {
        // Given: logger is initialized and enabled (automatic via @Slf4jMock and @WithMockLogger)
        // When: code is executed
        MyClass.doSomething();
        
        // Then: assertions using AssertLogger
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.INFO,
            "expected message part 1",
            "expected message part 2");
    }
}
```

