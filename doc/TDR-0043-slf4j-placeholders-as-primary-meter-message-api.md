# TDR-0043: slf4j `{}` Placeholders as the Primary Meter Message API

**Status**: Accepted
**Date**: 2026-07-12

## Context

`Meter.m(String, Object...)` and `MeterContext.ctx(String, String, Object...)` let application code
attach a formatted, human-readable message to an operation or its context. Until this change, both
overloads formatted their argument with `java.util.Formatter` (`String.format(format, args)`):

- `MeterValidator.validateMCallArgument(meter, format, args)` (backing `Meter.m`)
- `MeterContext.ctx(name, format, args)` (its own inline `String.format` call)

This carried two costs, one already flagged by an independent Meter CPU/GC performance review
(finding PERF-010, "eager `printf` formatting in `m(format, args)`"):

- **`Formatter` overhead.** Every call parses the format string, allocates a `Formatter`, autoboxes its
  arguments, and — unless a `Locale` is passed explicitly — performs a JVM-default-locale lookup. This
  runs eagerly, even when the log line that would consume the message is disabled.
- **A locale-dependent library surface.** [TDR-0039](TDR-0039-accept-scaled-integer-rounding-in-json5-serialization.md)
  and [TDR-0040](TDR-0040-locale-independent-readable-messages.md) already removed locale dependence
  from the JSON5 and readable-message paths (`UnitFormatter`, `MeterDataFormatter`,
  `WatcherDataFormatter`). `m`/`ctx` were the last remaining locale-sensitive operations in the `Meter`
  subsystem: `%f`/`%e`/`%g` conversions render with the JVM default locale's decimal separator unless the
  caller remembers to pass one.

A third, independent concern surfaced during review: **static analysis blind spot.** SAST/lint tools
that flag malformed `printf`-style calls (Sonar `java:S2275`, IntelliJ's `MalformedFormatString`, Error
Prone's `FormatString`) are wired to a fixed list of well-known methods (`String.format`, `printf`,
`Formatter.format`, ...). A project-defined `Meter.m(String, Object...)` is invisible to them — a
malformed `m("%d", "text")` call compiles, is not statically flagged, and is only caught at runtime by
`MeterValidator`'s own defensive `try/catch`, which logs a warning instead of failing the build. For a
diagnostics library, silently swallowing a caller's formatting mistake at runtime is a worse outcome
than a build-time warning.

slf4j itself defines an idiomatic, `Formatter`-free alternative: `{}`-placeholder substitution via
`org.slf4j.helpers.MessageFormatter`, the same mechanism backing every `logger.info("... {}", arg)`
call. It never throws (unmatched placeholders or excess arguments are left as-is / ignored), fitting the
non-throwing philosophy of [TDR-0017](TDR-0017-non-intrusive-validation-and-error-handling.md).

## Decision

**`m`/`ctx` are redefined to use slf4j `{}` placeholders. The previous `printf` behavior is kept, renamed
to `mf`/`ctxf`, and pinned to `Locale.ROOT`.** This is a deliberate, breaking API change, shipped in a
major release:

- **`Meter.m(String format, Object... args)`** — now substitutes `{}` placeholders via
  `MessageFormatter.arrayFormat(format, args).getMessage()`. No `Formatter`, no locale lookup, no
  exception path to guard (`MessageFormatter` never throws for a malformed pattern).
- **`Meter.mf(String format, Object... args)`** (new name for the previous `m` overload) — keeps
  `printf`-style formatting via `String.format(Locale.ROOT, format, args)`. The `Formatter` cost is
  accepted deliberately for callers who need `printf` features (`%.2f`, width/padding) that `{}`
  substitution cannot express; `Locale.ROOT` makes the one remaining locale-sensitive call
  deterministic instead of environment-dependent.
- **`MeterContext.ctx(String name, String format, Object... args)`** and
  **`MeterContext.ctxf(String name, String format, Object... args)`** mirror the same split.
- `mf`/`ctxf` still catch `IllegalFormatException` and log `INVALID_ARGUMENT` (the non-intrusive
  contract from TDR-0017); `{}`-based `m`/`ctx` have no such catch because `MessageFormatter` has no
  corresponding failure mode.

Both `MessageFormatter.arrayFormat` (not the slf4j-2.0-only `basicArrayFormat`) is used specifically so
the implementation works unmodified under both slf4j versions this project supports
([TDR-0010](TDR-0010-simultaneous-support-for-slf4j-1.7-2.0-and-logback-1.2-1.5.md)). No new dependency
is introduced — `slf4j-api` already provides `org.slf4j.helpers.MessageFormatter`.

## Consequences

### Positive ✅

- **`Meter`/`MeterContext` no longer read the JVM default locale anywhere** on the primary `m`/`ctx`
  path — this closes the last locale-dependent gap left after TDR-0039/TDR-0040, and even the `mf`/`ctxf`
  fallback is now deterministic via `Locale.ROOT`.
- **Cheaper default path.** `{}` substitution is a single linear scan with no format-string parsing, no
  `Formatter`/`Matcher` allocation, and no locale lookup — strictly cheaper than the previous `printf`
  default, addressing PERF-010 for the common case.
- **Static-analysis visibility restored for callers who need `printf`.** A caller who genuinely needs
  `%.2f`-style formatting now writes `mf(...)`, a name distinct enough that reviewers recognize it as
  `printf`-flavored; callers who migrate their `String.format` call to the call site itself (rather than
  inline it into `mf`) get full SAST/lint coverage, something the previous single `m` overload could
  never offer.
- **Consistent with the slf4j ecosystem.** Application code already writes `logger.info("... {} ...",
  arg)` throughout; `meter.m("... {} ...", arg)` now follows the same convention instead of a
  competing one.

### Negative ❌

- **Breaking change.** Any existing `m(format, args)` / `ctx(name, format, args)` call using `%`
  conversions silently changes behavior — the `%` sequence is no longer substituted and appears literally
  in the output, without a compile error (same method signature, `String, Object...`). This is
  acceptable only because it ships as part of a major version bump, with the change called out
  prominently in release notes; callers must audit and migrate call sites, either to `{}` placeholders
  under `m`/`ctx` or to the equivalent `mf`/`ctxf` call.
- **`{}` substitution cannot express `printf` features.** Width, padding, precision, and type-specific
  conversions (`%.2f`, `%05d`) have no `{}` equivalent; callers needing them must use `mf`/`ctxf`, which
  keeps the `Formatter` cost and the locale-pinning caveat below.
- **The SAST blind spot is not fully closed.** `MessageFormatter.arrayFormat` is just as invisible to
  static analyzers as the previous custom overload was; the mitigation is only that `mf`/`ctxf` are more
  legible as an intentional `printf` escape hatch, not a structural fix.
- **`mf`/`ctxf` output no longer respects the JVM default locale**, same tradeoff already accepted for
  `UnitFormatter` in TDR-0040 — e.g. `mf("%.2f", 3.14)` always renders `3.14`, never `3,14`, regardless of
  the running JVM's locale.

### Neutral ⚖️

- `MeterContext.ctxf`'s error path now stores `IllegalFormatException.getMessage()` instead of
  `getLocalizedMessage()` — a small, deliberate consistency fix so even the failure text does not depend
  on locale.

## Alternatives Considered

### ❌ Keep `m`/`ctx` as `printf`, add `Locale.ROOT` only (no renaming, no `{}` overload)

**Description**: Pin the existing `printf` overloads to `Locale.ROOT` and stop there — no new methods,
no breaking change.

**Why rejected**: Removes the locale dependency but keeps the `Formatter` cost on the default path and
leaves the SAST blind spot exactly as it was — the project decided the idiomatic slf4j `{}` convention
and the performance win were worth a deliberate breaking change, given a major release was already
planned.

### ❌ Deprecate the `printf` overloads instead of renaming them

**Description**: Keep `m`/`ctx` as `printf`-based (with `Locale.ROOT`), mark them
`@Deprecated(forRemoval = true)`, and introduce the `{}` variant under new names (e.g. `mf`/`ctxf` for
the *new* behavior).

**Why rejected**: Considered and superseded by the maintainer's explicit decision to make the breaking
change immediately, since a major release was already in scope — a deprecation cycle would only delay
the ecosystem-consistency and performance benefits without avoiding a breaking change (the method names
still change meaning or move).

## Implementation

- `src/main/java/org/usefultoys/slf4j/meter/MeterValidator.java` — `validateMCallArgument(meter, format,
  args)` reimplemented on `MessageFormatter.arrayFormat`; the previous `printf` implementation kept as
  `validateMfCallArgument(meter, format, args)`, pinned to `Locale.ROOT`.
- `src/main/java/org/usefultoys/slf4j/meter/Meter.java` — `m(String, Object...)` now `{}`-based; `printf`
  behavior moved to new `mf(String, Object...)`.
- `src/main/java/org/usefultoys/slf4j/meter/MeterContext.java` — same split for `ctx`/`ctxf`.
- Existing tests migrated call-by-call: incidental `%s`/`%d` usage converted to `{}` under `m`/`ctx`;
  tests specifically exercising `printf` semantics (illegal format detection, mismatched specifiers)
  moved to `mf`/`ctxf`. New tests added for the `{}` semantics of `m`/`ctx` (`MeterValidatorTest`'s
  `PlaceholderMessageArgumentsTests`, `MeterContextTest`'s `PlaceholderStrings`).

## References

- [TDR-0017: Non-Intrusive Validation and Error Handling](TDR-0017-non-intrusive-validation-and-error-handling.md)
- [TDR-0039: Accept Scaled-Integer Rounding in JSON5 Serialization](TDR-0039-accept-scaled-integer-rounding-in-json5-serialization.md)
- [TDR-0040: Locale-Independent Formatting for Human-Readable Messages](TDR-0040-locale-independent-readable-messages.md)
- [TDR-0010: Simultaneous Support for slf4j 1.7/2.0 and Logback 1.2/1.5](TDR-0010-simultaneous-support-for-slf4j-1.7-2.0-and-logback-1.2-1.5.md)
- [src/main/java/org/usefultoys/slf4j/meter/MeterValidator.java](../src/main/java/org/usefultoys/slf4j/meter/MeterValidator.java)
- [src/main/java/org/usefultoys/slf4j/meter/Meter.java](../src/main/java/org/usefultoys/slf4j/meter/Meter.java)
- [src/main/java/org/usefultoys/slf4j/meter/MeterContext.java](../src/main/java/org/usefultoys/slf4j/meter/MeterContext.java)
