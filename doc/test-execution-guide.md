# Test Execution Guide - SLF4J Toys

## Overview of the Problem

This project uses **two separate Maven Surefire executions** to avoid SLF4J classpath conflicts:

- **SLF4J ServiceLoader can bind ONLY ONE logger** at runtime
- Having both `logback-classic` AND `slf4j-test-mock` on the same classpath causes failures
- **Solution:** Run tests in isolated classpaths

## Execution Architecture

### Default (Disabled)
- The default Surefire execution is **disabled** to prevent conflicts
- Configuration: `<skip>true</skip>` in general configuration

### Execution 1: `unit-tests` (Unit Tests)
**Purpose:** Tests that do NOT use Logback
- **Logger used:** MockLogger (via `slf4j-test-mock`)
- **Tests included:** All tests EXCEPT `**/logback/**/*Test.java`
- **Dependency removed from classpath:** `logback-classic`
- **Number of tests:** ~1768 tests

### Execution 2: `logback-tests` (Logback Tests)
**Purpose:** Tests that use real Logback implementation
- **Logger used:** Logback (real implementation)
- **Tests included:** Only `**/logback/**/*Test.java`
- **Dependency removed from classpath:** `slf4j-test-mock`
- **Number of tests:** 84 tests

---

## Execution Commands

### 1. Run ALL Tests (Recommended)
```powershell
.\mvnw test
```
**Expected result:**
- Executes both executions
- `unit-tests`: ~1768 tests ✅
- `logback-tests`: 84 tests ✅
- **Total: ~1852 tests**
- Final status: BUILD SUCCESS

---

### 2. Run ONLY Tests WITHOUT Logback (unit-tests)
```powershell
.\mvnw test -Dskip.logback-tests=true
```
**Alternative with explicit goals:**
```powershell
.\mvnw surefire:test@unit-tests
```
**Expected result:**
- Executes ONLY `unit-tests`
- MockLogger on classpath
- Logback excluded
- ~1768 tests

---

### 3. Run ONLY Tests WITH Logback (logback-tests)
```powershell
.\mvnw surefire:test@logback-tests
```
**Expected result:**
- Executes ONLY tests in `**/logback/**/*`
- Logback on classpath
- MockLogger excluded
- 84 tests

---

### 4. Run Specific Tests (MeterLifeCycleTest)
⚠️ **IMPORTANT:** When using `-Dtest=TestName`, Maven may try to run in `logback-tests` and fail

**Correct solution:**
```powershell
# Runs in unit-tests execution specifically
.\mvnw surefire:test@unit-tests -Dtest=MeterLifeCycleTest
```

**Expected result:**
- MeterLifeCycleTest runs with MockLogger
- 140 tests
- Status: BUILD SUCCESS

---

### 5. Run with Full Clean
```powershell
.\mvnw clean test
```
**Expected result:**
- Removes `target/` directory
- Recompiles source code
- Executes both executions
- ~1852 tests

---

### 6. Check Specific Error
If you get error "Cannot invoke 'org.slf4j.Logger.getName()' because 'logger' is null":

**Cause:** Your test is running with incorrect classpath
- Tests without Logback specifics should not run in `logback-tests`
- Wrong execution = conflicting classpath

**Solution:**
```powershell
# Runs specifically in correct execution
.\mvnw surefire:test@unit-tests
```

---

## Configuration in pom.xml

### Execution 1: unit-tests
```xml
<execution>
    <id>unit-tests</id>
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

### Execution 2: logback-tests
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

---

## Quick Reference

| Command | Purpose | Tests | Execution |
|---------|---------|-------|-----------|
| `mvnw test` | All tests | ~1852 | unit-tests + logback-tests |
| `mvnw test -Dskip.logback-tests=true` | Without Logback | ~1768 | unit-tests only |
| `mvnw surefire:test@logback-tests` | Only Logback | 84 | logback-tests only |
| `mvnw surefire:test@unit-tests` | Without Logback (explicit) | ~1768 | unit-tests only |
| `mvnw surefire:test@unit-tests -Dtest=MeterLifeCycleTest` | Specific test | 140 | unit-tests only |
| `mvnw clean test` | Everything with rebuild | ~1852 | unit-tests + logback-tests |

---

## Troubleshooting

### Error: "Cannot invoke 'org.slf4j.Logger.getName()' because 'logger' is null"
- **Cause:** Classpath conflict
- **Solution:** Use `mvnw surefire:test@unit-tests` or `mvnw test` (without `-Dtest=TestName`)

### Error: "SLF4J ServiceLoader conflict"
- **Cause:** Both `logback-classic` and `slf4j-test-mock` on same classpath
- **Solution:** Do not modify pom.xml, use separate executions

### Tests run but some fail
- **Check:** Which execution is running (look at output)
- **Expected:** `[INFO] --- surefire:3.5.4:test (unit-tests) @` or `(logback-tests) @`

---

## Build Profiles

In addition to test executions, there are profiles for different SLF4J versions:

- `slf4j-2.0` (default): SLF4J 2.0.16 + Logback 1.5.23 + Jakarta Servlet
- `slf4j-2.0-javax`: SLF4J 2.0.16 + Logback 1.3.14 + javax Servlet
- `slf4j-1.7-javax`: SLF4J 1.7.36 + Logback 1.2.13 + javax Servlet (backward compatibility)

Usage: `.\mvnw -P slf4j-2.0-javax test`

---

## Related Documentation

For build profiles and Maven configurations, see [Build and Execution Guide](build-execution-guide.md).

---

## Conclusion

✅ **Main recommendation:** Use `.\mvnw test` to run everything
- Automatic
- Runs both executions with correct isolation
- No classpath confusion
- Final result: BUILD SUCCESS with ~1852 tests
