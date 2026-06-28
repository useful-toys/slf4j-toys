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

**Used by:** GitHub Actions release workflow (`release-deploy-version.yml`).
Releases are automated via CI — see [Release Process](#release-process) below.

**Process (automated by CI):**
1. Compiles code (Java 8 target)
2. Runs full test matrix across JDK/SLF4J combinations
3. Generates source JAR
4. Generates Javadoc JAR
5. Signs artifacts with GPG (key from GitHub secrets)
6. Deploys to Maven Central via Central Publishing Maven Plugin

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

### Scenario 6: Release to Maven Central (CI Automated)
Releases are fully automated via GitHub Actions:

1. **Create New Version** — manually triggered workflow that bumps the version and creates a git tag
2. **Release and Deploy** — automatically triggered by the version tag; runs full test matrix, then deploys to Maven Central

See `.github/workflows/create-new-version.yml` and `.github/workflows/release-deploy-version.yml` for details.

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

Releases are fully automated via GitHub Actions and follow a three-stage pipeline:

### Stage 1: Validate & Test
Runs automatically on every push and pull request via `maven-build-test.yml`.
Compiles and tests across the full JDK/SLF4J compatibility matrix.

### Stage 2: Create New Version
Manually triggered via **Actions > Create New Version** on GitHub, choosing
`major`, `minor`, or `patch` increment. This workflow:

1. Validates the current branch is `main` and up-to-date with `origin/main`
2. Runs the full test suite (`mvnw clean verify -P slf4j-2.0`)
3. Calculates the next semantic version from the latest `v*.*.*` tag
4. Commits the release version to `pom.xml`
5. Creates an annotated git tag `vX.Y.Z`
6. Commits the next `-SNAPSHOT` version
7. Pushes commits and the new tag

### Stage 3: Release and Deploy
Automatically triggered by the push of a `v*.*.*` tag via `release-deploy-version.yml`:

1. Runs the full test matrix (JDK 8/11/17/21 x SLF4J 1.7/2.0) with Logback integration tests
2. Builds with `-P release,slf4j-2.0`
3. Generates source JAR and Javadoc JAR
4. Signs artifacts with GPG (key from GitHub secrets)
5. Deploys to Maven Central via Central Publishing Maven Plugin
6. Creates a GitHub Release with auto-generated notes
7. Uploads JAR artifacts to the GitHub Release

### Pre-Release Validation (local)
For local validation before triggering a release:
```powershell
# Validate Javadoc
.\mvnw clean verify -P javadoc-validation

# Run full test suite
.\mvnw clean verify -P with-logback
```

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

### Pre-Release Validation
```powershell
# Validate docs and run full test suite before triggering CI release
.\mvnw clean verify -P javadoc-validation
.\mvnw clean verify -P with-logback
```

### Release
Releases are triggered via GitHub Actions: **Actions > Create New Version**.
See [Release Process](#release-process) above for the full pipeline.

---

## Summary

| Task | Command |
|------|---------|
| Development | `.\mvnw clean test` |
| Test Java 8 | `.\mvnw clean test -P jdk-8` |
| Test SLF4J 1.7 | `.\mvnw clean test -P slf4j-1.7-javax` |
| Validate Docs | `.\mvnw clean verify -P javadoc-validation` |
| Release | Triggered via GitHub Actions |
| Package Only | `.\mvnw clean package -DskipTests` |
| Coverage Report | `.\mvnw clean test jacoco:report` |

---

## Related Documentation

- [Test Execution Guide](test-execution-guide.md) - Detailed test execution strategies
- [pom.xml](../pom.xml) - Complete Maven configuration
- [README.md](../README.md) - Project overview
