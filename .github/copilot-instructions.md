# GitHub Copilot Instructions

> **Note**: For general project instructions shared between all AI assistants (Gemini, GitHub Copilot, etc.), please refer to [AI-INSTRUCTIONS.md](../AI-INSTRUCTIONS.md) in the root directory.

## General Instructions

Please follow all guidelines specified in [AI-INSTRUCTIONS.md](../AI-INSTRUCTIONS.md), including:

## Copilot-Specific Notes

### Test Structure and Naming
- **Use @DisplayName annotation** for all test methods with clear, descriptive names
- Test method names should be descriptive and follow the pattern `shouldDoSomethingWhenCondition`
- Group related tests using nested test classes with @Nested when appropriate
- Use Given-When-Then structure in test comments for clarity

### Configuration Reset in Tests
If a test uses a configuration class (e.g., `SessionConfig`), the test class must include the `@ExtendWith(ResetConfig.class)` annotation (e.g., `@ExtendWith(ResetSessionConfig.class)`). This eliminates the need for `@BeforeAll` and `@AfterAll` methods that reset the configuration. This applies to all configuration sources, including `SystemConfig`, `MeterConfig`, `WatcherConfig`, and `ReporterConfig`.

### Locale-Sensitive Tests
Test classes that perform string comparisons involving decimal numbers or dates must be annotated with `@WithLocale("en")`. This ensures that tests run uniformly and predictably across different environments and operating systems by standardizing the locale.

### Assertion Best Practices
- **All assertions must include descriptive failure messages**
- **Assertion messages should use "should" format** (e.g., "should return non-null value", "should throw IllegalArgumentException")
- Use assertThat() with meaningful error messages that explain what was expected
- Prefer specific assertions over generic ones (e.g., assertEquals over assertTrue when comparing values)
- Include context in assertion messages to help debugging failures

## Documentation Standards

### Javadoc Requirements
- **All public methods should have Javadoc following best practices**
- **Do NOT document methods that implement third-party or well-documented interfaces**
- **Do NOT document overridden methods unless there are significant behavioral changes**
- Focus Javadoc on methods that provide business logic or custom functionality
- Include `@param`, `@return`, and `@throws` tags when appropriate
- Write clear, concise descriptions that explain the method's purpose and behavior
- Use proper Javadoc formatting with complete sentences ending in periods

### Example Javadoc Structure
```java
/**
 * Calculates the total price including taxes and discounts.
 * This method applies all registered discount rules before calculating
 * the final tax amount.
 * 
 * @param items the list of items to calculate total for
 * @param taxRate the tax rate to apply (must be between 0.0 and 1.0)
 * @return the total price including taxes and discounts
 * @throws IllegalArgumentException if taxRate is outside valid range
 * @throws NullPointerException if items is null
 */
public BigDecimal calculateTotal(List<Item> items, double taxRate) {
    // implementation
}
```