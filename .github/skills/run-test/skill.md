---
name: run-test
description: 'Skill to run Maven tests in the slf4j-toys project.'
---

# Test Execution Strategy

**Two-tier testing strategy**: IDE-friendly default build + Maven-only Logback testing.

## Default Build - Core Tests (~1441 tests)

**IDE Support**: ✅ Full support (run, debug, coverage)

```powershell
# Run all core tests (Meter, Watcher, Reporter)
.\mvnw test

# Run specific test class
.\mvnw test -Dtest=MeterLifeCycleTest

# Run specific test method (requires quotes for # character)
.\mvnw test '-Dtest=MeterLifeCycleTest#shouldCreateMeterWithLoggerInitialState'
```

**What's tested**: Core library features with MockLogger  
**What's excluded**: Logback integration tests (`**/logback/**/*Test.java`)  
**Dependencies**: `slf4j-test-mock` (MockLogger)

## With-Logback Profile - Logback Tests (+84 tests)

**IDE Support**: ❌ Maven-only (IDE cannot run these tests)

```powershell
# Run ALL tests (core + logback)
.\mvnw test -P slf4j-2.0,with-logback

# Run only logback tests
.\mvnw test -P slf4j-2.0,with-logback -Dtest=MessageHighlightConverterTest

# Run specific logback test method (requires quotes for # character)
.\mvnw test -P slf4j-2.0,with-logback '-Dtest=MessageHighlightConverterTest#testMsgStartMarker'
```

**What's tested**: Logback converters and integration features  
**What's included**: Only `**/logback/**/*Test.java` tests  
**Dependencies**: `logback-classic` (real Logback Logger)  
**Source dirs added**: `src/logback-main/java`, `src/logback-test/java`

## Important Notes

- **No `clean` needed**: Maven handles incremental compilation; avoid `clean` unless necessary
- **Classpath isolation**: Each execution excludes the conflicting SLF4J binding to prevent ClassCastException
- **Profile required for Logback**: Must use `-P slf4j-2.0,with-logback` to activate Logback source directories and tests
- **Quote special characters**: Use single quotes for `-Dtest` parameters containing `#`

**See**: [TDR-0031](../../../doc/TDR-0031-ide-friendly-build-with-optional-logback-testing.md) for complete rationale.