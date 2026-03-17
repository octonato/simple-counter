# Data Model: Event Sourced Counter

**Date**: 2026-03-17

## Entities

### Counter

Represents a named counter with an integer value.

| Attribute | Type | Description |
|-----------|------|-------------|
| name | String | Display name of the counter (can be empty) |
| value | int | Current counter value (0 to Integer.MAX_VALUE) |

**Identity**: Counter ID provided by the caller (path parameter in HTTP endpoint).

**Invariants**:
- Value is always >= 0 (zero floor)
- Value is always <= Integer.MAX_VALUE
- Incrementing at MAX_VALUE overflows to 0

**State transitions**:
- Empty → Initialized (on first increment or name change)
- Any value → value + 1 (increment, unless at MAX_VALUE → 0)
- Any value > 0 → value - 1 (decrement)
- Any state → same state with new name (rename)

## Events

### CounterEvent (sealed interface)

| Event | TypeName | Fields | Description |
|-------|----------|--------|-------------|
| ValueIncremented | `value-incremented` | int newValue | Counter was incremented |
| ValueDecremented | `value-decremented` | int newValue | Counter was decremented |
| NameChanged | `name-changed` | String newName | Counter name was updated |
