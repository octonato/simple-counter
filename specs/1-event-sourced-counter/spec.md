# Feature Specification: Event Sourced Counter

**Feature Branch**: `1-event-sourced-counter`
**Created**: 2026-03-17
**Status**: Draft
**Input**: User description: "Event Sourced Counter with name, increment/decrement, zero floor, and overflow behavior"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Create and Increment a Counter (Priority: P1)

A user creates a new counter by giving it a name and then increments its value. The counter starts at zero and increases by one with each increment request.

**Why this priority**: This is the core functionality — a counter that can be created and incremented. Without this, no other feature is meaningful.

**Independent Test**: Can be fully tested by creating a counter with a name, incrementing it several times, and verifying the current value matches the expected count.

**Acceptance Scenarios**:

1. **Given** no counter exists for a given ID, **When** the user increments it, **Then** the counter is created with value 1 and a default empty name.
2. **Given** a counter with value 0 and name "my-counter", **When** the user increments it 3 times, **Then** the counter value is 3 and the name remains "my-counter".
3. **Given** a counter exists, **When** the user retrieves it, **Then** the current value and name are returned.

---

### User Story 2 - Decrement a Counter with Zero Floor (Priority: P1)

A user decrements a counter's value. The counter must never go below zero — attempting to decrement a counter at zero results in an error or rejection.

**Why this priority**: Decrement with the zero-floor constraint is a core business rule that defines the counter's behavior and must be enforced from the start.

**Independent Test**: Can be fully tested by creating a counter, incrementing it, decrementing it, and verifying the zero-floor boundary is enforced.

**Acceptance Scenarios**:

1. **Given** a counter with value 5, **When** the user decrements it, **Then** the counter value becomes 4.
2. **Given** a counter with value 0, **When** the user decrements it, **Then** the request is rejected with an error indicating the counter cannot go below zero.
3. **Given** a counter with value 1, **When** the user decrements it, **Then** the counter value becomes 0.

---

### User Story 3 - Rename a Counter (Priority: P2)

A user changes the name of an existing counter at any time. The name update does not affect the counter's value.

**Why this priority**: Naming is important for usability but secondary to the core counting functionality.

**Independent Test**: Can be fully tested by creating a counter with a name, changing the name, and verifying the new name is returned while the value remains unchanged.

**Acceptance Scenarios**:

1. **Given** a counter with name "old-name" and value 5, **When** the user changes the name to "new-name", **Then** the counter name is "new-name" and value remains 5.
2. **Given** a counter with an empty name, **When** the user sets the name to "my-counter", **Then** the counter name is "my-counter".

---

### User Story 4 - Overflow on Maximum Value (Priority: P3)

When a counter reaches its maximum value (Integer.MAX_VALUE, i.e., 2,147,483,647), the next increment causes the counter to overflow back to zero.

**Why this priority**: This is an edge case that handles extreme usage. It's important for correctness but unlikely to occur in typical usage.

**Independent Test**: Can be tested by setting a counter to its maximum value and incrementing it, then verifying the value resets to zero.

**Acceptance Scenarios**:

1. **Given** a counter at the maximum integer value (2,147,483,647), **When** the user increments it, **Then** the counter value becomes 0.
2. **Given** a counter that just overflowed to 0, **When** the user increments it again, **Then** the counter value becomes 1 (normal behavior resumes).

---

### Edge Cases

- What happens when a counter is decremented at value 0? The request is rejected with an error.
- What happens when a counter overflows from max value? It resets to 0.
- What happens when a counter name is set to an empty string? This is allowed — names can be empty.
- What happens when multiple rapid increments/decrements are applied? Each operation is processed sequentially per counter, maintaining consistency.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST create counters that start at value zero.
- **FR-002**: System MUST allow counters to have a name that can be set at creation or updated later.
- **FR-003**: System MUST support incrementing a counter's value by 1.
- **FR-004**: System MUST support decrementing a counter's value by 1.
- **FR-005**: System MUST reject decrement operations when the counter value is zero (zero floor constraint).
- **FR-006**: System MUST overflow the counter value to zero when incrementing from the maximum integer value (2,147,483,647).
- **FR-007**: System MUST allow the counter name to be changed at any time without affecting the counter value.
- **FR-008**: System MUST persist all counter state changes as events (event sourced).
- **FR-009**: System MUST allow retrieval of the current counter value and name.

### Key Entities

- **Counter**: Represents a named counter with a current integer value. Key attributes: identifier, name, current value. The counter enforces a zero floor (value >= 0) and overflows to zero at the maximum integer value.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Users can create, increment, decrement, and rename counters through the API with immediate feedback.
- **SC-002**: Counter value never goes below zero — all decrement attempts at zero are rejected.
- **SC-003**: Counter correctly overflows to zero when incremented past maximum value.
- **SC-004**: Counter name can be updated independently of counter value at any time.
- **SC-005**: All counter state changes are persisted and survive service restarts.

## Assumptions

- Counter increment and decrement operations change the value by exactly 1 (not by arbitrary amounts).
- Counter names have no length or character restrictions beyond what serialization supports.
- An empty string is a valid counter name.
- Each counter is identified by a unique ID provided by the caller.
- There is no authentication or authorization required for counter operations.
