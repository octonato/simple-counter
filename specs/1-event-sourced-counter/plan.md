# Implementation Plan: Event Sourced Counter

**Branch**: `1-event-sourced-counter` | **Date**: 2026-03-17 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `/specs/1-event-sourced-counter/spec.md`

## Summary

Implement a named, event-sourced counter with increment/decrement operations, a zero floor constraint, and overflow-to-zero behavior at Integer.MAX_VALUE. The implementation uses an Akka Event Sourced Entity with an HTTP endpoint for all operations.

## Technical Context

**Language/Version**: Java 21
**Primary Dependencies**: Akka SDK 3.5.16
**Storage**: Akka event journal (built-in)
**Testing**: JUnit 5, AssertJ, Akka TestKit (EventSourcedTestKit for unit, TestKitSupport for integration)
**Target Platform**: Akka platform (JVM service)
**Project Type**: Web service
**Constraints**: Counter value 0 to Integer.MAX_VALUE; sequential command processing per entity instance (guaranteed by Akka)

## Constitution Check

*No constitution file found. Proceeding with Akka SDK best practices as documented in AGENTS.md.*

- Domain logic in domain package, no Akka dependencies: **PASS**
- Events as sealed interface with @TypeName: **PASS**
- Immutable records for state and events: **PASS**
- Command handlers in entity, business logic in domain: **PASS**
- API-specific types in endpoint, not domain types: **PASS**
- @Acl annotation on endpoint: **PASS**

## Project Structure

### Documentation (this feature)

```text
specs/1-event-sourced-counter/
├── plan.md              # This file
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
├── contracts/
│   └── http-api.md      # Phase 1 output
└── tasks.md             # Phase 2 output (created by /akka.tasks)
```

### Source Code (repository root)

```text
src/main/java/com/example/
├── domain/
│   ├── Counter.java              # State record with business logic
│   └── CounterEvent.java         # Sealed event interface
├── application/
│   └── CounterEntity.java        # Event Sourced Entity
└── api/
    └── CounterEndpoint.java      # HTTP Endpoint

src/test/java/com/example/
├── application/
│   └── CounterEntityTest.java    # Unit tests (EventSourcedTestKit)
└── api/
    └── CounterEndpointIntegrationTest.java  # Integration tests (httpClient)
```

**Structure Decision**: Standard Akka SDK three-layer package structure (domain / application / api). Single entity, single endpoint — no views or workflows needed for this feature.

## Component Design

### Domain Layer

**Counter.java** (state record):
- Fields: `String name`, `int value`
- Methods: `increment()` → returns new Counter (handles overflow to 0), `decrement()` → returns new Counter (throws if value is 0), `withName(String)` → returns new Counter
- Validation: `canDecrement()` check, overflow check in increment

**CounterEvent.java** (sealed interface):
- `ValueIncremented(int newValue)` with `@TypeName("value-incremented")`
- `ValueDecremented(int newValue)` with `@TypeName("value-decremented")`
- `NameChanged(String newName)` with `@TypeName("name-changed")`

### Application Layer

**CounterEntity.java** (Event Sourced Entity):
- `emptyState()`: returns `new Counter("", 0)`
- Command handlers: `increment()`, `decrement()`, `changeName(ChangeNameCommand)`, `get()`
- `applyEvent(CounterEvent)`: pure state reconstruction from events

### API Layer

**CounterEndpoint.java** (HTTP Endpoint):
- `POST /counter/{counterId}/increment` → calls CounterEntity::increment
- `POST /counter/{counterId}/decrement` → calls CounterEntity::decrement
- `PUT /counter/{counterId}/name` → calls CounterEntity::changeName
- `GET /counter/{counterId}` → calls CounterEntity::get
- API-specific request/response records defined as inner classes
- `@Acl(allow = @Acl.Matcher(principal = Acl.Principal.ALL))`

### Test Strategy

**Unit tests** (CounterEntityTest):
- Increment from zero, multiple increments
- Decrement success, decrement at zero (error)
- Name change preserves value
- Overflow at MAX_VALUE

**Integration tests** (CounterEndpointIntegrationTest):
- Full HTTP round-trip for each endpoint
- Error response for decrement at zero
- Name change + get verification

## Complexity Tracking

No constitution violations. No complexity justifications needed.
