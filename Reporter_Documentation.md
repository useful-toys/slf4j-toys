# 📊 `org.usefultoys.slf4j.report`

A lightweight diagnostic logging toolkit for Java applications. Useful for logging detailed system environment reports at startup, debugging, or during runtime analysis.

---

## Reporter

Generates and logs detailed system reports such as JVM info, OS details, memory, locale, network interfaces, SSL contexts, and more.

### Basic Usage

```java
// Runs the default report synchronously on the current thread
Reporter.runDefaultReport();
```

### Usage

```java
// Create reporter with default logger (defined in ReporterConfig.name)
Reporter reporter = new Reporter();

// OR specify a custom logger
Logger logger = LoggerFactory.getLogger("diagnostics");
Reporter reporter = new Reporter(logger);

// Run configured reports
reporter.logDefaultReports(Reporter.sameThreadExecutor); // Synchronous execution

// OR run in separate threads
ExecutorService executor = Executors.newCachedThreadPool();
reporter.logDefaultReports(executor);
```

## ⚙️ `ReporterConfig`

Holds static flags that control which reports the `Reporter` should run.

```java
ReporterConfig.reportVM = true;
ReporterConfig.reportMemory = false;
ReporterConfig.reportNetworkInterface = true;
ReporterConfig.reportSSLContext = false;
```

You can also configure them via system properties:

```plaintext
-Dslf4jtoys.report.vm=true
-Dslf4jtoys.report.memory=false
-Dslf4jtoys.report.networkinterface=true
```

## ReportServlet

A `HttpServlet` that listens to HTTP GET requests and triggers system reports based on the requested path.

Supported paths:

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

Example Request:

```http
GET /myapp/report/VM
```

Logs JVM information to SLF4J.

### ⚠️ Security Notes

- **Do not expose** this servlet in production without:
  - authentication
  - IP filtering
  - rate limiting
- Reports may leak sensitive data (env vars, tokens, passwords)

## Built-in Report Modules

Each is an inner class of `Reporter`, implementing `Runnable`. You can trigger them manually if needed:

| Class                      | Description                                |
|---------------------------|---------------------------------------------|
| `ReportVM`                | JVM vendor, version, home                   |
| `ReportFileSystem`        | Disk usage info                             |
| `ReportMemory`            | Heap and memory usage                       |
| `ReportUser`              | Current user and home directory             |
| `ReportPhysicalSystem`    | CPU count                                   |
| `ReportOperatingSystem`   | OS name, version, separators                |
| `ReportCalendar`          | Date/time and timezone                      |
| `ReportLocale`            | Locale info and available locales           |
| `ReportCharset`           | Charset info and supported charsets         |
| `ReportNetworkInterface`  | IPs, flags, MACs for each NIC               |
| `ReportSSLContext`        | SSL/TLS config details                      |
| `ReportDefaultTrustKeyStore` | Lists trusted certs from default store   |
| `ReportJdbcConnection`       | JDBC connections properties              |

### Running build-in report modules individually

Each build-in report module is a Runnable that can be run individually. For example:

```java
  final Executor executor = Reporter.sameThreadExecutor;
  final Logger logger = LoggerFactory.getLogger(ReporterConfig.name);
  executor.execute(new ReportPhysicalSystem(logger));
  executor.execute(new ReportMemory(logger));
```

## 🛡️ Recommended Practices

- Run `Reporter` at app startup to record runtime environment
- Use SLF4J filtering to isolate diagnostic logs
- Avoid using this in performance-sensitive contexts
- Mask logs that may contain secrets
