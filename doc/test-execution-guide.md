# Test Execution Guide - SLF4J Toys

> This guide describes the two-tier, profile-based test architecture defined in
> [TDR-0031](TDR-0031-ide-friendly-build-with-optional-logback-testing.md). For the condensed,
> AI-oriented version of the same commands, see `.agents/skills/run-test/SKILL.md`.

## Overview of the Problem

This project uses **two separate Maven Surefire executions** to avoid SLF4J classpath conflicts:

- **SLF4J ServiceLoader can bind ONLY ONE logger** at runtime
- Having both `logback-classic` AND `slf4j-test-mock` on the same classpath causes failures
- **Solution:** Run tests in isolated classpaths, gated by a Maven profile rather than by execution flags

## Execution Architecture

### Execution 1: `default-test` (always active)
**Purpose:** Core tests that do NOT use Logback
- **Logger used:** MockLogger (via `slf4j-test-mock`)
- **Tests included:** All tests EXCEPT `**/logback/**/*Test.java`
- **Dependency removed from classpath:** `logback-classic`
- **Number of tests:** ~1441 tests
- **IDE support:** Full — this is the execution IDEs import and can run/debug/cover directly

### Execution 2: `logback-tests` (activated only by the `with-logback` profile)
**Purpose:** Tests that use the real Logback implementation
- **Logger used:** Logback (real implementation)
- **Tests included:** Only `**/logback/**/*Test.java`
- **Dependency removed from classpath:** `slf4j-test-mock`
- **Number of tests:** 84 tests
- **IDE support:** None — the `with-logback` profile also adds the `src/logback-main/java` and
  `src/logback-test/java` source roots via `build-helper-maven-plugin`, but only during a Maven
  build; IDEs never import them, so this execution is Maven-only

---

## Execution Commands

### 1. Run core tests (default, IDE-friendly)
```powershell
.\mvnw test
```
**Expected result:**
- Executes `default-test` only
- ~1441 tests ✅
- Final status: BUILD SUCCESS

### 2. Run everything, including Logback (Maven-only)
```powershell
.\mvnw test -P slf4j-2.0,with-logback
```
**Expected result:**
- Executes both `default-test` and `logback-tests`
- `default-test`: ~1441 tests ✅
- `logback-tests`: 84 tests ✅
- **Total: 1525 tests**
- Final status: BUILD SUCCESS

### 3. Run a specific test class or method
```powershell
.\mvnw test -Dtest=MeterLifeCycleTest
.\mvnw test '-Dtest=MeterLifeCycleTest#shouldCreateMeterWithLoggerInitialState'   # single quotes protect '#' in PowerShell
```
Runs against `default-test` (MockLogger). To target a Logback-only test class, add the profile:
```powershell
.\mvnw test -P slf4j-2.0,with-logback -Dtest=MessageHighlightConverterTest
```

### 4. Full clean rebuild
```powershell
.\mvnw clean test
```
Not needed for routine iteration — Maven's incremental compilation is reliable. Use `clean` when
you suspect stale build state, or for guaranteed-fresh CI/release-style validation. See
`.agents/skills/run-test/SKILL.md` for when this project's AI tooling reaches for `clean`.

---

## Configuration in pom.xml

### Execution 1: `default-test` (always active, in the base `<build>` section)
```xml
<execution>
    <id>default-test</id>
    <phase>test</phase>
    <goals><goal>test</goal></goals>
    <configuration>
        <skip>false</skip>
        <!-- Excludes Logback-specific tests -->
        <excludes>
            <exclude>**/logback/**/*Test.java</exclude>
        </excludes>
        <!-- Removes Logback from classpath -->
        <classpathDependencyExcludes>
            <classpathDependencyExclude>ch.qos.logback:logback-classic</classpathDependencyExclude>
        </classpathDependencyExcludes>
    </configuration>
</execution>
```

### Execution 2: `logback-tests` (only inside the `with-logback` profile)
```xml
<execution>
    <id>logback-tests</id>
    <phase>test</phase>
    <goals><goal>test</goal></goals>
    <configuration>
        <skip>false</skip>
        <!-- Includes ONLY Logback-specific tests -->
        <includes>
            <include>**/logback/**/*Test.java</include>
        </includes>
        <!-- Removes MockLogger from classpath -->
        <classpathDependencyExcludes>
            <classpathDependencyExclude>org.usefultoys:slf4j-test-mock</classpathDependencyExclude>
        </classpathDependencyExcludes>
    </configuration>
</execution>
```

The same `with-logback` profile also uses `build-helper-maven-plugin` to add `src/logback-main/java`
(production) and `src/logback-test/java` (tests) as source roots — see `pom.xml`'s `<profiles>`
section for the exact `add-source`/`add-test-source` executions.

---

## Quick Reference

| Command | Purpose | Tests | Executions run |
|---------|---------|-------|-----------------|
| `mvnw test` | Core tests, IDE-friendly | ~1441 | `default-test` |
| `mvnw test -P slf4j-2.0,with-logback` | Everything | 1525 | `default-test` + `logback-tests` |
| `mvnw test -Dtest=ClassName` | One class | varies | `default-test` |
| `mvnw test -P slf4j-2.0,with-logback -Dtest=ClassName` | One Logback class | varies | `logback-tests` |
| `mvnw clean test` | Core tests, full rebuild | ~1441 | `default-test` |

---

## Troubleshooting

### Error: "Cannot invoke 'org.slf4j.Logger.getName()' because 'logger' is null"
- **Cause:** classpath conflict — usually running a Logback-specific test without the
  `with-logback` profile active, or a non-Logback test that accidentally matched
  `**/logback/**/*Test.java`
- **Solution:** confirm which profile is active and that the test's package placement matches
  which execution it's meant to run under

### Tests run but some fail
- **Check:** which execution ran (look at the build log)
- **Expected:** `[INFO] --- surefire:3.5.6:test (default-test) @` or `(logback-tests) @`

---

## Build Profiles

In addition to the `with-logback` test-inclusion profile above, there are profiles for different
SLF4J/Servlet combinations:

- `slf4j-2.0` (default): SLF4J 2.0.x + Logback 1.5.x + Jakarta Servlet
- `slf4j-2.0-javax`: SLF4J 2.0.x + Logback 1.3.x + javax Servlet
- `slf4j-1.7-javax`: SLF4J 1.7.x + Logback 1.2.x + javax Servlet (backward compatibility)

Usage: `.\mvnw test -P slf4j-2.0-javax`

---

## Related Documentation

- [TDR-0031: IDE-Friendly Build with Optional Logback Testing](TDR-0031-ide-friendly-build-with-optional-logback-testing.md) — the design decision this guide documents
- [Build and Execution Guide](build-execution-guide.md) — build profiles and Maven configuration beyond testing
- `.agents/skills/run-test/SKILL.md` — the condensed version of this guide for AI assistants working in this repo

---

## Conclusion

✅ **Default recommendation:** Use `.\mvnw test` for everyday development (~1441 tests, IDE-friendly).
Reach for `.\mvnw test -P slf4j-2.0,with-logback` (1525 tests) only when the change touches Logback
converters or integration tests, or before a release-style full validation.
