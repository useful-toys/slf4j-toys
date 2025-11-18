# JUL Test Mock

A comprehensive mock implementation of the Java Util Logging (JUL) framework designed specifically for unit testing. This library provides a mock Handler that captures log records in memory, allowing developers to inspect and assert log events during test execution.

## Features

- **Complete JUL Handler Implementation**: Full mock implementation of Handler that captures all log records
- **Event Capturing**: All log records are captured in memory for test verification
- **Assertion Utilities**: Rich API for asserting log records with descriptive error messages
- **Level Control**: Fine-grained control over which log levels are captured during tests
- **Thread-Safe**: Safe for use in single-threaded test environments
- **Java 8+ Compatible**: Works with Java 8 and higher versions
- **Zero Dependencies**: Only requires JUL (built into Java) and JUnit 5 for assertions

## Maven Dependency

```xml
<dependency>
    <groupId>org.usefultoys</groupId>
    <artifactId>jul-test-mock</artifactId>
    <version>1.9.0</version>
    <scope>test</scope>
</dependency>
```

## Quick Start

### Recommended Usage with AssertHandler

The `AssertHandler` class provides convenient static methods for asserting log records - this is the recommended way to use this library:

```java
import java.util.logging.Logger;
import java.util.logging.Level;
import org.usefultoys.jul.mock.MockHandler;
import static org.usefultoys.jul.mock.AssertHandler.*;

class MyTest {
    private Logger logger;
    private MockHandler handler;
    
    @BeforeEach
    void setUp() {
        logger = Logger.getLogger("test.logger");
        logger.setUseParentHandlers(false);
        handler = new MockHandler();
        logger.addHandler(handler);
        logger.setLevel(Level.ALL);
        handler.clearRecords();
    }
    
    @Test
    void testWithAssertHandler() {
        logger.info("Processing user: alice");
        logger.severe("Failed to process user: bob");
        
        // Use AssertHandler for cleaner assertions
        assertRecord(handler, 0, Level.INFO, "Processing user");
        assertRecord(handler, 1, Level.SEVERE, "Failed to process");
    }
}
```

### Alternative: Direct MockHandler Usage

You can also work directly with `MockHandler` for more detailed inspection:

```java
import java.util.logging.Logger;
import java.util.logging.Level;
import org.usefultoys.jul.mock.MockHandler;
import org.usefultoys.jul.mock.MockLogRecord;

@Test
void testLogging() {
    // Setup logger with MockHandler
    Logger logger = Logger.getLogger("test.logger");
    logger.setUseParentHandlers(false);
    MockHandler handler = new MockHandler();
    logger.addHandler(handler);
    logger.setLevel(Level.ALL);
    handler.clearRecords();
    
    // Log some messages
    logger.log(Level.INFO, "User {0} logged in", "john");
    logger.log(Level.WARNING, "Low disk space: {0} GB remaining", 2.5);
    
    // Assert log records
    assertEquals(2, handler.getRecordCount());
    assertEquals("User john logged in", handler.getRecord(0).getFormattedMessage());
    assertEquals(Level.INFO, handler.getRecord(0).getLevel());
}
```

## Core Classes

### MockHandler

The main mock handler implementation that captures log records in memory.

**Key Methods:**
- `getRecordCount()` - Returns the number of captured records
- `getRecord(int index)` - Returns a specific record by index
- `getLogRecords()` - Returns all records as an unmodifiable list
- `clearRecords()` - Clears all captured records
- `setLevel(Level)` - Sets the minimum level to capture
- `setStdoutEnabled(boolean)` - Enable/disable printing to stdout
- `setStderrEnabled(boolean)` - Enable/disable printing to stderr
- `toText()` - Returns all messages as a formatted string

### MockLogRecord

Immutable representation of a single log record.

**Key Properties:**
- `getLevel()` - The log level (FINEST, FINER, FINE, CONFIG, INFO, WARNING, SEVERE)
- `getFormattedMessage()` - The formatted message with parameters resolved
- `getMessage()` - The original message template
- `getParameters()` - The message parameters
- `getThrown()` - The associated throwable (if any)
- `getLoggerName()` - The name of the logger that created this record
- `getSourceClassName()` - The class name where logging occurred
- `getSourceMethodName()` - The method name where logging occurred
- `getMillis()` - The timestamp in milliseconds
- `getSequenceNumber()` - The sequence number of the record

### AssertHandler

Utility class providing static assertion methods for testing log records.

**Index-Based Record Assertions:**
- `assertRecord(MockHandler, int, String)` - Assert message contains text
- `assertRecord(MockHandler, int, Level, String)` - Assert level and message
- `assertRecord(MockHandler, int, Level, String...)` - Assert level with multiple message parts

**Existence-Based Record Assertions:**
- `assertHasRecord(MockHandler, String)` - Assert any record contains message part
- `assertHasRecord(MockHandler, Level, String)` - Assert any record has level and message
- `assertHasRecord(MockHandler, Level, String...)` - Assert any record has level and all message parts

**Throwable Assertions:**
- `assertRecordWithThrowable(MockHandler, int, Class)` - Assert record has specific throwable type
- `assertRecordWithThrowable(MockHandler, int, Class, String)` - Assert record has throwable type and message
- `assertRecordHasThrowable(MockHandler, int)` - Assert record has any throwable
- `assertHasRecordWithThrowable(MockHandler, Class)` - Assert any record has specific throwable type
- `assertHasRecordWithThrowable(MockHandler, Class, String)` - Assert any record has throwable type and message
- `assertHasRecordWithThrowable(MockHandler)` - Assert any record has any throwable

**Record Counting Assertions:**
- `assertRecordCount(MockHandler, int)` - Assert total number of records
- `assertNoRecords(MockHandler)` - Assert no records were logged
- `assertRecordCountByLevel(MockHandler, Level, int)` - Assert count of records by level
- `assertRecordCountByMessage(MockHandler, String, int)` - Assert count of records containing message part

**Record Sequence Assertions:**
- `assertRecordSequence(MockHandler, Level...)` - Assert exact sequence of log levels
- `assertRecordSequence(MockHandler, String...)` - Assert exact sequence of message parts

## Advanced Usage

### Controlling Log Levels

```java
@Test
void testLogLevelControl() {
    Logger logger = Logger.getLogger("test");
    logger.setUseParentHandlers(false);
    MockHandler handler = new MockHandler();
    logger.addHandler(handler);
    handler.clearRecords();
    
    // Only capture WARNING and above
    handler.setLevel(Level.WARNING);
    logger.setLevel(Level.ALL);
    
    logger.fine("This won't be captured");
    logger.warning("This will be captured");
    
    assertEquals(1, handler.getRecordCount());
    assertRecord(handler, 0, Level.WARNING, "This will be captured");
}
```

### Testing Exception Logging

```java
@Test
void testExceptionLogging() {
    Logger logger = Logger.getLogger("test");
    logger.setUseParentHandlers(false);
    MockHandler handler = new MockHandler();
    logger.addHandler(handler);
    handler.clearRecords();
    
    Exception ex = new RuntimeException("Test exception");
    logger.log(Level.SEVERE, "Operation failed", ex);
    
    MockLogRecord record = handler.getRecord(0);
    assertEquals(Level.SEVERE, record.getLevel());
    assertEquals("Operation failed", record.getFormattedMessage());
    assertSame(ex, record.getThrown());
}
```

### Clearing Records Between Tests

```java
@Test
void testRecordClearing() {
    Logger logger = Logger.getLogger("test");
    logger.setUseParentHandlers(false);
    MockHandler handler = new MockHandler();
    logger.addHandler(handler);
    handler.clearRecords();
    
    logger.info("First message");
    assertEquals(1, handler.getRecordCount());
    
    // Clear records for next test phase
    handler.clearRecords();
    assertEquals(0, handler.getRecordCount());
    
    logger.info("Second message");
    assertEquals(1, handler.getRecordCount());
}
```

### Inspecting All Log Output

```java
@Test
void testCompleteLogOutput() {
    Logger logger = Logger.getLogger("test");
    logger.setUseParentHandlers(false);
    MockHandler handler = new MockHandler();
    logger.addHandler(handler);
    handler.clearRecords();
    
    logger.info("Starting process");
    logger.fine("Processing step 1");
    logger.fine("Processing step 2");
    logger.info("Process completed");
    
    // Get all messages as formatted text
    String allMessages = handler.toText();
    assertTrue(allMessages.contains("Starting process"));
    assertTrue(allMessages.contains("Process completed"));
    
    // Or inspect individual records
    List<MockLogRecord> records = handler.getLogRecords();
    assertEquals(4, records.size());
}
```

### Using Existence-Based Assertions

```java
@Test
void testExistenceAssertions() {
    Logger logger = Logger.getLogger("test");
    logger.setUseParentHandlers(false);
    MockHandler handler = new MockHandler();
    logger.addHandler(handler);
    handler.clearRecords();
    
    logger.info("User john logged in");
    logger.warning("Invalid password attempt");
    logger.severe("Database connection failed");
    
    // Check if any record contains specific text (order doesn't matter)
    assertHasRecord(handler, "john");
    assertHasRecord(handler, Level.SEVERE, "Database");
    assertHasRecord(handler, Level.WARNING, "password");
}
```

### Testing Exception Handling

```java
@Test
void testExceptionAssertions() {
    Logger logger = Logger.getLogger("test");
    logger.setUseParentHandlers(false);
    MockHandler handler = new MockHandler();
    logger.addHandler(handler);
    handler.clearRecords();
    
    logger.log(Level.SEVERE, "Database error", new SQLException("Connection timeout"));
    logger.log(Level.WARNING, "Network issue", new IOException("Connection refused"));
    
    // Test specific throwable types
    assertRecordWithThrowable(handler, 0, SQLException.class);
    assertRecordWithThrowable(handler, 1, IOException.class, "refused");
    
    // Test existence of any throwable
    assertHasRecordWithThrowable(handler, SQLException.class);
    assertHasRecordWithThrowable(handler); // Any throwable
}
```

### Counting Records

```java
@Test
void testRecordCounting() {
    Logger logger = Logger.getLogger("test");
    logger.setUseParentHandlers(false);
    MockHandler handler = new MockHandler();
    logger.addHandler(handler);
    handler.clearRecords();
    
    logger.info("Application started");
    logger.warning("Authentication failed");
    logger.info("Processing request");
    logger.severe("Critical error occurred");
    logger.warning("Unauthorized access");
    
    // Count total records
    assertRecordCount(handler, 5);
    
    // Count by level
    assertRecordCountByLevel(handler, Level.INFO, 2);
    assertRecordCountByLevel(handler, Level.WARNING, 2);
    assertRecordCountByLevel(handler, Level.SEVERE, 1);
    
    // Count by message content
    assertRecordCountByMessage(handler, "error", 1); // Case sensitive
}
```

### Validating Record Sequences

```java
@Test
void testRecordSequences() {
    Logger logger = Logger.getLogger("test");
    logger.setUseParentHandlers(false);
    MockHandler handler = new MockHandler();
    logger.addHandler(handler);
    handler.clearRecords();
    
    logger.info("Process starting");
    logger.fine("Step 1 completed");
    logger.fine("Step 2 completed");
    logger.warning("Warning occurred");
    logger.info("Process finished");
    
    // Verify exact sequence of levels
    assertRecordSequence(handler, 
        Level.INFO, Level.FINE, Level.FINE, Level.WARNING, Level.INFO);
    
    // Verify sequence of message parts
    assertRecordSequence(handler, 
        "starting", "Step 1", "Step 2", "Warning", "finished");
}
```

### Working with Parameterized Messages

```java
@Test
void testParameterizedMessages() {
    Logger logger = Logger.getLogger("test");
    logger.setUseParentHandlers(false);
    MockHandler handler = new MockHandler();
    logger.addHandler(handler);
    handler.clearRecords();
    
    logger.log(Level.INFO, "User {0} accessed {1} at {2}", 
        new Object[]{"alice", "/admin", "10:30 AM"});
    
    MockLogRecord record = handler.getRecord(0);
    assertEquals("User alice accessed /admin at 10:30 AM", 
        record.getFormattedMessage());
    
    // Can also check raw message and parameters
    assertEquals("User {0} accessed {1} at {2}", record.getMessage());
    assertArrayEquals(new Object[]{"alice", "/admin", "10:30 AM"}, 
        record.getParameters());
}
```

## Integration with Test Frameworks

### JUnit 5

```java
@ExtendWith(MockitoExtension.class)
class MyServiceTest {
    
    private MockHandler handler;
    private Logger logger;
    
    @BeforeEach
    void setUp() {
        logger = Logger.getLogger(MyService.class.getName());
        logger.setUseParentHandlers(false);
        handler = new MockHandler();
        logger.addHandler(handler);
        logger.setLevel(Level.ALL);
        handler.clearRecords(); // Start each test clean
    }
    
    @AfterEach
    void tearDown() {
        logger.removeHandler(handler);
    }
    
    @Test
    void testServiceOperation() {
        MyService service = new MyService();
        service.performOperation();
        
        assertRecord(handler, 0, Level.INFO, "Operation started");
        assertRecord(handler, 1, Level.INFO, "Operation completed");
    }
}
```

### Testing Configuration

Since this is a test-scoped dependency, ensure your test classpath includes the mock implementation:

1. Add the dependency to your `pom.xml` with `<scope>test</scope>`
2. Configure your loggers in test setup to use the MockHandler
3. Your production code will use the real JUL handlers

## Best Practices

### 1. Use AssertHandler for Cleaner Tests

```java
// Good - Uses AssertHandler for cleaner code
assertRecord(handler, 0, Level.INFO, "User logged in");

// Less preferred - Manual casting and assertion
MockLogRecord record = handler.getRecord(0);
assertEquals(Level.INFO, record.getLevel());
assertTrue(record.getFormattedMessage().contains("User logged in"));
```

### 2. Clear Records Between Test Methods

```java
@BeforeEach
void setUp() {
    handler = new MockHandler();
    logger.addHandler(handler);
    handler.clearRecords();
}
```

### 3. Disable Parent Handlers

```java
// Prevent logs from propagating to parent handlers
logger.setUseParentHandlers(false);
```

### 4. Test Different Log Levels

```java
@Test
void testDifferentLogLevels() {
    logger.finest("Finest message");
    logger.finer("Finer message");
    logger.fine("Fine message");
    logger.config("Config message");
    logger.info("Info message");
    logger.warning("Warning message");
    logger.severe("Severe message");
    
    assertEquals(7, handler.getRecordCount());
    assertRecord(handler, 0, Level.FINEST, "Finest");
    assertRecord(handler, 6, Level.SEVERE, "Severe");
}
```

### 5. Choose the Right Assertion Type

**Use index-based assertions** when order matters:
```java
// When testing specific sequence of records
assertRecord(handler, 0, Level.INFO, "Starting");
assertRecord(handler, 1, Level.FINE, "Processing");
assertRecord(handler, 2, Level.INFO, "Completed");
```

**Use existence-based assertions** when order doesn't matter:
```java
// When testing that records occurred, regardless of order
assertHasRecord(handler, Level.SEVERE, "Database connection failed");
assertHasRecord(handler, "user authentication");
```

**Use counting assertions** for volume verification:
```java
// When testing logging frequency or filtering
assertRecordCount(handler, 5);
assertRecordCountByLevel(handler, Level.SEVERE, 0); // No errors expected
assertNoRecords(handler); // Nothing should be logged
```

**Use sequence assertions** for workflow validation:
```java
// When testing state machine transitions or process flows
assertRecordSequence(handler, Level.INFO, Level.FINE, Level.WARNING, Level.INFO);
```

### 6. Test Exception Logging Properly

```java
@Test
void testExceptionHandling() {
    // Test both the message and the exception
    logger.log(Level.SEVERE, "Operation failed", 
        new SQLException("Connection timeout"));
    
    assertRecord(handler, 0, Level.SEVERE, "Operation failed");
    assertRecordWithThrowable(handler, 0, SQLException.class, "timeout");
}
```

### 7. Validate Record Counts for Performance

```java
@Test
void testMinimalLogging() {
    // Ensure production code doesn't log excessively
    performOperation();
    
    assertRecordCountByLevel(handler, Level.FINE, 0); // No debug in production
    assertRecordCountByLevel(handler, Level.INFO, 1);  // Single info message expected
}
```

## Differences from SLF4J Test Mock

While `jul-test-mock` is inspired by `slf4j-test-mock`, there are some key differences due to the nature of JUL:

1. **Handler-based instead of Logger-based**: JUL uses Handlers to process log records, so you attach a MockHandler to your logger
2. **No Marker support**: JUL doesn't have a Marker concept like SLF4J
3. **No MDC support**: JUL doesn't have built-in MDC (though you can use ThreadContext or similar patterns)
4. **Different log levels**: JUL uses FINEST, FINER, FINE, CONFIG, INFO, WARNING, SEVERE instead of TRACE, DEBUG, INFO, WARN, ERROR
5. **Parameterized messages use MessageFormat**: JUL uses `{0}`, `{1}` style instead of `{}` placeholders

## Thread Safety

This mock implementation is designed for single-threaded test environments. If you need to test multi-threaded logging scenarios, ensure proper synchronization in your test code.
You may run tests in parallel as long as each test uses its own logger and handler instances.

## Requirements

- Java 8 or higher
- JUnit 5 (for assertion utilities)
- Lombok (build-time dependency for code generation)

## License

Licensed under the Apache License, Version 2.0. See the [LICENSE](../LICENSE) file for details.

## Contributing

This library follows the project's [GitHub Copilot Instructions](../.github/copilot-instructions.md) for code style and development practices.
