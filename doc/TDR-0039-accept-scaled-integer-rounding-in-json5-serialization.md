# TDR-0039: Accept Scaled-Integer Rounding in JSON5 Serialization

**Status**: Accepted  
**Date**: 2026-07-11

## Context

The JSON5 serializers (`MeterDataJson5`, `SystemDataJson5`, `EventDataJson5`) sit on the hot path of `Meter` and `Watcher` instrumentation: every start/progress/stop message and every watcher snapshot serializes its data through them. Profiling and JMH benchmarks showed that `String.format` calls dominated the serialization cost — each call parses the format string, allocates a `Formatter`, and autoboxes primitive arguments.

All `String.format` calls were replaced with direct `StringBuilder.append` calls. For integral values (`%d`) and plain strings (`%s`) the replacement is behavior-preserving and locale-independent by construction. Decimal values, however, require a rounding strategy. The current instance is `systemLoad`, previously formatted with `String.format(Locale.US, "%.1f", ...)` and now formatted via scaled-integer arithmetic:

```java
final long scaled = Math.round(data.systemLoad * 10);
sb.append(',').append(PROP_SYSTEM_LOAD).append(':').append(scaled / 10).append('.').append(scaled % 10);
```

The two approaches round differently in rare tie cases. `String.format("%.1f")` applies `HALF_UP` to the exact decimal expansion of the binary `double`, while `Math.round(x * 10)` first multiplies (which may round the intermediate product up to a representable double) and then applies `floor(x + 0.5)`. For example, `0.35` (stored as `0.34999999999999997…`) formats as `"0.3"` with `%.1f` but as `"0.4"` with the scaled-integer approach, because `0.35 * 10` rounds to exactly `3.5` in double arithmetic.

## Decision

Adopt scaled-integer formatting as the general strategy for **all** decimal values serialized to JSON5, current and future, and accept its small rounding differences. Performance takes priority over exact decimal-formatting semantics.

The project prioritizes:

*   **Performance**: JSON5 serialization runs on every instrumentation event. Integer divisions and character appends are substantially cheaper than a `Formatter` round-trip, and the code stays allocation-free. This priority applies to every rounding performed by the serializers, not just the current `systemLoad` case.
*   **Minimalism**: reproducing exact `HALF_UP`-on-decimal-expansion semantics would require `BigDecimal`, a hand-written decimal expansion, or keeping `String.format` — all disproportionate to the value at stake ([TDR-0004](./TDR-0004-minimalist-manual-json-implementation.md)).
*   **Fitness for purpose**: the serialized values are coarse diagnostic metrics. A one-unit difference in the last emitted decimal carries no analytical meaning; it is within the intrinsic noise of the metrics.

Overflow of the scaled-integer arithmetic is prevented **at the source, not in the serializer**: values are normalized to a bounded range when collected, so the serializer needs no defensive code on the hot path. For `systemLoad`, `SystemMetricsCollector` guarantees the [0, 1] range — the primary source (`getSystemCpuLoad()`) is bounded by contract, and the fallback (`getSystemLoadAverage() / availableProcessors`) is clamped at 1.0 (full load). Any future decimal field must likewise establish a bounded range at collection time.

The output remains locale-independent (always `'.'` as decimal separator) and remains parseable by the existing `read` regexes, so serialization/deserialization round-trips are unaffected.

## Consequences

**Positive**:

*   **No `Formatter` overhead**: no format-string parsing, no `Formatter` allocation, no autoboxing for decimal fields.
*   **Locale safety by construction**: integer appends cannot be affected by the default locale, removing the need for `Locale.US`.
*   **No defensive code on the hot path**: bounded ranges are guaranteed at collection time, so serializers stay branch-minimal.
*   **Simple, auditable code**: the rounding behavior is fully visible at the call site.
*   **Consistent `systemLoad` semantics**: both collection paths now report a normalized [0, 1] value; readable formatters never display loads above 100%.

**Negative / Accepted Risk**:

*   Values whose decimal expansion lies extremely close to a rounding tie (e.g. `0.35` at one decimal) may format one unit higher or lower in the last decimal than `String.format` would produce. This is accepted as measurement noise for diagnostic metrics, and the acceptance applies to all current and future roundings in the JSON5 serializers.
*   An oversubscribed CPU (load average above the processor count) is reported as exactly 100% load on the fallback path; the degree of oversubscription is not preserved. This matches the primary source, which never reports above 100% either.
*   The scaled-integer approach hardcodes the number of decimal places at the call site (scale factor `10` for one decimal). Changing the precision of a field requires revisiting the arithmetic, not just a format string.

## Alternatives Considered

*   **Keep `String.format(Locale.US, ...)` for decimal fields only**: rejected; it retains the `Formatter` overhead that this change set out to remove, for fields emitted on every watcher snapshot.
*   **`BigDecimal.setScale(1, RoundingMode.HALF_UP)`**: rejected; allocates several objects per call and is slower than `String.format`, contradicting the performance priority.
*   **Hand-written decimal expansion matching `%.1f` semantics**: rejected; significant complexity and audit burden to preserve a distinction without diagnostic meaning.
*   **Defensive clamp in the serializer (e.g. `Math.min` on the scaled value)**: rejected; it adds a per-event operation to handle values that a correctly bounded collector never produces. Normalizing once at collection time is cheaper and also fixes the inconsistency between the two `systemLoad` collection paths.

## Implementation

*   `src/main/java/org/usefultoys/slf4j/internal/SystemDataJson5.java` — scaled-integer formatting in `write`, with a comment at the call site referencing this TDR.
*   `src/main/java/org/usefultoys/slf4j/internal/SystemMetricsCollector.java` — fallback path clamps `loadAverage / availableProcessors` at 1.0.
*   `src/main/java/org/usefultoys/slf4j/internal/SystemData.java` — Javadoc documents the [0, 1] range of `systemLoad`.
*   `src/test/java/org/usefultoys/slf4j/internal/SystemMetricsCollectorTest.java` — test for the oversubscription clamp.
*   `src/test/java/org/usefultoys/slf4j/internal/SystemDataJson5Test.java` — existing tests verify the `'.'` decimal separator and serialization round-trips.

## References

*   [src/main/java/org/usefultoys/slf4j/internal/SystemDataJson5.java](../src/main/java/org/usefultoys/slf4j/internal/SystemDataJson5.java)
*   [src/main/java/org/usefultoys/slf4j/internal/SystemMetricsCollector.java](../src/main/java/org/usefultoys/slf4j/internal/SystemMetricsCollector.java)
*   [doc/TDR-0004-minimalist-manual-json-implementation.md](./TDR-0004-minimalist-manual-json-implementation.md)
*   [doc/TDR-0018-portable-access-to-platform-specific-metrics.md](./TDR-0018-portable-access-to-platform-specific-metrics.md)
