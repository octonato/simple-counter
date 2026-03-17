# Research: Event Sourced Counter

**Date**: 2026-03-17

## Technology Stack

- **Decision**: Java 21 with Akka SDK 3.5.16
- **Rationale**: Project already configured with these versions in pom.xml
- **Alternatives considered**: None — project scaffold is pre-configured

## Event Sourced Entity Pattern

- **Decision**: Use Event Sourced Entity (not Key Value Entity)
- **Rationale**: Spec explicitly requires event sourcing (FR-008). Event sourcing provides audit trail of all counter changes and supports event replay.
- **Alternatives considered**: Key Value Entity — simpler but doesn't meet the event sourcing requirement.

## Overflow Behavior

- **Decision**: Handle overflow in domain logic using explicit check against Integer.MAX_VALUE before incrementing
- **Rationale**: Java's default integer overflow wraps to negative values. The spec requires overflow to zero, so we need explicit logic rather than relying on Java arithmetic.
- **Alternatives considered**: Using `Math.addExact()` with exception handling — rejected as less clear than an explicit check.

## Counter Initialization

- **Decision**: Counter starts with value 0 and empty name. `emptyState()` returns a Counter record with these defaults.
- **Rationale**: Spec says counters start at zero (FR-001). Implicit creation on first command (no separate "create" command needed).
- **Alternatives considered**: Explicit create command — rejected as spec doesn't mention one; increment on non-existent counter should just work.

## Error Handling for Zero Floor

- **Decision**: Return `effects().error("Cannot decrement below zero")` when decrement is attempted at value 0
- **Rationale**: Akka SDK pattern for validation errors in command handlers. The spec says "request is rejected with an error."
- **Alternatives considered**: Silently ignoring the decrement — rejected as spec explicitly requires rejection.
