# slf4j-toys JMH Benchmarks

Microbenchmarks that measure the CPU and memory overhead of `Meter` and `Watcher`.
Their purpose is practical: **let you prove whether a code change to slf4j-toys actually
made instrumentation cheaper** — before and after an optimization — instead of guessing.

This module is deliberately **not** part of the Maven reactor. It is developer tooling:
it is never built by the default build, never published, and never puts JMH on the
classpath of the released artifact. It targets JDK 21 and does not keep Java 8 compatibility.

---

## Part 1 — The short, didactic version

### What a benchmark here actually does

Every benchmark repeatedly executes one small slf4j-toys operation (say
`meter.start().ok()`) millions of times and reports **how long one execution takes on
average** (nanoseconds per operation) and, optionally, **how many bytes one execution
allocates**. JMH (the Java Microbenchmark Harness) takes care of the hard parts: warming
up the JIT compiler, running several independent JVMs ("forks"), and reporting a
confidence interval so you know how much to trust the number.

### The one mental model to keep

A raw number like "1,130 ns/op" means little on its own. Two comparisons make it useful:

1. **Against a baseline without the Meter** — so you can say "the Meter adds X% to an
   operation of this size". That is why the suite always measures the *same* synthetic
   operation both with and without a Meter around it.
2. **Against the same benchmark before your change** — so you can say "my optimization
   removed Y ns and Z bytes per call". That is the before/after workflow, and it is the
   whole point of this module.

### Your first measurement, end to end

From the repository root (or the worktree whose library you want to measure):

```powershell
# 1. Install the library SNAPSHOT this module measures (redo after any library change).
.\mvnw -DskipTests install

# 2. Build the runnable benchmark jar.
.\mvnw -f benchmarks\pom.xml clean package

# 3. Run one benchmark quickly, just to see it work.
#    Use JDK 21 explicitly: the jar is Java-21 bytecode and your PATH `java` may be older.
& "$env:JAVA_HOME\bin\java" -jar benchmarks\target\benchmarks.jar `
    "MeterOperationBenchmark.startOk" -p load=0 -p logging=OFF -f 1 -wi 1 -i 1
```

You will see a table ending with something like:

```
Benchmark                        (load)  (logging)  Mode  Cnt   Score   Error  Units
MeterOperationBenchmark.startOk       0        OFF  avgt    1  1130,4          ns/op
```

`Score` is the average time per operation. That is a measurement. The `-f 1 -wi 1 -i 1`
flags made it fast but noisy — fine to confirm things run, **not** fine to make a
decision. For decisions, drop those flags and let each class use its configured
forks/iterations, and add the GC profiler (Part 2).

### The four ideas the suite is built on

- **Wrap a real operation.** A Meter is always measured around a synthetic operation of
  controlled cost (`Blackhole.consumeCPU(load)`), because in production it always wraps
  real work. `load = 0` recovers the pure Meter overhead as a special case.
- **Measure two things.** Time (ns/op) *and* allocation (bytes/op, via `-prof gc`). The
  allocation number has almost no variance, so it is the best signal for small wins.
- **Logging dominates.** With logging off you measure the instrumentation floor; with it
  on you also measure message/JSON construction — usually the biggest cost, and where most
  optimizations live. The suite parameterizes this.
- **First use ≠ steady state.** The first time a category is used is far more expensive
  than later uses. That needs its own benchmark mode (`SingleShotTime`).

---

## Part 2 — The detailed version

### Prerequisites

- **JDK 21** (matches the repository build JDK). Check `echo $env:JAVA_HOME` points to a 21.
- The library **SNAPSHOT installed into the local Maven repository**. The benchmark POM
  depends on `org.usefultoys:slf4j-toys:<version>-SNAPSHOT`; it is resolved from `~/.m2`,
  **not** rebuilt automatically. Any time you change library code you want to measure, run
  `.\mvnw -DskipTests install` again, then repackage the benchmarks. Forgetting this is the
  most common way to measure the wrong code.

### Build

```powershell
.\mvnw -f benchmarks\pom.xml clean package
```

The Shade plugin produces a self-contained, executable jar at
`benchmarks\target\benchmarks.jar` (main class `org.openjdk.jmh.Main`).

### Run: invocation and selectors

Always launch with JDK 21:

```powershell
& "$env:JAVA_HOME\bin\java" -jar benchmarks\target\benchmarks.jar [regex] [options]
```

| Goal | Example |
|------|---------|
| List every benchmark | `... benchmarks.jar -l` |
| JMH help (all options) | `... benchmarks.jar -h` |
| Run one class | `... benchmarks.jar "WatcherBenchmark"` |
| Run one method | `... benchmarks.jar "MeterOperationBenchmark.startOk"` |
| Run several by regex | `... benchmarks.jar "MeterLifecycleBenchmark.(createStartOk\|okWithContext)"` |
| Pick parameter values | `... benchmarks.jar "MeterOperationBenchmark" -p load=0,500 -p logging=OFF,MESSAGE_DATA` |

If you omit `-p`, JMH runs **all** declared values of that `@Param`, i.e. the full cross
product. That is thorough but slow — narrow it while iterating.

### Run: the flags that control quality

| Flag | Meaning | Smoke value | Decision value |
|------|---------|-------------|----------------|
| `-f` | forks (independent JVMs) | `1` | `3`–`5` |
| `-wi` | warmup iterations | `1` | `5`–`10` |
| `-i` | measurement iterations | `1` | `5`–`10` (`20`+ for SingleShot) |
| `-w` / `-r` | warmup / measurement time each | `400ms` | `1s` (default) |
| `-t` | threads | `1` | `4`/`8` to expose contention |

The classes already declare sensible decision-level defaults (`@Fork`, `@Warmup`,
`@Measurement`), so for a real run you can pass **no** timing flags at all and only select
what to run. Pass the smoke values only to confirm a benchmark executes.

**Multiple forks matter most.** Run-to-run JIT differences are the largest source of
false positives; a single fork cannot see them. Never decide on `-f 1`.

### Measure memory, not just time

```powershell
& "$env:JAVA_HOME\bin\java" -jar benchmarks\target\benchmarks.jar "MeterOperationBenchmark" -prof gc
```

This adds allocation columns. The important one is **`gc.alloc.rate.norm`** — **bytes
allocated per operation**. It is essentially deterministic (variance ~0), so it exposes
allocation changes (a removed `String.format`, an eliminated `WeakReference`) long before
they show up in the timing. `consumeCPU` allocates nothing, so any bytes/op reported are
attributable to slf4j-toys itself.

### Reading the output

```
Benchmark                        (load)  (logging)  Mode  Cnt   Score   Error  Units
MeterOperationBenchmark.startOk       0        OFF  avgt   15  1130,4 ± 42,1  ns/op
```

- **Score** — the average (here, ns/op; lower is better).
- **Error** — the half-width of the 99.9% confidence interval.
- **Cnt** — number of measured data points (forks × iterations).
- A difference between two runs is only **real** when the `Score ± Error` intervals do
  **not** overlap. If they overlap, you measured noise.

Three ways to turn scores into an answer:

- **Absolute overhead** — read `Score` directly at `load = 0`, `logging = OFF`
  (`MeterOperationBenchmark` / `MeterLifecycleBenchmark.createStartOk`).
- **Relative overhead** — subtract the no-Meter baseline at the *same* load:
  `MeterOperationBenchmark.startOk[load=L]` − `OperationBaselineBenchmark.operationOnly[load=L]`.
- **Per-phase cost** — subtract neighbouring lifecycle scenarios:
  `MeterLifecycleBenchmark.okWithContext` − `MeterLifecycleBenchmark.createStartOk` = cost of `ctx(..)`.

### The before/after workflow (the main use case)

Goal: prove an optimization to the library is a real improvement.

```powershell
# ---------- ANTES / BEFORE: the library without the change ----------
.\mvnw -DskipTests install                       # install the "before" library
.\mvnw -f benchmarks\pom.xml clean package
& "$env:JAVA_HOME\bin\java" -jar benchmarks\target\benchmarks.jar `
    "MeterOperationBenchmark|MeterLifecycleBenchmark" `
    -prof gc -rf json -rff before.json

# ---------- apply the optimization to the library, then ----------
# ---------- DEPOIS / AFTER: the library with the change ----------
.\mvnw -DskipTests install                       # reinstall the "after" library
.\mvnw -f benchmarks\pom.xml clean package
& "$env:JAVA_HOME\bin\java" -jar benchmarks\target\benchmarks.jar `
    "MeterOperationBenchmark|MeterLifecycleBenchmark" `
    -prof gc -rf json -rff after.json
```

`-rf json -rff <file>` writes machine-readable results. Compare `before.json` and
`after.json` method by method, on both `Score` and `gc.alloc.rate.norm`. Declare a win
only when the change exceeds the error margins on both sides.

**Critical rule:** the benchmark code must be **identical** on both sides — only the
library may differ. So when comparing an optimization branch (e.g.
`perf/meter-string-format`) against `main`, put that branch on the same base that carries
these benchmarks (rebase it), install each state in turn, and reuse this exact jar.

### Environment hygiene

- Fix the JDK and the machine; close heavy applications.
- If possible, disable turbo boost / CPU frequency scaling for stable numbers.
- Do not trust a single run; rely on forks and confidence intervals.
- JMH may print a note that Compiler Blackholes are experimental — keep the JVM consistent
  across the runs you compare so the Blackhole mode is the same on both sides.

### The suite at a glance

| Class | Question it answers | Axes (`@Param`) | Mode |
|-------|---------------------|-----------------|------|
| `OperationBaselineBenchmark` | reference cost with **no** Meter (`empty`, `nanoTimeX2`, `operationOnly`) | `load` | AverageTime |
| `MeterOperationBenchmark` | overhead of a Meter **around an operation** (`startOk`/`startFail`/`startReject`) | `load` × `logging` | AverageTime |
| `MeterLifecycleBenchmark` | cost of **each phase** by difference (create, start, ok, `m`/`ctx`/`path`/`iterations`, `close`, `sub`) | `logging` | AverageTime |
| `MeterColdStartBenchmark` | **first** use of a category vs. every subsequent use | — | SingleShotTime |
| `WatcherBenchmark` | Watcher **construction** vs. `run()` | `logging` (all 4) | AverageTime |
| `MeterStartOkBenchmark`, `WatcherRunBenchmark` | original minimal skeletons (floor only) | — | AverageTime |

### The `load` axis

`load` is the token count passed to `Blackhole.consumeCPU(load)`, a controlled amount of
CPU work standing in for a production operation. It is not calibrated to a fixed number of
nanoseconds, but it spans the spectrum you care about:

| `load` | Represents |
|--------|------------|
| `0` | pure Meter overhead (no operation) |
| `50` | a trivial in-memory call |
| `500` | a typical business method |
| `5000` | an I/O-bound operation (query, remote call) |

Reading Meter overhead as a function of `load` answers "how much does the Meter interfere
with a real operation" honestly: heavy on tiny operations, negligible on large ones.

### The `logging` axis and a Meter/Watcher asymmetry

`LoggingScenario` gives message and JSON-data output **separate loggers** (distinct names
via `MeterConfig`/`WatcherConfig` suffixes) and routes enabled output to a discarding
appender, so the measured cost is the **string construction inside slf4j-toys**, not
logback's encoder or disk I/O. The regimes:

- `OFF` — both loggers off: the instrumentation floor.
- `MESSAGE` — human-readable line built and emitted; JSON off.
- `MESSAGE_DATA` — both the human-readable line and the JSON5 data line built and emitted.
- `DATA_ONLY` — message off, data on.

Reachability differs between the components:

- **Meter** nests the data statement inside the message-level guard, so `DATA_ONLY` emits
  nothing and equals `OFF`. Only `OFF` / `MESSAGE` / `MESSAGE_DATA` are meaningful;
  `DATA_ONLY` exists only to confirm that equivalence.
- **Watcher** emits the two lines independently (`run()` collects when
  `isInfoEnabled() || isTraceEnabled()`), so `DATA_ONLY` **is** a distinct, reachable
  regime. All four apply.

### Why `MeterColdStartBenchmark` uses SingleShotTime

The first use of a category pays a one-time cost inside the Meter constructor: resolving
the message/data loggers (`LoggerFactory.getLogger`) and registering the category in
`EVENT_COUNTER` (`putIfAbsent(new AtomicLong(0))`). AverageTime would warm those caches
during warmup and erase the effect. So the `first*` methods use a brand-new, never-seen
category on **every** measured invocation (a monotonic counter), while `warm*` reuse a
fixed category; `first* − warm*` is the first-use registration overhead. Warmup iterations
still run, but they only warm the JIT — each measured shot stays cold at the category
level. SingleShotTime is inherently noisy (large error bars); use its configured
higher iteration counts and read it qualitatively.

### A note on Meter reuse

A `Meter` is single-use: after a terminal call (`ok`/`fail`/`reject`/`close`) it is
finished. Benchmarks therefore build a **fresh Meter per invocation** rather than reusing
one instance — reusing a finished Meter would measure the "already stopped" inconsistency
branch instead of the real lifecycle. A `Watcher`, by contrast, is designed to run
repeatedly, so `WatcherBenchmark.run` reuses one prepared instance on purpose.
