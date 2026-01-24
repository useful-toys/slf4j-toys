---
description: 'Coding conventions and standards for Java test code in the slf4j-toys project.'
applyTo: '**/*Test.java'
---

# Java Testing Standards

## Coverage Requirements

- **Target**: >95% code coverage
- Create multiple scenarios covering all logical branches and conditionals
- Test both positive (success) and negative (expected failure) scenarios
- Cover all meaningful combinations of parameters, even if redundant for coverage purposes
- Prefer real-world scenarios when possible
- Lombok-generated functionality (e.g., `@NonNull` validation, `@Getter`, `@ToString`) does not require explicit testing

## Test Scenarios and Variations

**Prefer parameterized tests** when testing multiple variations of the same scenario:

```java
@ParameterizedTest
@DisplayName("should parse valid formats")
@CsvSource({
    "2026-01-23, 2026, 1, 23",
    "2025-12-31, 2025, 12, 31",
    "2024-02-29, 2024, 2, 29"  // leap year
})
void shouldParseValidFormats(String input, int year, int month, int day) {
    // Given: input date string
    // When: parsing the date
    LocalDate result = DateParser.parse(input);
    
    // Then: should match expected components
    assertEquals(year, result.getYear(), "should parse correct year");
    assertEquals(month, result.getMonthValue(), "should parse correct month");
    assertEquals(day, result.getDayOfMonth(), "should parse correct day");
}
```

**Benefits**:
- Single test method handles multiple scenarios
- Easier to add new test cases (just add data)
- Better test organization and maintainability
- Parallel execution of variations

**Other parameterization options**:
- `@ValueSource` for single parameter variations
- `@MethodSource` for complex object parameters
- `@EnumSource` for testing all enum values

## Dependencies and Tools

- **Minimize new Maven dependencies**. Only allowed:
  - **Compile**: `org.slf4j:slf4j-api`, `org.projectlombok:lombok`
  - **Test**: `org.junit.jupiter:*` (JUnit 5), `org.usefultoys:slf4j-test-mock`
- **Build tools**: 
  - `maven-surefire-plugin` for test execution
  - `jacoco-maven-plugin` for coverage analysis

## Test Structure and Organization

### Naming Conventions

- **Test class names**: `ClassNameTest` (e.g., `MeterTest`, `SessionTest`)
- **Test method names**: `shouldDoSomethingWhenCondition` pattern
- **Use `@DisplayName`**: Provide descriptive display names for all test classes and methods

### Nested Test Groups

**Use `@Nested` classes** to group tests semantically by feature or method:

```java
class MeterTest {
    
    @Nested
    @DisplayName("start() method")
    class StartMethod {
        
        @Test
        @DisplayName("should initialize start time when called first time")
        void shouldInitializeStartTimeWhenCalledFirstTime() {
            // test implementation
        }
        
        @Test
        @DisplayName("should throw exception when called twice")
        void shouldThrowExceptionWhenCalledTwice() {
            // test implementation
        }
    }
    
    @Nested
    @DisplayName("stop() method")
    class StopMethod {
        // stop-related tests
    }
}
```

### Given-When-Then Structure

**Use Given-When-Then comments** in all test methods for clarity:

```java
@Test
@DisplayName("should calculate duration when meter is stopped")
void shouldCalculateDurationWhenMeterIsStopped() {
    // Given: a started meter
    Meter meter = Meter.start("operation");
    
    // When: stopping the meter
    meter.stop();
    
    // Then: should have positive duration
    assertTrue(meter.getDuration() > 0, "should have positive duration");
}
```

## Test Assertions

- **All assertions must include a descriptive message** using "should" format
  - Examples: "should return non-null value", "should throw IllegalArgumentException", "should match expected result"
- **Prefer specific assertions** over generic ones:
  - ✅ `assertEquals(expected, actual, message)` 
  - ❌ `assertTrue(expected.equals(actual), message)`
- **Include context** in assertion messages to help debugging failures

## Reflection and Testability

- **Avoid `setAccessible(true)`**: Do not use reflection to bypass access modifiers in tests
  - Prefer testing through public APIs
  - Use package-private seams when appropriate
  - Use JUnit extensions for integration points
  
- **When coverage gaps exist**, you may *suggest* production refactoring to improve testability:
  - **Do NOT apply production changes automatically** unless explicitly requested
  - Ensure proposals **do not introduce test-only code** in production (no test hooks, flags, or APIs)
  - Keep behavior and public APIs unchanged unless user explicitly requests changes
  - Examples of acceptable suggestions: dependency injection, small internal facades

## Test Class Javadoc Requirements

**All test classes must include Javadoc** documenting what the tests cover.

**Required sections**:
1. **Brief description**: What class/feature is being tested (use `{@link ClassName}`)
2. **Coverage section**: Detailed list of validated features and edge cases

**Format**:
```java
/**
 * Unit tests for {@link ClassUnderTest}.
 * <p>
 * Tests validate that ClassUnderTest correctly [main functionality description].
 * <p>
 * <b>Coverage:</b>
 * <ul>
 *   <li><b>Feature 1:</b> Description of what is validated</li>
 *   <li><b>Feature 2:</b> Description of what is validated</li>
 *   <li><b>Edge Case 1:</b> Description of edge case handling</li>
 * </ul>
 */
class MyClassTest {
    // test methods
}
```

**Example**:
```java
/**
 * Unit tests for {@link Session}.
 * <p>
 * Tests validate that Session correctly generates and manages UUIDs,
 * with proper immutability and formatting validation.
 * <p>
 * <b>Coverage:</b>
 * <ul>
 *   <li><b>UUID Generation:</b> Verifies non-null UUID generation on first access</li>
 *   <li><b>UUID Immutability:</b> Ensures UUID remains constant across accesses</li>
 *   <li><b>UUID Format:</b> Validates 32-character hexadecimal format</li>
 *   <li><b>Short UUID Default:</b> Tests shortSessionUuid() with default config</li>
 *   <li><b>Short UUID Custom:</b> Tests shortSessionUuid() with custom config</li>
 * </ul>
 */
class SessionTest {
    // test methods
}
```
