# Reporter API Documentation

The `Reporter` is a diagnostic tool in *slf4j-toys* designed to generate and log comprehensive reports about the application's runtime environment. It is particularly useful for capturing a snapshot of the system configuration at application startup, which can be invaluable for troubleshooting issues related to the environment.

The `Reporter` is a minimalist, embedded solution for teams that need to understand the environment where their application is running without resorting to external, and often complex, monitoring agents.

## Core Concepts

The `Reporter` is composed of several individual "report modules" (e.g., `ReportVM`, `ReportMemory`), each responsible for collecting information about a specific aspect of the system.

When executed, the `Reporter` runs the enabled modules and logs their output as multi-line `INFO` messages to an SLF4J logger. This provides a detailed, human-readable summary of the system's state directly in your application logs.

## Configuration and Usage

Before running the `Reporter`, you must enable the specific report modules you want to generate. By default, most are disabled to keep the tool lightweight.

### Enabling Reports

You can enable reports in two ways:

**1. Via System Properties (Recommended)**
This is the most common and flexible method, as it allows you to configure reports without changing your application's code. You can pass these properties as `-D` flags at startup.

*Example:* To enable the VM, memory, and OS reports:
```bash
java -Dslf4jtoys.report.vm=true \
     -Dslf4jtoys.report.memory=true \
     -Dslf4jtoys.report.os=true \
     -jar my-application.jar
```

**2. Via Static Attributes**
You can also enable reports programmatically by setting the static boolean fields in the `ReporterConfig` class. This must be done before the `Reporter` is executed.

*Example:*
```java
import org.usefultoys.slf4j.report.ReporterConfig;

public class MyApplication {
    public static void main(String[] args) {
        // Enable desired reports programmatically
        ReporterConfig.reportVM = true;
        ReporterConfig.reportMemory = true;
        ReporterConfig.reportOperatingSystem = true;

        // ... now run the reporter
    }
}
```

### Use Case 1: Default Report at Application Startup

Once reports are enabled, the simplest way to run them is to call `Reporter.runDefaultReport()` at application startup.

**Example:**
```java
import org.usefultoys.slf4j.report.Reporter;
import org.usefultoys.slf4j.report.ReporterConfig;

public class MyApplication {
    public static void main(String[] args) {
        // Enable reports
        ReporterConfig.reportVM = true;
        ReporterConfig.reportMemory = true;

        // Log a comprehensive report of the enabled modules
        Reporter.runDefaultReport();

        // ... application logic continues ...
    }
}
```

### Use Case 2: Custom and Asynchronous Execution

For more control, you can instantiate a `Reporter` to specify a custom logger or run the reports asynchronously.

**Example:**
```java
import org.usefultoys.slf4j.report.Reporter;
import org.usefultoys.slf4j.LoggerFactory;
import org.slf4j.Logger;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Enable reports first (e.g., via system properties)

// 1. Get a custom logger
Logger diagnosticsLogger = LoggerFactory.getLogger("diagnostics");
Reporter reporter = new Reporter(diagnosticsLogger);

// 2. Run reports asynchronously
ExecutorService executor = Executors.newSingleThreadExecutor();
reporter.logDefaultReports(executor);
```

### Use Case 3: Running Individual Report Modules

You can also bypass the main `Reporter` and run any report module directly. This is useful for getting specific information on demand.

**Example:**
```java
import org.usefultoys.slf4j.report.Reporter.ReportMemory;
import org.slf4j.Logger;

Logger memoryLogger = LoggerFactory.getLogger("memory-check");

// Instantiate and run only the memory report module
new ReportMemory(memoryLogger).run();
```

## On-Demand Reporting with `ReportServlet`

For web applications, `slf4j-toys` provides the `ReportServlet`, which triggers specific reports on-demand via HTTP GET requests. This is useful for ad-hoc diagnostics by administrators or automated health checks.

When a request is made to a URL like `/report/VM`, the corresponding report module is executed, and its output is sent to the logs.

The following paths are supported, each corresponding to a report module:
- `/VM`
- `/FileSystem`
- `/Memory`
- `/User`
- `/PhysicalSystem`
- `/OperatingSystem`
- `/Calendar`
- `/Locale`
- `/Charset`
- `/NetworkInterface`
- `/SSLContext`
- `/DefaultTrustKeyStore`
- `/Environment`
- `/Properties`

### How to Configure

Register the servlet in your application's `web.xml` deployment descriptor.

```xml
<servlet>
    <servlet-name>ReportServlet</servlet-name>
    <servlet-class>org.usefultoys.slf4j.report.ReportServlet</servlet-class>
</servlet>
<servlet-mapping>
    <servlet-name>ReportServlet</servlet-name>
    <url-pattern>/admin/report/*</url-pattern>
</servlet-mapping>
```
With this configuration, a `GET` request to `/admin/report/VM` will trigger the JVM report.

### :warning: Security Warning

**It is critical to protect this endpoint.** Exposing detailed system information can be a security risk. Ensure that only authorized users or systems can access this URL. Protect it using standard security mechanisms like role-based access control (`<security-constraint>`), IP filtering, or a firewall.

## Full Configuration List

The behavior of the `Reporter` is controlled by static flags in the `ReporterConfig` class.

| System Property | `ReporterConfig` Field | Default | Description |
| :--- | :--- | :--- | :--- |
| `slf4jtoys.report.name` | `name` | `"report"` | The default logger name used by the `Reporter`. |
| `slf4jtoys.report.vm` | `reportVM` | `false` | JVM vendor, version, and runtime arguments. |
| `slf4jtoys.report.memory` | `reportMemory` | `false` | Heap, non-heap, and memory pool usage. |
| `slf4jtoys.report.filesystem` | `reportFileSystem` | `false` | Disk usage for all file system roots. |
| `slf4jtoys.report.physicals` | `reportPhysicalSystem`| `false` | CPU and physical memory architecture. |
| `slf4jtoys.report.os` | `reportOperatingSystem`| `false` | OS name, version, and architecture. |
| `slf4jtoys.report.user` | `reportUser` | `false` | Current user, home directory, and country. |
| `slf4jtoys.report.calendar` | `reportCalendar` | `false` | Current date, time, and timezone. |
| `slf4jtoys.report.locale` | `reportLocale` | `false` | Default locale and system-supported locales. |
| `slf4jtoys.report.charset` | `reportCharset` | `false` | Default charset and available charsets. |
| `slf4jtoys.report.env` | `reportEnvironment` | `false` | **(Sensitive)** All system environment variables. |
| `slf4jtoys.report.properties`| `reportProperties` | `false` | **(Sensitive)** All Java system properties. |
| `slf4jtoys.report.network` | `reportNetworkInterface`| `false` | Details for all network interfaces (IPs, MAC). |
| `slf4jtoys.report.ssl` | `reportSSLContext` | `false` | Default SSL/TLS context and protocols. |
| `slf4jtoys.report.keystore` | `reportDefaultTrustKeyStore`| `false` | **(Sensitive)** Lists all trusted certs. |

## Available Reports and Examples

Below are examples of the output generated by each report module.

### JVM Report
*   **Utility**: Shows which JVM is running the application, its version, and any startup arguments. Essential for diagnosing issues related to the Java runtime.
*   **Property**: `slf4jtoys.report.vm=true`
```
INFO report - Java Virtual Machine:
 - Name: OpenJDK 64-Bit Server VM
 - Vendor: Eclipse Adoptium
 - Version: 17.0.2+8
 - Home: /opt/java/openjdk-17
 - Arguments: -Dfile.encoding=UTF-8, -Xms256m, -Xmx2048m
```

### Memory Report
*   **Utility**: Provides a detailed breakdown of memory usage, including heap, non-heap, and specific memory pools. Crucial for identifying memory leaks or tuning memory allocation.
*   **Property**: `slf4jtoys.report.memory=true`
```
INFO report - Memory:
 - Heap: 245.0MB used, 4.0GB committed, 4.0GB max
 - Non-Heap: 80.3MB used, 92.2MB committed
 - Pools: G1 Eden Space, G1 Old Gen, Metaspace
```

### Operating System Report
*   **Utility**: Displays core information about the host operating system. Helps identify environment-specific bugs.
*   **Property**: `slf4jtoys.report.os=true`
```
INFO report - Operating System:
 - Name: Linux
 - Version: 5.15.0-48-generic
 - Architecture: amd64
```

### File System Report
*   **Utility**: Shows the available and used space for all mounted file systems. Useful for diagnosing "disk full" errors.
*   **Property**: `slf4jtoys.report.filesystem=true`
```
INFO report - File System:
 - /: 100.0GB used, 150.0GB free, 250.0GB total
 - /boot: 500.0MB used, 500.0MB free, 1.0GB total
```

### Network Interfaces Report
*   **Utility**: Lists all network interfaces with their IP addresses, MAC addresses, and status. Essential for troubleshooting network connectivity issues.
*   **Property**: `slf4jtoys.report.network=true`
```
INFO report - Network Interface 'eth0':
 - Status: up, broadcast, multicast
 - IPs: /192.168.1.100, /fe80:0:0:0:...
 - MAC: 00:1B:44:11:3A:B7
```

### Environment Variables Report
*   **Utility**: Dumps all environment variables visible to the Java process.
*   **Warning**: :warning: **HIGHLY SENSITIVE**. Can leak credentials, API keys, and other secrets. Use only in secure, controlled environments.
*   **Property**: `slf4jtoys.report.env=true`
```
INFO report - Environment Variables:
 - PATH: /usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin
 - JAVA_HOME: /opt/java/openjdk-17
 - MY_API_KEY: ******** (value may be exposed)
```

### System Properties Report
*   **Utility**: Dumps all Java system properties (`-D` flags and defaults).
*   **Warning**: :warning: **SENSITIVE**. Can leak configuration details or credentials.
*   **Property**: `slf4jtoys.report.properties=true`
```
INFO report - System Properties:
 - java.version: 17.0.2
 - user.home: /home/appuser
 - slf4jtoys.report.vm: true
```
