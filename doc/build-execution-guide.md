# Build and Execution Guide - SLF4J Toys

## Overview

This project uses Maven with multiple build profiles and configurations to support:
- Multiple SLF4J versions (1.7, 2.0)
- Multiple Servlet API versions (javax, jakarta)
- Java version compatibility (Java 8, Java 11+, Java 21)
- Test isolation with proper classpath management

---

## Build System Architecture

### Core Build Configuration
- **Maven Version:** 3.9.8
- **Java Compilation Target:** Java 8+ (source and target: 1.8)
- **Enforcer Rules:** Only on Java 21 (skipped for other versions)
- **Compiler:** Maven Compiler Plugin 3.14.1

### Key Plugins
- **Maven Surefire:** Test execution with classpath isolation
- **Maven Compiler:** Java 8 target compatibility
- **JaCoCo:** Code coverage analysis
- **Maven Javadoc:** Documentation generation
- **Maven GPG:** Artifact signing for releases
- **Maven Enforcer:** Build requirement validation

---

## Build Profiles (POM Profiles)

Profiles control which dependencies and code are included in builds. Use `mvnw -P profile-name` syntax.

### 1. Default Profile: `slf4j-2.0` (Jakarta)
**What it does:**
- SLF4J 2.0.16 + Logback 1.5.23 (latest)
- Jakarta Servlet API (modern)
- Java 11+ compatible
- **Active by default** - no `-P` flag needed

**Suitable for:**
- Modern applications using Jakarta EE
- Production builds
- Development on Java 11+

**Command:**
```powershell
.\mvnw clean package
# No profile flag needed - slf4j-2.0 is default
```

---

### 2. Legacy Profile: `slf4j-1.7-javax`
**What it does:**
- SLF4J 1.7.36 (legacy) + Logback 1.2.13 (legacy)
- javax Servlet API (legacy Java EE)
- Excludes Jakarta Servlet code from compilation and tests
- Java 8+ compatible

**Suitable for:**
- Testing backward compatibility
- Legacy applications still using SLF4J 1.7
- Ensuring library works with old stack

**When to use:**
- Validating support for legacy SLF4J versions
- Testing with old Java EE applications

**Command:**
```powershell
.\mvnw clean test -P slf4j-1.7-javax
```

**What gets excluded:**
- `**/ReportContextListener.java`
- `**/ReportServlet.java`
- `**/WatcherServlet.java`
- Corresponding test classes
- Jakarta Servlet from classpath

---

### 3. Compatibility Profile: `slf4j-2.0-javax`
**What it does:**
- SLF4J 2.0.16 (modern) + Logback 1.3.14 (compatible)
- javax Servlet API (legacy Java EE)
- Excludes Jakarta Servlet code from compilation and tests
- Java 8+ compatible

**Suitable for:**
- Transitioning applications (new SLF4J, old Servlet API)
- Supporting Java 8 with modern logging
- Backward compatibility with javax-based frameworks

**Command:**
```powershell
.\mvnw clean test -P slf4j-2.0-javax
```

**What gets excluded:**
- `**/ReportContextListener.java`
- `**/ReportServlet.java`
- `**/WatcherServlet.java`
- Corresponding test classes
- Jakarta Servlet from classpath

---

### 4. Java 8 Compatibility Profile: `jdk-8`
**What it does:**
- Uses Mockito 4.11.0 (Java 8 compatible, vs 5.x for Java 11+)
- Skips Maven Enforcer (enforcer requires Java 21)
- **Auto-activates** when running on Java 8

**Suitable for:**
- Testing backward compatibility on Java 8
- Validating Java 8 support

**When activated:**
- Automatically detected when JDK version is 1.8
- Can be explicitly activated: `-P jdk-8`

**Command:**
```powershell
# Auto-activated on Java 8
.\mvnw clean test

# Or explicitly
.\mvnw clean test -P jdk-8
```

---

### 5. Documentation Profile: `javadoc-validation`
**What it does:**
- Validates Javadoc syntax and completeness
- Fails build if Javadoc generation fails
- Source compatibility: Java 8 (for doclint compatibility)

**Suitable for:**
- Pre-release validation
- CI/CD quality gates
- Documentation integrity checks

**Command:**
```powershell
.\mvnw clean verify -P javadoc-validation
```

---

### 6. Release Profile: `release`
**What it does:**
- Generates source JAR (`-sources.jar`)
- Generates Javadoc JAR (`-javadoc.jar`)
- Signs artifacts with GPG
- Deploys to Maven Central via Central Publishing Maven Plugin

**Suitable for:**
- Creating production releases
- Publishing to Maven Central
- Official version releases

**Requires:**
- GPG key configured
- Maven Central credentials
- `~/.m2/settings.xml` with `maven-central` server entry

**Command:**
```powershell
.\mvnw -P release clean deploy
```

**Process:**
1. Compiles code (Java 8 target)
2. Runs tests (unit-tests execution)
3. Generates source JAR
4. Generates Javadoc JAR
5. Signs artifacts with GPG
6. Deploys to staging repository
7. Auto-publishes from staging to Maven Central

---

## Build Workflows by Scenario

### Scenario 1: Development (Default)
```powershell
.\mvnw clean test
```
- Uses `slf4j-2.0` profile (default)
- Runs unit-tests and logback-tests
- Total: ~1852 tests
- Target: Java 8
- Status: BUILD SUCCESS

---

### Scenario 2: Java 8 Compatibility Testing
```powershell
.\mvnw clean test -P jdk-8
```
- Auto-activates jdk-8 profile
- Uses Mockito 4.11.0 (Java 8 compatible)
- Skips Maven Enforcer
- Runs ~1852 tests
- Validates Java 8 support

---

### Scenario 3: SLF4J 1.7 Backward Compatibility
```powershell
.\mvnw clean test -P slf4j-1.7-javax
```
- Legacy SLF4J 1.7.36 + Logback 1.2.13
- javax Servlet API
- Excludes Jakarta Servlet code/tests
- Runs ~1852 tests
- Validates legacy compatibility

---

### Scenario 4: Modern Stack with Legacy Servlet API
```powershell
.\mvnw clean test -P slf4j-2.0-javax
```
- Modern SLF4J 2.0.16 + Logback 1.3.14
- javax Servlet API (bridge mode)
- Excludes Jakarta Servlet code/tests
- Runs ~1852 tests
- Useful for transitioning applications

---

### Scenario 5: Full Build with Documentation Validation
```powershell
.\mvnw clean verify -P javadoc-validation
```
- Default slf4j-2.0 profile
- Compiles code (Java 8 target)
- Runs all tests (~1852)
- Validates Javadoc syntax
- Builds JAR
- Generates code coverage report

---

### Scenario 6: Release to Maven Central
```powershell
.\mvnw -P release clean deploy
```
- Default slf4j-2.0 profile
- Compiles, tests, builds JAR
- Generates `-sources.jar`
- Generates `-javadoc.jar`
- Signs all artifacts with GPG
- Deploys to Maven Central staging
- Waits for auto-publish

---

### Scenario 7: Package Only (No Tests)
```powershell
.\mvnw clean package -DskipTests
```
- Compiles code
- Skips test execution
- Generates JAR
- Generates JaCoCo coverage file (but no coverage for tests)

---

### Scenario 8: Package Only + Documentation
```powershell
.\mvnw clean package -P javadoc-validation -DskipTests
```
- Compiles code (Java 8 target)
- Validates Javadoc
- Skips tests
- Generates JAR with Javadoc validation

---

## Combining Profiles

Multiple profiles can be combined with commas:

### Java 8 + SLF4J 1.7 Backward Compatibility
```powershell
.\mvnw clean test -P jdk-8,slf4j-1.7-javax
```
- Auto-activates jdk-8 (if on Java 8)
- Activates slf4j-1.7-javax explicitly
- Uses Mockito 4.11.0
- Uses SLF4J 1.7.36
- Tests with javax Servlet API

---

## Dependency Matrix by Profile

| Profile | SLF4J | Logback | Servlet API | Java Target | Mockito |
|---------|-------|---------|-------------|-------------|---------|
| `slf4j-2.0` (default) | 2.0.16 | 1.5.23 | Jakarta | Java 8 | 5.x (Java 11+ required) |
| `slf4j-2.0-javax` | 2.0.16 | 1.3.14 | javax | Java 8 | 5.x (Java 11+ required) |
| `slf4j-1.7-javax` | 1.7.36 | 1.2.13 | javax | Java 8 | 5.x (Java 11+ required) |
| `jdk-8` | Same as parent | Same as parent | Same as parent | Java 8 | 4.11.0 |
| `release` | Same as parent | Same as parent | Same as parent | Java 8 | Same as parent |
| `javadoc-validation` | Same as parent | Same as parent | Same as parent | Java 8 | Same as parent |

---

## Maven Enforcer Rules

**Default (Java 21):**
- Requires Maven 3.9.8
- Requires Java 21 (error if not in range [21, 22))

**Skipped on:**
- Java 8 (via jdk-8 profile)
- Other JDK versions via `<maven.enforcer.skip>false</maven.enforcer.skip>`

**To skip explicitly:**
```powershell
.\mvnw clean test -Dmaven.enforcer.skip=true
```

---

## Code Coverage Analysis

### Automatic Coverage Generation
```powershell
.\mvnw clean test
```
Creates: `target/jacoco.exec`

### Generate Coverage Report
```powershell
.\mvnw jacoco:report
# Report: target/site/jacoco/index.html
```

### Combined Build + Report
```powershell
.\mvnw clean test jacoco:report
```

---

## Release Process

### Step 1: Validate Javadoc
```powershell
.\mvnw clean verify -P javadoc-validation
```

### Step 2: Run Full Test Suite
```powershell
.\mvnw clean test
```

### Step 3: Create Release Artifacts
```powershell
.\mvnw -P release clean deploy
```

**What happens:**
1. Compiles (Java 8 target)
2. Runs unit-tests execution
3. Runs logback-tests execution
4. Creates JAR
5. Creates `-sources.jar`
6. Creates `-javadoc.jar`
7. Signs all 3 JARs with GPG
8. Deploys to Maven Central staging repository
9. Auto-publishes from staging to Maven Central

### Step 4: Verify on Maven Central
- Check: https://search.maven.org/ after 5-10 minutes
- Verify version appears in release repository

---

## Troubleshooting Build Issues

### Error: "Requires Java 21"
**Cause:** Enforcer rules require Java 21 on default profile

**Solution:**
```powershell
# Skip enforcer
.\mvnw clean test -Dmaven.enforcer.skip=true

# Or use jdk-8 profile on Java 8
.\mvnw clean test -P jdk-8

# Or check your JAVA_HOME
java -version
```

### Error: "Source option 8 is obsolete"
**Cause:** Compiling Java 8 target on Java 21

**Solution:** This is a warning, not an error. Safe to ignore.
```powershell
# Suppress warning
.\mvnw clean test -Dorg.slf4j.simpleLogger.defaultLogLevel=warn
```

### Error: "Mockito requires Java 11+"
**Cause:** Using default Mockito 5.x on Java 8

**Solution:**
```powershell
# Use jdk-8 profile
.\mvnw clean test -P jdk-8
```

### Error: "SLF4J ServiceLoader conflict"
**Cause:** Classpath has both logback-classic and slf4j-test-mock

**Solution:**
```powershell
# Use proper test execution (not -Dtest=ClassName directly)
.\mvnw surefire:test@unit-tests

# Or clean build
.\mvnw clean test
```

---

## Best Practices

### Development
```powershell
# Fastest: tests only, no docs validation
.\mvnw clean test
```

### Pre-Release
```powershell
# Full validation: docs + tests
.\mvnw clean verify -P javadoc-validation
```

### CI/CD
```powershell
# Complete validation with coverage
.\mvnw clean test
.\mvnw jacoco:report
```

### Release
```powershell
# Validation first, then release
.\mvnw clean verify -P javadoc-validation
.\mvnw -P release clean deploy
```

---

## Summary

| Task | Command |
|------|---------|
| Development | `.\mvnw clean test` |
| Test Java 8 | `.\mvnw clean test -P jdk-8` |
| Test SLF4J 1.7 | `.\mvnw clean test -P slf4j-1.7-javax` |
| Validate Docs | `.\mvnw clean verify -P javadoc-validation` |
| Build Release | `.\mvnw -P release clean deploy` |
| Package Only | `.\mvnw clean package -DskipTests` |
| Coverage Report | `.\mvnw clean test jacoco:report` |

---

## Related Documentation

- [Test Execution Guide](test-execution-guide.md) - Detailed test execution strategies
- [pom.xml](../pom.xml) - Complete Maven configuration
- [README.md](../README.md) - Project overview
