---
name: run-test
description: 'Run the slf4j-toys test suite with Maven. Use whenever you need to execute, verify, or debug tests in this repository — running the full suite, a single test class, or a single test method; checking whether a change broke anything before committing or pushing; or reproducing a failing test locally. Covers the two-tier build (core tests vs. the with-logback profile) and the exact mvnw invocations, including Java 21 and PowerShell argument-quoting requirements.'
---

# Running Tests in slf4j-toys

## Why this skill exists

The project splits tests into two tiers so that IDEs keep a clean, conflict-free classpath while CI still gets full coverage. Getting the tier wrong either skips tests silently or fails to compile. Always pick the tier deliberately using the table below rather than guessing a command.

## Guidelines

- Always invoke Maven through the wrapper (`.\mvnw`), never a system-installed `mvn` — it pins the Maven version the project expects.
- Default to plain `test`, not `clean test` — Maven's incremental compilation already picks up changed sources, so `clean` just costs a full recompile for no benefit. Only add `clean` if a run gives a suspicious result you suspect is stale build state; `doc/build-execution-guide.md` uses `clean` everywhere because it targets guaranteed-fresh CI/release builds, a different goal than this skill's iterative dev-loop runs.
- Java 21 must be on `PATH` / `JAVA_HOME` before running any `mvnw` command. See `powershell` skill for how to set up JDK 21 in the current terminal session.
- On PowerShell, wrap any `-D` parameter containing `#` (e.g. `-Dtest=Class#method`) in single quotes — PowerShell treats an unquoted `#` as a comment start and silently truncates the argument. See `powershell` skill for the full escaping rules.

## Choosing a tier

`slf4j-2.0` is `activeByDefault`, so plain `.\mvnw test` already runs under it; name it explicitly only alongside `with-logback`. Default to the **core tier** for everyday development. Reach for **with-logback** only when the change touches `src/logback-main/java`, `src/logback-test/java`, or anything under `**/logback/**` — those sources aren't on the default classpath at all, so core-tier tests can't exercise them.

| Tier | Command | Tests | IDE run/debug/coverage |
| --- | --- | --- | --- |
| Core (default) | `.\mvnw test` | ~1441 | Yes |
| With Logback | `.\mvnw test -P slf4j-2.0,with-logback` | +84 (1525 total) | No — Maven only |

```powershell
# Core tier
.\mvnw test
.\mvnw test -Dtest=MeterLifeCycleTest
.\mvnw test '-Dtest=MeterLifeCycleTest#shouldCreateMeterWithLoggerInitialState'   # single quotes protect '#'

# With-Logback tier
.\mvnw test -P slf4j-2.0,with-logback
.\mvnw test -P slf4j-2.0,with-logback -Dtest=MessageHighlightConverterTest
.\mvnw test -P slf4j-2.0,with-logback '-Dtest=MessageHighlightConverterTest#testMsgStartMarker'
```

The with-logback profile adds the `src/logback-main/java` and `src/logback-test/java` source roots and swaps `MockLogger` for real `logback-classic` — the only way Logback converters and integration tests get compiled and run at all, since the IDE never imports those roots.

## Related skills

- `powershell` — JDK 21 setup and PowerShell argument-quoting details referenced above.
- `trunk-based-development` — when to run which tier relative to commit/push/PR steps.
