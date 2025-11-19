# GitHub Copilot Instructions for slf4j-toys

<!-- 
This file contains specific instructions and context for GitHub Copilot 
to help it better understand this project and provide more accurate suggestions.
-->

## Project Overview

## Code Style Guidelines

### Language Requirements
- **All source code must be written in English**, including:
  - Variable names, method names, class names
  - Comments and documentation
  - String literals used in user-facing messages
  - Test descriptions and assertions
- **Source code files must be encoded in UTF-8**


### Java Version Compatibility
- **Code must be compatible with Java 8 or higher**
- Use modern Java features when appropriate (streams, method references, lambda expressions)
- Avoid features that require Java versions newer than 8 unless explicitly documented

### Lombok Usage
- **Prefer Lombok annotations over boilerplate code generation**
- Use `@Getter` and `@Setter` instead of manual getter/setter methods
- Use `@ToString` instead of manual toString() implementations
- Use `@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)` for consistent field encapsulation
- Use `@RequiredArgsConstructor` when appropriate
- Use `@Builder` for complex object creation patterns
- Use `@Value` for immutable data classes
- Avoid generating boilerplate code manually when Lombok can handle it

## Best Practices

## Common Patterns

## Testing Guidelines

### Test Structure and Naming
- **Use @DisplayName annotation** for all test methods with clear, descriptive names
- Test method names should be descriptive and follow the pattern `shouldDoSomethingWhenCondition`
- Group related tests using nested test classes with @Nested when appropriate
- Use Given-When-Then structure in test comments for clarity

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