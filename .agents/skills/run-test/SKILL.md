---
name: run-test
description: 'Run the slf4j-toys test suite with Maven. Use whenever you need to execute, verify, or debug tests in this repository — running the full suite, a single test class, or a single test method; checking whether a change broke anything before committing or pushing; or reproducing a failing test locally. Covers the two-tier build (core tests vs. the with-logback profile) and the exact mvnw invocations, including Java 21 and PowerShell argument-quoting requirements.'
---

# Running Tests in slf4j-toys

## Why this skill exists

The project splits tests into two tiers so that IDEs keep a clean, conflict-free classpath while CI still gets full coverage. Getting the tier wrong either skips tests silently or fails to compile. Always pick the tier deliberately using the table below rather than guessing a command.

## Guidelines

- Always invoke Maven through the wrapper (`.\mvnw`), never a system-installed `mvn` — it pins the Maven version the project expects.
- Never add the `clean` goal (`.\mvnw clean test`). It forces a full recompile and defeats incremental builds; plain `test` is sufficient because Maven already recompiles changed sources.
- Java 21 must be on `PATH` / `JAVA_HOME` before running any `mvnw` command. See `powershell` skill for how to set up JDK 21 in the current terminal session.
- On PowerShell, wrap any `-D` parameter containing `#` (e.g. `-Dtest=Class#method`) in single quotes — PowerShell treats an unquoted `#` as a comment start and silently truncates the argument. See `powershell` skill for the full escaping rules.

## Choosing a tier

| Tier | Command prefix | Test count | IDE support (run/debug/coverage) |
| --- | --- | --- | --- |
| Core (default) | `.\mvnw test` | ~1441 | Yes |
| With Logback | `.\mvnw test -P slf4j-2.0,with-logback` | +84 (1525 total) | No — Maven only |

`slf4j-2.0` is `activeByDefault`, so plain `.\mvnw test` already runs under it; naming it explicitly is only needed alongside `with-logback`.

Default to the **core tier** for everyday development — it is what the IDE uses and covers Meter, Watcher, and Reporter with `MockLogger`. Reach for the **with-logback tier** only when the change touches `src/logback-main/java`, `src/logback-test/java`, or anything under `**/logback/**`, since those sources and tests aren't on the default classpath at all.

## Core tier — default build

Tests Meter, Watcher, Reporter, and supporting classes against `MockLogger` (the `slf4j-test-mock` dependency), excluding all Logback integration tests (`**/logback/**/*Test.java`). This is what IDEs import, so run/debug/coverage all work normally here.

```powershell
# Run all core tests
.\mvnw test

# Run one test class
.\mvnw test -Dtest=MeterLifeCycleTest

# Run one test method (single quotes protect the '#')
.\mvnw test '-Dtest=MeterLifeCycleTest#shouldCreateMeterWithLoggerInitialState'
```

## With-Logback tier — full coverage

Activates the `with-logback` Maven profile, which adds the `src/logback-main/java` and `src/logback-test/java` source roots and swaps `MockLogger` for the real `logback-classic` logger. This is how Logback converters and integration tests get compiled and run at all — the IDE never sees these sources, so this tier is Maven-only (no run/debug from the IDE).

```powershell
# Run everything: core + Logback (1525 tests)
.\mvnw test -P slf4j-2.0,with-logback

# Run only the Logback tests
.\mvnw test -P slf4j-2.0,with-logback -Dtest=MessageHighlightConverterTest

# Run one Logback test method (single quotes protect the '#')
.\mvnw test -P slf4j-2.0,with-logback '-Dtest=MessageHighlightConverterTest#testMsgStartMarker'
```

## Related skills

- `powershell` — JDK 21 setup and PowerShell argument-quoting details referenced above.
- `trunk-based-development` — when to run which tier relative to commit/push/PR steps.
