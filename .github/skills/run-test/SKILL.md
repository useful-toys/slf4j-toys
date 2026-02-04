---
name: run-test
description: 'Instructions to run tests in the slf4j-toys project.'
---

# Test Execution Strategy

## Guidelines

 - Use maven wrapper.
 - Avoid using `clean` goal.
 - Remember that running tests requires Java 21.

## Default Build - Core Tests (~1441 tests)

* Tests core functionality (Meter, Watcher, Reporter), excluding Logback integration tests
* Suporrts IDEs (run, debug, coverage)
* Includes core library features with MockLogger  
* Excludes Logback integration tests (`**/logback/**/*Test.java`)  
* Dependencies: `slf4j-test-mock` (MockLogger)

```powershell
# Run all core tests (Meter, Watcher, Reporter)
.\mvnw test

# Run specific test class
.\mvnw test -Dtest=MeterLifeCycleTest

# Run specific test method (requires quotes for # character)
.\mvnw test '-Dtest=MeterLifeCycleTest#shouldCreateMeterWithLoggerInitialState'
```

## With-Logback Profile - Logback Tests (+84 tests)

* Not susupported by IDEs (run, debug, coverage)
* Tests all features including Logback integration
* Requires Maven profile `with-logback` to activate Logback source dirs and tests
* Includes all tests 
* Dependencies: `logback-classic` (real Logback Logger) and `slf4j-test-mock` (MockLogger)
* Adds source main and test directores `src/logback-main/java`, `src/logback-test/java`

```powershell
# Run ALL tests (core + logback)
.\mvnw test -P slf4j-2.0,with-logback

# Run only logback tests
.\mvnw test -P slf4j-2.0,with-logback -Dtest=MessageHighlightConverterTest

# Run specific logback test method (requires quotes for # character)
.\mvnw test -P slf4j-2.0,with-logback '-Dtest=MessageHighlightConverterTest#testMsgStartMarker'
```