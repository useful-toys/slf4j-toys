---
description: 'Java testing standards and guidelines'
applyTo: '**/*Test.java'
---

# Java Testing Standards

This document contains all Java testing guidelines for the slf4j-toys project.

## Testing Standards

### Testing
- **All new features and bug fixes must include corresponding unit tests**
- **Coverage**: Target >95% code coverage. Cover all logical branches and conditionals
- **Dependencies**: Minimize new Maven dependencies. Only allowed:
  - Compile: `org.slf4j:slf4j-api`, `org.projectlombok:lombok`
  - Test: `org.junit.jupiter:*` (JUnit 5)
- **Build tools**: Use `maven-surefire-plugin` for testing and `jacoco-maven-plugin` for coverage

### Reflection & Testability
- **Avoid reflection-based access bypass in tests**: Do not use `AccessibleObject#setAccessible(true)` (or similar) to modify visibility of constructors, methods, or fields.
  - Prefer testing through public APIs, realistic integration points (e.g., JUnit extensions), or package-private seams.
  - Prefer isolation strategies that do not require reflective mutation (e.g., unique identifiers, test-local instances, deterministic inputs).
- **When coverage gaps are not realistically testable**, you may *suggest* production-code refactoring options to improve testability (e.g., dependency injection for `LoggerFactory`, small internal facades for reflection/static calls), but:
  - **Do not apply such production changes automatically** unless explicitly requested.
  - Ensure the proposal **does not introduce any dependency of production code on test design** (no test-only hooks, flags, or APIs intended solely for tests).
  - Keep behavior and public APIs unchanged unless the user explicitly asks for API changes.

### Test Structure & Organization
- Group tests semantically using JUnit 5's `@Nested` classes
- Create a test group for each method or feature of the class under test
- Use `@DisplayName` with descriptive names for all test classes and methods
- Test method names should be descriptive and follow the pattern `shouldDoSomethingWhenCondition`

### Test Assertions
- **All assertions must include a descriptive message** using "should" format (e.g., "should return non-null value", "should throw IllegalArgumentException")
- Prefer specific assertions over generic ones (e.g., assertEquals over assertTrue when comparing values)
- Include context in assertion messages to help debugging failures

### Test Cases
- Test both positive (success) and negative (expected failure) scenarios
- Cover all meaningful combinations of parameters, even if redundant for coverage purposes
- Prefer real-world scenarios when possible
- Lombok-generated functionality (e.g., builders, @NonNull validation) does not require explicit testing

### Test Class Javadoc Requirements
**All test classes must include a Javadoc that documents what the tests are covering.**

The test class Javadoc must include:
1. **Brief description**: What class/feature is being tested (use `{@link ClassName}` for the class under test)
2. **Coverage section**: Detailed list of what the tests validate and cover

**Format:**
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
 *   <li><b>Edge Case 1:</b> Description of what is validated</li>
 * </ul>
 */
class MyClassTest {
    // ... test methods
}
```

**Example:**
```java
/**
 * Unit tests for {@link Session}.
 * <p>
 * Tests validate that Session correctly generates and manages UUIDs,
 * with proper immutability and formatting validation.
 * <p>
 * <b>Coverage:</b>
 * <ul>
 *   <li><b>UUID Generation:</b> Verifies that Session generates a non-null UUID on first access</li>
 *   <li><b>UUID Immutability:</b> Ensures that the UUID remains constant across multiple accesses</li>
 *   <li><b>UUID Format:</b> Validates that generated UUIDs are 32-character hexadecimal strings</li>
 *   <li><b>Short UUID with Default Size:</b> Tests shortSessionUuid() method with default SessionConfig.uuidSize</li>
 *   <li><b>Short UUID with Custom Size:</b> Tests shortSessionUuid() method with custom SessionConfig.uuidSize configuration</li>
 * </ul>
 */
```

This makes it clear to future readers:
- What component is being tested
- What specific aspects/features are covered by the test suite
- What edge cases are handled
- How to add new tests to extend coverage
