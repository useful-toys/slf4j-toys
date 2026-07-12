# TDR-0026: Progress Policy (Throttling and Slowness Signaling)

**Status**: Superseded — merged into [TDR-0022](TDR-0022-progress-model-iterations-and-increments.md) and [TDR-0020](TDR-0020-three-outcome-types-ok-reject-fail.md)
**Date**: 2026-01-04
**Updated**: 2026-07-12

## Decision (merged)

This TDR duplicated decisions that are specified in full elsewhere:

* **Time-based throttling and work-based gating of `progress()`** — specified in
  [TDR-0022: Progress Model (Iterations and Increments)](TDR-0022-progress-model-iterations-and-increments.md),
  section "Progress emission is explicit (no background timer)".
* **Slowness signaling relative to `limitMilliseconds(...)`** (slow OK at WARN, slow-progress markers) —
  specified in [TDR-0020: Three Outcome Types (OK, REJECT, FAIL)](TDR-0020-three-outcome-types-ok-reject-fail.md),
  section "Time limits and slow signaling".

This file is retained only to preserve the TDR numbering and inbound links; it records no decision of its own.
