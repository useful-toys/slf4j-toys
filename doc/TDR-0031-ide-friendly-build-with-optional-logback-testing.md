# TDR-0031: IDE-Friendly Build with Optional Logback Testing

**Status**: Accepted  
**Date**: 2026-01-12

## Context

The `slf4j-toys` library provides optional Logback-specific features (custom converters for status highlighting and message formatting). These features require:
1. **Logback-specific production code** (`src/logback-main/java`): Custom `LayoutConverter` implementations
2. **Logback-specific integration tests** (`src/logback-test/java`): Tests that verify converter behavior with real Logback

However, including these Logback features in the default build creates several problems:

### The Challenge: SLF4J Binding Conflicts

**The core problem**: SLF4J can only bind to ONE logging implementation at runtime.

- **Core tests** (Meter, Watcher, Reporter) require `slf4j-test-mock` which provides a `MockLogger` implementation for precise log assertion in tests
- **Logback integration tests** require `logback-classic` which provides the real `Logger` implementation
- Having **both on the classpath** causes a runtime `ClassCastException`:
  ```
  java.lang.ClassCastException: ch.qos.logback.classic.Logger 
    cannot be cast to org.slf4j.impl.MockLogger
  ```

### The IDE Problem

Modern IDEs (especially IntelliJ IDEA) import Maven projects using the **default build configuration**. When both source directories (`src/logback-main/java` and `src/main/java`) and conflicting dependencies are present:

1. **IDE confusion with multiple source roots**: IDEs don't know which classes belong together
2. **Dependency conflicts**: IDE cannot resolve which SLF4J binding to use
3. **Test execution failures**: Running tests from IDE triggers ClassCastException
4. **No IDE support for conditional builds**: IDEs don't handle Maven profile-based source roots well
5. **Degraded developer experience**: 99% of development work is on core features, not Logback converters

### Previous Approaches That Failed

We attempted several solutions that didn't work:

1. **Maven Surefire with multiple executions in single build**:
   - Problem: Maven profiles don't isolate classpaths within the same build
   - IDE still imports all source roots and dependencies

2. **Separate module** (`slf4j-toys-logback`):
   - Problem: Massive POM duplication (all dependencies, plugins, profiles repeated)
   - Maintenance burden for ~84 tests covering optional features

3. **JUnit tags** (`@Tag("logback")`):
   - Problem: Cannot remove dependencies from classpath based on tags
   - Both bindings still present, causing ClassCastException

## Decision

We decided to use a **two-tier testing strategy**:

1. **DEFAULT BUILD**: IDE-friendly, no Logback, no profile needed
2. **WITH-LOGBACK PROFILE**: Maven-only, activates Logback testing

### Implementation Strategy

#### 1. Default Build Configuration (No Profile)

The default Maven/IDE import provides:

- **Source directories**:
  - `src/main/java` (core code)
  - `src/test/java` (core tests)
  - **EXCLUDES** `src/logback-main/java`
  - **EXCLUDES** `src/logback-test/java`

- **Dependencies**:
  - `slf4j-api` (compile)
  - `slf4j-test-mock` (test) ← Provides MockLogger
  - **EXCLUDES** `logback-classic`

- **Surefire execution** `default-test`:
  - Runs ~1441 tests (Meter, Watcher, Reporter, utils)
  - Excludes `**/logback/**/*Test.java`
  - Excludes `logback-classic` from test classpath
  - **IDE can run/debug these tests** ✅

#### 2. With-Logback Profile (Maven Only)

Activated with: `mvn test -P slf4j-2.0,with-logback`

The profile adds:

- **Additional source directories** (via `build-helper-maven-plugin`):
  - `src/logback-main/java` (Logback converters)
  - `src/logback-test/java` (integration tests)

- **Additional dependency**:
  - `logback-classic` (provided, optional)

- **Additional Surefire execution** `logback-tests`:
  - Runs ~84 Logback integration tests
  - Includes **ONLY** `**/logback/**/*Test.java`
  - Excludes `slf4j-test-mock` from test classpath
  - Includes `logback-classic` on classpath
  - **IDE CANNOT run these tests** ❌ (source dirs not imported)

### Maven Configuration Details

#### Surefire Plugin - Two Executions

```xml
<plugin>
    <artifactId>maven-surefire-plugin</artifactId>
    <executions>
        <!-- Always active -->
        <execution>
            <id>default-test</id>
            <configuration>
                <excludes>
                    <exclude>**/logback/**/*Test.java</exclude>
                </excludes>
                <classpathDependencyExcludes>
                    <classpathDependencyExclude>ch.qos.logback:logback-classic</classpathDependencyExclude>
                </classpathDependencyExcludes>
            </configuration>
        </execution>
    </executions>
</plugin>
```

```xml
<!-- In with-logback profile -->
<plugin>
    <artifactId>maven-surefire-plugin</artifactId>
    <executions>
        <!-- Only when profile active -->
        <execution>
            <id>logback-tests</id>
            <configuration>
                <includes>
                    <include>**/logback/**/*Test.java</include>
                </includes>
                <classpathDependencyExcludes>
                    <classpathDependencyExclude>org.usefultoys:slf4j-test-mock</classpathDependencyExclude>
                </classpathDependencyExcludes>
            </configuration>
        </execution>
    </executions>
</plugin>
```

**Key insight**: Maven allows the same plugin to be declared both in `<build>` and in `<profile><build>` if they have the **same version**. Maven merges the executions from both declarations when the profile is active.

#### Build Helper Plugin - Conditional Sources

```xml
<!-- In with-logback profile -->
<plugin>
    <artifactId>build-helper-maven-plugin</artifactId>
    <executions>
        <execution>
            <id>add-logback-source</id>
            <phase>generate-sources</phase>
            <goals><goal>add-source</goal></goals>
            <configuration>
                <sources>
                    <source>src/logback-main/java</source>
                </sources>
            </configuration>
        </execution>
        <execution>
            <id>add-logback-test</id>
            <phase>generate-test-sources</phase>
            <goals><goal>add-test-source</goal></goals>
            <configuration>
                <sources>
                    <source>src/logback-test/java</source>
                </sources>
            </configuration>
        </execution>
    </executions>
</plugin>
```

These source directories are **ONLY added during Maven build with profile active**, they are **NOT imported by IDEs**.

## Consequences

### Positive ✅

1. **IDE-Friendly Development**:
   - Clean project structure in IDE (no Logback confusion)
   - Full IDE support: run, debug, code coverage, refactoring
   - No SLF4J binding conflicts during development
   - 99% of development work (Meter, Watcher, Reporter) has full IDE support

2. **Complete CI/CD Coverage**:
   - Build matrix includes `with-logback` profile
   - All 1525 tests executed (1441 core + 84 logback)
   - Logback features verified in every CI/CD run

3. **Simplified Developer Workflow**:
   - New developers can import and run tests immediately
   - No profile configuration needed in IDE
   - No Maven knowledge required for daily work

4. **Single JAR Distribution**:
   - All features (with and without Logback) in one artifact
   - Users get Logback converters if they have `logback-classic` on their runtime classpath
   - No separate `slf4j-toys-logback.jar` needed

5. **Classpath Isolation**:
   - Each test execution has exactly one SLF4J binding
   - No runtime ClassCastException
   - Tests are deterministic and reliable

### Negative ❌

1. **No IDE Support for Logback Tests**:
   - Developers cannot run/debug Logback tests from IDE
   - Must use Maven command: `mvn test -P slf4j-2.0,with-logback`
   - Logback test development requires Maven-based workflow

2. **Build Complexity**:
   - POM requires careful configuration of profiles and executions
   - Comments and documentation essential for maintainability
   - New developers must understand the build strategy

3. **Logback Code Not Visible in IDE**:
   - `src/logback-main/java` files can be edited but are not imported
   - No IDE refactoring support for Logback converters
   - Syntax errors only caught during Maven build

### Neutral ⚖️

1. **Documentation Required**:
   - Comprehensive comments in POM explain the strategy
   - This TDR documents the decision rationale
   - Build guide shows commands for all scenarios

2. **CI/CD Overhead**:
   - Tests run twice: once default, once with-logback
   - Acceptable because total test time is still reasonable
   - Ensures complete coverage across configurations

## Alternatives Considered

### ❌ Separate Module (`slf4j-toys-logback`)

**How it works**:
- Create `slf4j-toys-logback/pom.xml`
- Duplicate all dependencies, plugins, profiles
- Move Logback code to separate module

**Why rejected**:
- Massive POM duplication (200+ lines repeated)
- Two artifacts to maintain and version
- Users must add two dependencies
- Only 84 tests justify this complexity? No.

### ❌ Both Dependencies on Classpath with JUnit Tags

**How it works**:
- Add both `slf4j-test-mock` and `logback-classic` as test dependencies
- Use `@Tag("logback")` to separate tests
- Configure Surefire to run tags separately

**Why rejected**:
- Cannot remove dependencies from classpath based on tags
- Both SLF4J bindings present → ClassCastException
- Maven Surefire classpath is per-execution, not per-tag

### ❌ IDE Profile Configuration

**How it works**:
- Configure IDE to switch between profiles
- IntelliJ: Maven tool window → Profiles → toggle `with-logback`

**Why rejected**:
- IDEs don't handle profile-based source roots well
- Switching profiles doesn't reimport source directories
- Must reimport project after every profile change
- Confusing workflow for 99% of work that doesn't need Logback

### ❌ Optional Compilation with `maven-compiler-plugin` Excludes

**How it works**:
- Keep all sources in `src/main/java` and `src/test/java`
- Use compiler excludes to skip Logback classes by default
- Profile removes excludes

**Why rejected**:
- IDE still imports all source files (doesn't understand compiler excludes)
- IDE shows compile errors for Logback code when `logback-classic` not on classpath
- False positive errors during normal development

## Implementation

### File Structure

```
slf4j-toys/
├── src/
│   ├── main/java/              ← Core production code (always compiled)
│   ├── test/java/              ← Core tests (always compiled)
│   ├── logback-main/java/      ← Logback converters (profile only)
│   └── logback-test/java/      ← Logback tests (profile only)
├── pom.xml                     ← Defines both build configurations
└── doc/
    ├── TDR-0031-*.md          ← This document
    └── build-execution-guide.md ← User-facing guide
```

### Commands

```powershell
# Default build (IDE-friendly, 1441 tests)
mvn clean test

# Complete build (Maven only, 1525 tests)
mvn clean test -P slf4j-2.0,with-logback

# Run only Logback tests (Maven only, 84 tests)
mvn surefire:test@logback-tests -P slf4j-2.0,with-logback
```

### CI/CD Integration

GitHub Actions workflow runs:
```yaml
- name: Test without Logback
  run: mvn test -P slf4j-2.0

- name: Test with Logback  
  run: mvn test -P slf4j-2.0,with-logback
```

This ensures both configurations are validated on every commit.

## Related Decisions

- **TDR-0009**: Multi-Spec Servlet Support (javax vs. jakarta)
  - Similar profile-based strategy for javax/jakarta variants
  - Different issue: coexisting implementations vs. conflicting dependencies

- **TDR-0010**: Simultaneous Support for SLF4J 1.7/2.0 and Logback 1.2-1.5
  - Profiles for SLF4J version combinations
  - Covers API compatibility, not test execution strategy

- **TDR-0028**: Logback Lifecycle Status and Color Highlighting
  - Documents WHAT the Logback features do
  - This TDR documents HOW they are tested

## References

- [pom.xml](../pom.xml) - See top-level comment block for detailed explanation
- [build-execution-guide.md](./build-execution-guide.md) - User-facing guide
- [TDR-0009](./TDR-0009-multi-spec-servlet-support-javax-vs-jakarta.md)
- [TDR-0010](./TDR-0010-simultaneous-support-for-slf4j-1.7-2.0-and-logback-1.2-1.5.md)

## Notes for Future Maintainers

### When Adding New Logback Features

1. Add production code to `src/logback-main/java/`
2. Add integration tests to `src/logback-test/java/org/usefultoys/slf4j/logback/`
3. Use package naming: `**/logback/**/*` to match Surefire includes
4. Run tests with: `mvn test -P slf4j-2.0,with-logback`
5. IDE won't see these files - edit with text editor or external tool

### When Modifying Core Features

1. Work normally in IDE with `src/main/java/` and `src/test/java/`
2. Run tests directly from IDE (right-click → Run Test)
3. No Maven profile needed
4. Full debugging and coverage support

### If SLF4J 3.0 Changes Binding Mechanism

If SLF4J 3.0 allows multiple bindings or provides better isolation:
- Re-evaluate this strategy
- May be possible to simplify to single build configuration
- Keep this TDR as historical context for the decision

