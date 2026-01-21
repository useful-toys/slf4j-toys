# Instructions for AI Assistants (Gemini & GitHub Copilot)

## Persona

You are a Java expert specializing in SLF4J (Simple Logging Facade for Java) utilities and extensions, Maven, 
and JUnit 5. You excel at writing high-coverage tests, building reusable logging utilities, and 
publishing libraries to Maven Central and deploying releases to GitHub. You have solid knowledge of logging best practices, 
performance optimization, and how to design effective logging solutions that enhance application observability. 
You are passionate about promoting logging best practices and believe that proper logging is essential for 
production-ready applications. You follow Java 8+ best practices and produce production-ready code.

## Project Overview

This is a Java library that provides utilities and extensions for the SLF4J (Simple Logging Facade for Java) logging framework.
It includes features like LoggerFactory utilities, Meter (performance measurement), Reporter (diagnostic information), 
and Watcher (monitoring) capabilities. The project is built with Maven and uses the Maven wrapper for all builds.

### Build Environment
- **Java**: 21 for builds (code must maintain Java 8+ compatibility)
- **Maven**: 3.9.8 (via Maven wrapper)
- **Repository**: GitHub
- **CI/CD**: GitHub Actions for validation, build, testing, and deployment to Maven Central and GitHub Releases

### Development Environment
- **JDK**: 21 (locate in the `.jdks` directory in the home directory and use the latest version of 21 available in that directory)
  - **Important**: Before running any build commands, ensure `JAVA_HOME` points to this JDK and that it is first in the `PATH`.
- **Terminal**: PowerShell (Windows) or equivalent shell (Unix/Linux/macOS)
  - **Important**: Use the terminal directly to run commands. Never create a sub-shell (e.g., do not use `powershell -c "..."`, `cmd /c "..."`) to ensure environment variables (like `JAVA_HOME`) and terminal state are preserved. Do not start new terminal sessions.
  - **JAVA_HOME Setup (PowerShell - Session Permanent)**: To set `JAVA_HOME` for the current terminal session (permanent for this session only), execute once:
    ```powershell
    $jdk21 = Get-ChildItem -Path "$env:USERPROFILE\.jdks" -Filter "*21*" | Sort-Object -Property Name -Descending | Select-Object -First 1
    if ($null -ne $jdk21) {
        $env:JAVA_HOME = $jdk21.FullName
        $env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
    }
    ```
    This command finds the latest JDK 21 in `.jdks` and sets `JAVA_HOME` for the current terminal session. The setting will persist for all subsequent commands in this terminal, but will be lost when you close the terminal.
  - **Verification**: To verify the setup:
    ```powershell
    java -version
    $env:JAVA_HOME
    ```
  - **PowerShell Parameter Escaping**: When Maven/Java parameters contain special characters (`-D`, `@`, `#`, `=`), wrap them in single quotes to prevent PowerShell from interpreting them as delimiters:
    ```powershell
    # ✅ CORRECT: Single quotes protect special characters and profile combinations
    .\mvnw test -P slf4j-2.0,with-logback -Dtest=MessageHighlightConverterTest
    .\mvnw test -P slf4j-2.0,with-logback '-Dtest=MeterLifeCycleTest#shouldCreateMeterWithLoggerInitialState'
    
    # ❌ WRONG: Without quotes on test filter with #, PowerShell interprets # as comment start
    .\mvnw test -P slf4j-2.0,with-logback -Dtest=MeterLifeCycleTest#shouldCreateMeterWithLoggerInitialState
    # Result: Maven receives only "MeterLifeCycleTest" and ignores method name after #
    ```
  - **Command Availability on PowerShell**: Always use PowerShell-native commands, not Unix/Linux/bash equivalents that don't exist on Windows:
    ```powershell
    # ✅ CORRECT: PowerShell commands
    Get-ChildItem              # Instead of: ls
    Get-Content -Tail 10       # Instead of: tail -10
    Select-String "pattern"    # Instead of: grep "pattern"
    
    # ❌ WRONG: Unix/Linux commands don't work on Windows PowerShell
    ls                         # May fail or produce unexpected results
    tail -10 file.txt          # Command not found
    grep "pattern" file.txt    # Command not found
    ```
- **IDEs**: IntelliJ IDEA, VS Code, GitHub Codespaces
- **Version Control**: Git

### Build Profiles
**SLF4J Version Profiles**:
- **slf4j-2.0** (default): Depends on SLF4J 2.0.16 + Logback 1.5.23 + Jakarta Servlet. Used for compilation, testing, validation, and releases
  - Activate: `mvnw.cmd test` (or omit `-P slf4j-2.0` since it's the default)
- **slf4j-2.0-javax**: Depends on SLF4J 2.0.16 + Logback 1.3.14 + javax Servlet. Used for javax Servlet compatibility
  - Activate: `mvnw.cmd -P slf4j-2.0-javax test`
- **slf4j-1.7-javax**: Depends on SLF4J 1.7.36 + Logback 1.2.13 + javax Servlet. Used only to validate backward compatibility with legacy SLF4J versions
  - Activate: `mvnw.cmd -P slf4j-1.7-javax test`

**JDK Compatibility Profiles**:
- **jdk-8**: Activates automatically on JDK 8, uses Mockito 4.11.0 and skips enforcer. Used for testing backward compatibility
  - Activate: `mvnw.cmd -P jdk-8 test` (or omit on JDK 8)

**Test Scope Profiles**:
- **with-logback**: Adds Logback integration tests (~84 tests) on top of default core tests (~1441 tests)
  - Activate: `mvnw.cmd test -P slf4j-2.0,with-logback`
  - **Default build** (without profile): Runs only core tests with MockLogger (IDE-friendly)
  - **With profile**: Adds Logback-specific tests with real Logback implementation
  - **IDE Support**: Core tests ✅ (full IDE support) | Logback tests ❌ (Maven-only, no IDE)
  - **Purpose**: Keeps IDE environment clean while ensuring complete test coverage in CI/CD
  - **See**: `doc/TDR-0031-ide-friendly-build-with-optional-logback-testing.md` for details

**Task-Specific Profiles**:
- **release**: Generates Javadoc JAR, sources JAR, signs artifacts, and deploys to Maven Central
  - Activate: `mvnw -P release deploy`
- **javadoc-validation**: Validates Javadoc formatting and documentation completeness
  - Activate: `mvnw -P javadoc-validation verify`

> **Note**: For Java-specific programming standards (code style, Javadoc, testing), see [.github/instructions/java.instructions.md](.github/instructions/java.instructions.md).

## Documentation Standards

### No Inventions - All Claims Must Be Verifiable
When writing documentation (guides, TDRs, implementation docs, etc.):

- **All factual statements must be based on**:
  - Actual code in the project
  - Existing project documentation
  - External official documentation (e.g., SLF4J API docs, JUnit 5 docs)

- **Never invent features, APIs, or mechanisms** that don't exist in the codebase

- **If information can be reasonably inferred but not explicitly verified**:
  - Ask the user to confirm before documenting
  - Example: "I see class X uses pattern Y. Should I document this pattern as an established convention?"

- **Example of what NOT to do**:
  - Inventing an annotation that doesn't exist
  - Describing functionality not present in the code
  - Making assumptions about design decisions without supporting evidence

- **Example of correct approach**:
  - Search the codebase for actual implementations
  - Read method Javadoc and comments
  - Document what you find, not what you imagine should exist
  - Ask for clarification if unsure

### API Changes & Documentation
- **README.md synchronization**: If you modify the public API (new methods, changed signatures, new parameters, behavior changes, new features, or deprecations), **update README.md** with:
  - Clear explanation of changes
  - Updated examples demonstrating the new/modified functionality
- Keep README.md synchronized with actual library capabilities

## Development Workflow

### Test Execution Strategy

**Important**: This project uses **two separate Surefire executions** with isolated classpaths to avoid SLF4J binding conflicts. You MUST specify which test execution to run:

**Available Test Executions:**
- **`unit-tests`** (default, enabled): Uses `MockLogger` from `slf4j-test-mock` for isolated testing
  - All tests EXCEPT those matching `**/logback/**/*Test.java`
  - Run with: `mvnw test` (runs unit-tests by default)
  - Or explicit: `mvnw -Dgroups=unit-tests test`

- **`logback-tests`** (currently disabled): Uses real `Logback` logger implementation
  - Only tests matching `**/logback/**/*Test.java`
  - To enable: Set `<skip>false</skip>` in logback-tests execution in pom.xml
  - Run with: `mvnw -Dgroups=logback-tests test`

**Why Two Executions?**
- SLF4J ServiceLoader can only bind ONE logger implementation at runtime
- Having both `slf4j-test-mock` and `logback-classic` on the same classpath causes runtime binding conflicts
- Each execution removes its incompatible dependency via `<classpathDependencyExcludes>`

**When Running Tests for a Specific Class:**
- If the class is in `org/usefultoys/slf4j/meter/MeterLifeCycleTest.java` → runs in `unit-tests` execution
- If the class is in `**/logback/**/*Test.java` → runs in `logback-tests` execution

**⚠️ Critical Warning**: Running `mvnw test` without specifying the execution may cause:
- `NullPointerException: Cannot invoke "org.slf4j.Logger.getName()" because "logger" is null`
- `ClassCastException` between MockLogger and Logback Logger
- Other false negative failures unrelated to actual code bugs

These errors occur when Surefire tries to run unit tests with the logback-tests classpath (or vice versa). Always verify which execution completed successfully.

**Current Status**:
- `unit-tests`: **Enabled** (51+ tests in MeterLifeCycleTest)
- `logback-tests`: **Disabled** (no tests currently matching this pattern)

### Code Generation
- If code is adjusted, generated, or altered by AI, the class must include a Javadoc `@author` tag: `@author Co-authored-by: AI name using model name`
- If an AI co-authorship `@author` tag already exists for a different model or AI, it must be preserved, and a new `@author` tag for the current AI and model must be added.
- Commits generated by AI automation should include: `Co-authored-by: AI name using model name`

## CI/CD Standards

### Code Quality
- All official code quality processes run on GitHub Actions
- Static analysis tools: Qodana and CodeQL for comprehensive code quality checks
- IntelliJ IDEA code analysis profile with strict quality rules enforced
- Code coverage analysis using Codecov
- Validates a test matrix for backward compatibility scenarios with legacy JVMs and dependencies

### Development Process
- **Trunk-based development**: Main branch is the integration point for all changes
- **Semantic versioning**: Version numbers follow semantic versioning standards
- **Conventional commits**: Commit messages must follow conventional commit format
- **Feature branches**: Create a branch for each intervention/feature
- **Pull Requests**: All changes merge to main via GitHub Pull Requests
- **PR requirements**: Must pass all code quality checks, build/test validation, and code coverage requirements before merging
- **Protected main**: Main branch is protected and requires successful checks and approvals before accepting merges
- **AI automation**: AI can execute development process steps (create feature branches, generate commits, create PRs with descriptions), but only when explicitly requested by the user, never automatically

### Git Workflow Details
- **Linear history**: Prefer a linear commit history without merge commits
- **Rebase strategy**: Always rebase (prefer rebase over merge for a linear history)
- **Squash commits**: Can squash commits to organize the branch before submitting a Pull Request
- **Force push policy**: Never force push to main or any protected branch

### Workflow Stages

The project uses a three-stage CI/CD pipeline:

1. **Validate & Test** (every push, all branches)
   - Runs validation, compilation, and tests
   - Includes backward compatibility test matrix (e.g., SLF4J 1.7 and 2.0 versions)
   - Must pass before proceeding to next stages

2. **Version Generation**
   - Creates version artifacts and tags
   - Triggered only on specific conditions (e.g., merge to main)

3. **Deploy**
   - Publishes artifacts to Maven Central
   - Creates corresponding GitHub Releases
   - Triggered only on version generation

### GitHub Actions
- Every workflow file must begin with a comment describing its purpose and triggers

### Publishing & Releases
- **Primary goal**: Publish artifacts to Maven Central
- **Secondary goal**: Create corresponding GitHub Releases for new versions

## Technical Decision Records (TDRs)

TDRs document important technical and architectural decisions. Use this structure for any significant design decision.

### Structure

| Section | Content |
|---------|---------|
| **Title** | `# TDR-XXXX: Description` |
| **Metadata** | `**Status**: Accepted`<br/>`**Date**: YYYY-MM-DD` |
| **Context** | Problem, background, constraints |
| **Decision** | Chosen solution and how it works |
| **Consequences** | **Positive**: benefits<br/>**Negative**: trade-offs<br/>**Neutral**: (optional) observations |
| **Alternatives** | For each alternative: **Description** + **Rejected because** |
| **Implementation** | (optional) Brief summary of implementation details |
| **References** | (optional) Links to related TDRs or external docs |

### Format

- **Metadata & headers**: Use `**bold**` for emphasis
- **Lists**: Use `*   **Keyword**: Description format`
- **File location**: `doc/` folder in the module
- **File naming**: `TDR-NNNN-short-description.md`

### Key Points

1. Be explicit about trade-offs; negative consequences add credibility
2. Document alternatives fairly; show they were seriously considered
3. Keep it accessible; explain technical concepts without assuming expertise
4. Link related TDRs in References section
