---
name: java-env-setup
description: Configures JAVA_HOME and PATH environment variables for Java 21 in PowerShell
---

# Java Environment Setup Skill

This skill must be activated in each new PowerShell terminal session to configure Java JDK 21 as the active JDK. Maven builds require this configuration to compile and run the project.

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
