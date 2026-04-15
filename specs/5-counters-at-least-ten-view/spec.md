# Feature Specification: View for Counters with Value >= 10

**Feature Branch**: `claude/write-issue-5-spec-1cMuM`
**Created**: 2026-04-15
**Status**: Draft
**Issue**: octonato/simple-counter#5
**Input**: User description: "Needs a View for counter for counter >= 10 — The view should return the list of counters that are greater or equal to 10"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - List Counters With Value At Least Ten (Priority: P1)

A user queries the system for all counters whose current value is greater than or equal to 10 and receives the list of matching counters (each with its identifier, name and current value).

**Why this priority**: This is the sole functional ask of the issue. Without it, there is no way to discover which counters have crossed the "ten" threshold, and the feature delivers no value.

**Independent Test**: Can be fully tested by creating several counters with varying values (some below 10, some equal to 10, some above 10), querying the view, and verifying the returned list contains exactly the counters whose values are >= 10.

**Acceptance Scenarios**:

1. **Given** counters with values 3, 9, 10, 11 and 42, **When** the user queries the view, **Then** the result contains only the counters with values 10, 11 and 42.
2. **Given** no counters have ever reached a value of 10, **When** the user queries the view, **Then** the result is an empty list.
3. **Given** a counter whose value is 10, **When** the counter is decremented to 9, **Then** it no longer appears in the view results.
4. **Given** a counter whose value is 9, **When** the counter is incremented to 10, **Then** it appears in the view results.

---

### User Story 2 - View Reflects Renames (Priority: P2)

When a counter that qualifies for the view is renamed, the view returns the updated name on the next query.

**Why this priority**: Counters can be renamed at any time (see `specs/1-event-sourced-counter/spec.md`). The view should present the most recent name so callers see consistent information, but the threshold behaviour is the primary concern.

**Independent Test**: Create a counter, increment it to 10 or more, rename it, query the view, and verify the new name is returned.

**Acceptance Scenarios**:

1. **Given** a counter with value 15 and name "old-name" is present in the view, **When** the user renames it to "new-name", **Then** a subsequent query returns "new-name" for that counter.
2. **Given** a counter with value 5 and name "below-threshold", **When** the user renames it to "still-below", **Then** the counter is still not present in the view.

---

### Edge Cases

- **Exactly ten**: A counter whose value is exactly 10 is included in the result (the threshold is inclusive).
- **Overflow to zero**: When a counter overflows from `Long.MAX_VALUE` back to 0, it is removed from the view on the next refresh because 0 < 10.
- **Decrement across the threshold**: A counter decremented from 10 to 9 is removed from the view.
- **Increment across the threshold**: A counter incremented from 9 to 10 is added to the view.
- **Empty result**: If no counters currently satisfy the condition, the view returns an empty list rather than an error.
- **Empty name**: Counters with an empty name are returned as-is; an empty name is a valid name per the existing spec.
- **Eventual consistency**: The view is updated asynchronously from the counter events, so there may be a short delay between a counter crossing the threshold and the view reflecting that change.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST expose a query that returns all counters whose current value is greater than or equal to 10.
- **FR-002**: The query result MUST include, for each matching counter, its identifier, name, and current value.
- **FR-003**: The view MUST include a counter as soon as its value becomes >= 10 as a result of an increment.
- **FR-004**: The view MUST exclude a counter as soon as its value drops below 10 (for example, via decrement or overflow to zero).
- **FR-005**: The view MUST reflect the latest known name of a counter, updating when a counter is renamed.
- **FR-006**: The view MUST return an empty list when no counters satisfy the condition (not an error).
- **FR-007**: The query MUST be reachable via an HTTP endpoint so that existing API clients can consume it using the same mechanism as the other counter operations.
- **FR-008**: The view MUST be built from the existing counter events (`value-incremented`, `value-decremented`, `name-changed`); no new events are required on the counter entity.

### Key Entities

- **Counter (existing)**: Already defined in `specs/1-event-sourced-counter/spec.md`. Identified by a unique ID, has a name and a current value.
- **CountersAtLeastTen (new, read model row)**: One row per counter that currently has a value >= 10. Attributes: counter ID, name, current value. A counter is absent from the read model when its value is below 10.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: A user can retrieve, in a single request, the list of all counters whose value is >= 10.
- **SC-002**: The list contains exactly the counters whose latest persisted value is >= 10 — no false positives (counters below 10) and no false negatives (missing counters above 10), allowing for the view's eventual consistency window.
- **SC-003**: The list reflects the most recent name assigned to each counter.
- **SC-004**: Crossing the threshold in either direction (up or down) causes the next query to include or exclude the counter accordingly.
- **SC-005**: The query returns an empty result (not an error) when no counters satisfy the condition.

## Assumptions

- The threshold "10" is fixed and inclusive; it is not configurable by the caller.
- The view is built from the existing `CounterEntity` event stream and does not require any new events or changes to the counter domain model.
- Eventual consistency between the counter entity and the view is acceptable.
- No pagination is required — the number of counters with value >= 10 is expected to be modest. Pagination can be added later if needed.
- No authentication or authorization is required, consistent with the rest of the service.
- The query is read-only; there is no way to mutate counters through this view.
