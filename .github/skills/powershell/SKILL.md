---
name: powershell
description: 'Instructions for running maven using PowerShell in the slf4j-toys project.'
---

# PowerShell Usage Guidelines

## Java Environment Setup

Set up `JAVA_HOME` and update `PATH` for JDK 21 on each new PowerShell terminal session.

**Prerequisites:**
- JDK 21 must be installed in `$env:USERPROFILE\.jdks` directory

**Setup:**
Execute the following command once per terminal session:

```powershell
$jdk21 = Get-ChildItem -Path "$env:USERPROFILE\.jdks" -Filter "*21*" | Sort-Object -Property Name -Descending | Select-Object -First 1
if ($null -ne $jdk21) {
    $env:JAVA_HOME = $jdk21.FullName
    $env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
}
```

**Verification:**
To verify the setup, execute:

```powershell
java -version
$env:JAVA_HOME
```

## Terminal Session Management

**CRITICAL**: Always use the existing terminal session. Never create new terminal sessions or spawn new interpreter processes:

```powershell
# ✅ CORRECT: Execute commands directly in current session
.\mvnw test
Get-ChildItem
javac MyClass.java

# ❌ WRONG: Never spawn new shells or create sub-processes
powershell -Command ".\mvnw test"        # Creates unnecessary new PowerShell process
cmd /c "mvnw test"                       # Spawns cmd.exe sub-process
Start-Process powershell -ArgumentList "..." # Opens new window
bash -c "mvnw test"                      # Attempts to launch bash

# ❌ WRONG: Never use background execution
Start-Job { .\mvnw test }                # Background job
.\mvnw test &                            # Background operator (doesn't work as in Unix)
```

## Parameter Escaping

When Maven/Java parameters contain special characters (`-D`, `@`, `#`, `=`), wrap them in single quotes to prevent PowerShell from interpreting them as delimiters:

```powershell
# ✅ CORRECT: Single quotes protect special characters
.\mvnw test -P slf4j-2.0,with-logback -Dtest=MessageHighlightConverterTest
.\mvnw test -P slf4j-2.0,with-logback '-Dtest=MeterLifeCycleTest#shouldCreateMeterWithLoggerInitialState'
```

```powershell
# ❌ WRONG: Without quotes, PowerShell interprets # as comment start
.\mvnw test -P slf4j-2.0,with-logback -Dtest=MeterLifeCycleTest#shouldCreateMeterWithLoggerInitialState
# Result: Maven receives only "MeterLifeCycleTest" and ignores method name after #
```

## Command Availability

Always use PowerShell-native commands, not Unix/Linux/bash equivalents that don't exist on Windows:

```powershell
# ✅ CORRECT: PowerShell commands
Get-ChildItem              # Instead of: ls
Get-Content -Tail 10       # Instead of: tail -10
Select-String "pattern"    # Instead of: grep "pattern"
```

```powershell
# ❌ WRONG: Unix/Linux commands don't work on Windows PowerShell
ls                         # May fail or produce unexpected results
tail -10 file.txt          # Command not found
grep "pattern" file.txt    # Command not found
```

## Command Chaining

Never use `&&` or `||` operators from bash/Unix shells - they don't work in PowerShell:

```powershell
# ✅ CORRECT: Use semicolon (;) to chain commands
.\mvnw clean; .\mvnw test
.\mvnw compile; .\mvnw package

# ❌ WRONG: && and || don't work in PowerShell
.\mvnw clean && .\mvnw test    # Error: unexpected token '&&'
.\mvnw test || echo "failed"   # Error: unexpected token '||'
```

## Path Separators

PowerShell accepts both backslash and forward slash in paths:

```powershell
# ✅ CORRECT: Both work (PowerShell normalizes them)
Get-Content "src\main\java\MyClass.java"
Get-Content "src/main/java/MyClass.java"

# ⚠️ NOTE: Avoid mixing separators in the same path for clarity
```

## Environment Variables

Use `$env:` prefix to access environment variables:

```powershell
# ✅ CORRECT: Use $env: prefix
echo $env:JAVA_HOME
$env:PATH += ";C:\new\path"
.\mvnw test -Dmaven.repo.local=$env:USERPROFILE\.m2\repository

# ❌ WRONG: Unix-style won't work
echo $JAVA_HOME              # Looks for PowerShell variable, not environment variable
export JAVA_HOME=/path       # 'export' command doesn't exist
```

## Line Continuation

Use backtick (`` ` ``) at the end of a line to continue on the next line:

```powershell
# ✅ CORRECT: Use backtick (`) at end of line
.\mvnw test `
  -P slf4j-2.0,with-logback `
  -Dtest=MeterLifeCycleTest `
  -Dmaven.test.failure.ignore=true

# ❌ WRONG: Backslash doesn't work
.\mvnw test \
  -P slf4j-2.0
```

## String Quoting

Single quotes create literal strings; double quotes allow variable expansion:

```powershell
# ✅ CORRECT: Single quotes for literals, double for variable expansion
'literal string with $var'         # Outputs: literal string with $var
"string with $env:JAVA_HOME"       # Expands: string with C:\Program Files\Java\jdk-21

# ⚠️ IMPORTANT: Use single quotes for Maven parameters to prevent PowerShell variable expansion
.\mvnw test '-Dtest=MyTest#method'     # Correct: # is literal
.\mvnw test "-Dtest=MyTest#method"     # Also works in this case, but prefer single quotes
```
