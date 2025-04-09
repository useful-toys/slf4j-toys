
# 📊 `org.usefultoys.slf4j.report`

A lightweight diagnostic logging toolkit for Java applications. Useful for logging detailed system environment reports at startup, debugging, or during runtime analysis.

---

## 📦 Main Classes

### 🧰 `Reporter`

Generates and logs detailed system reports such as JVM info, OS details, memory, locale, network interfaces, SSL contexts, and more.

#### 🔹 Usage

```java
Reporter reporter = new Reporter(); // Uses default logger
reporter.logAllReports(Reporter.sameThreadExecutor); // Run all reports synchronously
```

#### 🔹 Run only configured reports

```java
reporter.logDefaultReports(Reporter.sameThreadExecutor);
```

---

### ⚙️ `ReporterConfig`

Holds static flags that control which reports the `Reporter` should run.

#### 🔹 Example

```java
ReporterConfig.reportVM = true;
ReporterConfig.reportMemory = false;
```

You can also configure them via system properties (e.g., `-Dslf4jtoys.report.vm=false`).

---

### 🌐 `ReportServlet`

A `HttpServlet` that listens to HTTP GET requests and triggers system reports based on the requested path.

#### 🔹 Supported paths

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

#### 🔹 Example Request

```http
GET /myapp/report/VM
```

Logs JVM information to SLF4J.

#### ⚠️ Security Notes

- ⚠️ **Do not expose** this servlet in production without:
  - authentication
  - IP filtering
  - rate limiting
- ⚠️ Reports may leak sensitive data (env vars, tokens, passwords)

---

### 🪵 `JdbcConnectionReporter`

Logs structured details about a JDBC `Connection`.

#### 🔹 Example

```java
Logger logger = LoggerFactory.getLogger("diagnostics");
JdbcConnectionReporter reporter = new JdbcConnectionReporter(logger)
    .printTypeMap(true);
reporter.run(connection);
```

---

## 🧩 Built-in Report Modules

Each is an inner class of `Reporter`, implementing `Runnable`. You can trigger them manually if needed:

| Class                      | Description                                  |
|---------------------------|----------------------------------------------|
| `ReportVM`                | JVM vendor, version, home                    |
| `ReportFileSystem`        | Disk usage info                              |
| `ReportMemory`            | Heap and memory usage                        |
| `ReportUser`              | Current user and home directory              |
| `ReportPhysicalSystem`    | CPU count                                    |
| `ReportOperatingSystem`   | OS name, version, separators                 |
| `ReportCalendar`          | Date/time and timezone                       |
| `ReportLocale`            | Locale info and available locales           |
| `ReportCharset`           | Charset info and supported charsets         |
| `ReportNetworkInterface`  | IPs, flags, MACs for each NIC                |
| `ReportSSLContext`        | SSL/TLS config details                       |
| `ReportDefaultTrustKeyStore` | Lists trusted certs from default store   |

---

## 🛡️ Recommended Practices

- Run `Reporter` at app startup to record runtime environment
- Use SLF4J filtering to isolate diagnostic logs
- Avoid using this in performance-sensitive contexts
- Mask logs that may contain secrets
