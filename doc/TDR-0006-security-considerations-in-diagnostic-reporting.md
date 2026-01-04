# TDR-0006: Security Considerations in Diagnostic Reporting

**Status**: Accepted
**Date**: 2026-01-03

## Context

The `Reporter` component is designed to provide comprehensive diagnostic information about the JVM, the operating system, and the environment. This creates an inherent conflict in the library's design: its primary goal is to provide maximum **transparency** about the runtime environment to aid in troubleshooting and auditing, but this same transparency can inadvertently **reveal sensitive details** about the infrastructure, configuration, or credentials.

While this information is invaluable for troubleshooting, it involves accessing and logging potentially sensitive data (e.g., system properties, environment variables, user paths, network configurations). If not handled carefully, these reports could leak credentials, tokens, or internal infrastructure details into log files.

## Decision

We implemented several security-conscious design patterns and features to mitigate the risk of information disclosure while maintaining the utility of the reports.

### Implemented Security Measures

1.  **Opt-in for Sensitive Sections**: High-risk report modules are disabled by default in `ReporterConfig`.
    *   `reportProperties` (System Properties): Defaults to `false`.
    *   `reportEnvironment` (Environment Variables): Defaults to `false`.
    *   `reportNetworkInterface`: Defaults to `false`.
    *   `reportSSLContext`: Defaults to `false`.
    *   `reportDefaultTrustKeyStore`: Defaults to `false`.
2.  **Regex-based Censorship (Blacklisting)**: For modules that list key-value pairs (System Properties and Environment Variables), a configurable regular expression (`forbiddenPropertyNamesRegex`) is used to identify sensitive keys.
    *   **Default Pattern**: `(?i).*password.*|.*secret.*|.*key.*|.*token.*` (case-insensitive).
    *   **Behavior**: If a key matches the pattern, its value is replaced with `********` in the log output.
3.  **Graceful SecurityManager Handling**: The library is designed to run in restricted environments (like some JavaEE containers or applets).
    *   Methods like `Reporter.getPropertySafely()` catch `SecurityException` and return `(Access denied)` instead of crashing.
    *   Report modules (e.g., `ReportSystemProperties`) catch `SecurityException` during bulk access and log a descriptive message.
4.  **Human-Readable Focus**: Reports are designed for human consumption (INFO level), which typically undergo different retention and access policies than machine-parsable data logs.
5.  **Log-Only Output (No Remote Leakage)**: Even when reports are triggered remotely (e.g., via a Servlet or JMX), the information is **never returned to the caller**. It is strictly written to the system logs. This ensures that an external attacker cannot directly scrape system details via the reporting trigger.

## Consequences

**Positive**:
*   **Reduced Attack Surface**: By disabling sensitive reports by default, the library follows the principle of least privilege.
*   **Credential Protection**: Common sensitive keys are automatically censored, preventing accidental leakage of database passwords or API tokens stored in system properties.
*   **Robustness**: The library does not require elevated permissions to function; it simply reports what it is allowed to see.
*   **Isolation**: Remote triggers do not expose data to the network, keeping the diagnostic information within the system's internal logging boundary.

**Negative**:
*   **Incomplete Censorship**: Blacklisting via regex is not foolproof. Sensitive data stored under non-standard keys (e.g., `MY_APP_AUTH_CREDENTIAL`) will not be caught by the default pattern.
*   **False Sense of Security**: Users might assume the library is "secure" and enable all reports without reviewing their specific environment's sensitive keys.

**Neutral**:
*   **Premise of Log Security**: The library operates on the strong premise that the **log environment is a secure area**. It assumes that either the log files are strictly protected by OS-level permissions or the logging framework (Logback, Log4j2) is configured to route `Meter` and `Reporter` data to a dedicated secure storage. If the logs are compromised, the diagnostic data becomes a significant liability.

## Unaddressed Security Concerns (Technical Debt)

1.  **No Whitelisting Support**: There is currently no mechanism to specify a "whitelist" of allowed properties. Whitelisting is generally more secure than blacklisting but more difficult to configure for general-purpose diagnostics.
2.  **Log Injection**: The library does not explicitly sanitize values for log injection attacks (e.g., CRLF injection). It assumes the underlying SLF4J implementation or log appender handles such concerns.
3.  **Memory Dumps**: While the library reports memory *usage*, it does not dump memory contents. However, if a report is triggered during a sensitive operation, the log file itself becomes a target for attackers.
4.  **Dynamic Configuration Risks**: Since `ReporterConfig` fields are public and non-final, a malicious component within the same JVM could programmatically disable censorship or enable sensitive reports without the administrator's knowledge.
5.  **Sanitization of Paths**: User home directories and classpath entries are logged as-is. In some environments, these paths might reveal sensitive usernames or internal directory structures.

## Implementation

*   `ReporterConfig.forbiddenPropertyNamesRegex` provides the central control for censorship.
*   `ReportSystemProperties` and `ReportSystemEnvironment` implement the matching and masking logic.
*   `Reporter.getPropertySafely` provides a reusable pattern for safe property access.

## References

*   [Reporter.java](../src/main/java/org/usefultoys/slf4j/report/Reporter.java)
*   [ReporterConfig.java](../src/main/java/org/usefultoys/slf4j/report/ReporterConfig.java)
*   [ReportSystemProperties.java](../src/main/java/org/usefultoys/slf4j/report/ReportSystemProperties.java)
*   [TDR-0005: Robust and Minimalist Configuration Mechanism](./TDR-0005-robust-and-minimalist-configuration-mechanism.md)
