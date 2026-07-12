# TDR-0040: Locale-Independent Formatting for Human-Readable Messages

**Status**: Accepted  
**Date**: 2026-07-11

## Context

The human-readable log messages of `Meter` and `Watcher` (start/progress/stop lines and watcher
snapshots) are built on the instrumentation hot path: every lifecycle event formats timing, throughput,
iteration counts and memory sizes for humans. All of these funnel through `UnitFormatter`, the single
formatting helper shared by `MeterDataFormatter`, `WatcherDataFormatter` and the `report` package.

Profiling of the readable path showed two costs concentrated in `UnitFormatter`:

- Decimal values were rendered with `String.format(SessionConfig.locale, "%.1f%s", ...)`. Each call parses
  the format string, allocates a `Formatter`, autoboxes its arguments, and performs a locale lookup.
- Every `UnitFormatter` method returned a `String` that the caller then copied into its `StringBuilder` —
  an intermediate allocation per formatted value, on top of the `Formatter` cost.

The sibling JSON5 serialization path already removed the equivalent `String.format` cost via scaled-integer
arithmetic (see [TDR-0039](./TDR-0039-accept-scaled-integer-rounding-in-json5-serialization.md)), but that
decision was scoped to the machine-readable JSON5 output, which is locale-independent by requirement. The
readable path additionally consulted `SessionConfig.locale`, so the decimal separator (e.g. `','` under a
German or Brazilian locale) was locale-dependent.

## Decision

Human-readable messages no longer use the locale. `UnitFormatter` stops consulting `SessionConfig.locale`
and always formats decimals in an arbitrarily chosen US style: a fixed `'.'` decimal separator produced by
scaled-integer arithmetic, extending the [TDR-0039](./TDR-0039-accept-scaled-integer-rounding-in-json5-serialization.md)
strategy from JSON5 to the readable path.

```java
// Locale-independent, one decimal place, no Formatter. See TDR-0039 / TDR-0040.
final long scaled = Math.round(value * 10);
sb.append(scaled / 10).append('.').append(scaled % 10).append(unit);
```

We prioritize **speed and allocation** over **internationalization**. slf4j-toys is a
diagnostic/instrumentation tool: a single, standard numeric format is sufficient for reading diagnostic
output, and locale-specific number formatting carries no analytical value while costing a `Formatter`
round-trip and a locale lookup on every instrumentation event.

To also remove the intermediate `String`, `UnitFormatter` gains `append*(StringBuilder, ...)` variants
(`appendBytes`, `appendNanoseconds`, `appendIterations`, `appendIterationsPerSecond`) that write directly
into the caller's buffer. `MeterDataFormatter` and `WatcherDataFormatter` use these on the hot path. The
existing `String`-returning methods (`bytes`, `nanoseconds`, ...) are kept, delegating to the append core,
so the `report` package and unit tests are unaffected.

The scaled-integer rounding may differ by one unit in the last decimal from `String.format("%.1f")` in rare
tie cases, exactly as accepted in TDR-0039. In practice the intermediate `value * 10` multiplication rounds
ties toward the representable half (e.g. `1.15 * 10` yields `11.5`), so the existing `UnitFormatterTest`
expectations remain satisfied without modification.

## Consequences

### Positive ✅

- **No `Formatter` overhead on the readable path**: no format-string parsing, no `Formatter` allocation, no
  autoboxing, no locale lookup for decimal fields.
- **No intermediate `String`**: the `append*` variants write straight into the message `StringBuilder`,
  removing one allocation per formatted value on every Meter/Watcher event.
- **Consistency with JSON5**: both output paths now share one rounding and separator strategy (scaled-integer,
  `'.'`), making formatting behavior uniform and auditable across the codebase.
- **Simplicity**: dropping locale removes the `DecimalFormatSymbols`/locale-caching machinery that a
  locale-aware manual formatter would otherwise require.

### Negative ❌

- **No internationalization of readable numbers**: users under comma-decimal locales now see `1.2kB` instead
  of `1,2kB`. Accepted deliberately: diagnostic output favors a single standard format over localization.
- **Rounding is not exact `%.1f`**: values whose decimal expansion lies extremely close to a tie may format
  one unit higher or lower in the last decimal. Accepted as diagnostic measurement noise (per TDR-0039).
- **Fixed precision at the call site**: the scale factor `10` (one decimal) is hardcoded; changing precision
  means revisiting the arithmetic rather than a format string.

### Neutral ⚖️

- `SessionConfig.locale` is no longer read by `UnitFormatter`. The configuration field itself is retained,
  since other components may still consult it; pruning it is out of scope for this change.

## Alternatives Considered

### ❌ Keep `String.format(SessionConfig.locale, "%.1f%s", ...)`

**Description**: Preserve locale-aware decimal formatting via `String.format` on the readable path only.

**Why rejected**: Retains exactly the `Formatter` overhead (format-string parsing, allocation, autoboxing,
locale lookup) and the intermediate `String` that this change set out to remove, for a value — localized
decimal separators — that has no diagnostic significance.

### ❌ Locale-aware manual formatting (cached decimal separator)

**Description**: Reproduce the current localized output by fetching the decimal separator from
`DecimalFormatSymbols.getInstance(SessionConfig.locale)`, caching it, and invalidating the cache when the
runtime locale changes.

**Why rejected**: Adds caching and invalidation complexity to preserve a distinction (comma vs. dot) that
carries no analytical value for a diagnostic tool. Contradicts the minimalism the project favors.

### ❌ Epsilon-calibrated rounding to match `%.1f` exactly

**Description**: Add a small relative epsilon before `Math.round` to reproduce `String.format("%.1f")`
tie-breaking on the shortest decimal expansion, keeping output byte-for-byte identical.

**Why rejected**: This is the hand-written decimal-expansion complexity already rejected by TDR-0039, to
preserve a last-digit distinction with no diagnostic meaning. The plain scaled-integer form already satisfies
all existing tests.

## Implementation

- `src/main/java/org/usefultoys/slf4j/utils/UnitFormatter.java` — scaled-integer, locale-independent decimal
  formatting in the append-based `longUnit`/`doubleUnit` engines; new `append*(StringBuilder, ...)` variants;
  `String`-returning methods retained as delegating overloads; `SessionConfig` import removed.
- `src/main/java/org/usefultoys/slf4j/meter/MeterDataFormatter.java` — call sites migrated to `UnitFormatter.append*`.
- `src/main/java/org/usefultoys/slf4j/watcher/WatcherDataFormatter.java` — call sites migrated to `UnitFormatter.append*`.
- `src/test/java/org/usefultoys/slf4j/utils/UnitFormatterTest.java` — existing expectations pass unchanged.

## References

- [TDR-0039: Accept Scaled-Integer Rounding in JSON5 Serialization](./TDR-0039-accept-scaled-integer-rounding-in-json5-serialization.md)
- [TDR-0004: Minimalist Manual JSON Implementation](./TDR-0004-minimalist-manual-json-implementation.md)
- [src/main/java/org/usefultoys/slf4j/utils/UnitFormatter.java](../src/main/java/org/usefultoys/slf4j/utils/UnitFormatter.java)
