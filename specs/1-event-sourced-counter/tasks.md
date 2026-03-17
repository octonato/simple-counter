# Tasks: Event Sourced Counter

**Branch**: `1-event-sourced-counter` | **Date**: 2026-03-17
**Plan**: [plan.md](plan.md) | **Spec**: [spec.md](spec.md)

## Phase 1: Setup

- [x] T001 Verify project compiles with `mvn compile` and resolve any dependency issues

## Phase 2: Foundational — Domain Model

These tasks create the shared domain layer used by all user stories.

- [x] T002 [P] Create Counter state record with fields (name, value), `increment()`, `decrement()`, `withName()` methods, and validation logic in `src/main/java/com/example/domain/Counter.java`
- [x] T003 [P] Create CounterEvent sealed interface with ValueIncremented, ValueDecremented, NameChanged event records (each with @TypeName) in `src/main/java/com/example/domain/CounterEvent.java`
- [x] T004 Verify domain layer compiles with `mvn compile`

## Phase 3: User Story 1 — Create and Increment a Counter (P1)

**Goal**: Users can create counters and increment their value. Counter starts at zero.
**Independent test**: Create a counter, increment it multiple times, verify value.

- [x] T005 [US1] Create CounterEntity extending EventSourcedEntity with `emptyState()`, `increment()` command handler, `get()` command handler, and `applyEvent()` in `src/main/java/com/example/application/CounterEntity.java`
- [x] T006 [US1] Create CounterEndpoint with POST `/{counterId}/increment` and GET `/{counterId}` routes, API-specific response record, and @Acl annotation in `src/main/java/com/example/api/CounterEndpoint.java`
- [x] T007 [US1] Verify increment and get compile and work with `mvn compile`
- [x] T008 [US1] Create CounterEntityTest with unit tests for increment from zero, multiple increments, and get using EventSourcedTestKit in `src/test/java/com/example/application/CounterEntityTest.java`
- [x] T009 [US1] Run unit tests with `mvn test`
- [x] T010 [US1] Create CounterEndpointIntegrationTest with integration tests for POST increment and GET using httpClient in `src/test/java/com/example/api/CounterEndpointIntegrationTest.java`
- [x] T011 [US1] Run integration tests with `mvn verify`

## Phase 4: User Story 2 — Decrement with Zero Floor (P1)

**Goal**: Users can decrement counters. Decrementing at zero is rejected.
**Independent test**: Increment a counter, decrement it, verify zero-floor rejection.
**Depends on**: Phase 3 (entity and endpoint exist)

- [x] T012 [US2] Add `decrement()` command handler to CounterEntity that validates zero floor and persists ValueDecremented event in `src/main/java/com/example/application/CounterEntity.java`
- [x] T013 [US2] Add POST `/{counterId}/decrement` route to CounterEndpoint in `src/main/java/com/example/api/CounterEndpoint.java`
- [x] T014 [US2] Add unit tests for decrement success and decrement-at-zero error to CounterEntityTest in `src/test/java/com/example/application/CounterEntityTest.java`
- [x] T015 [US2] Add integration tests for POST decrement (success and error at zero) to CounterEndpointIntegrationTest in `src/test/java/com/example/api/CounterEndpointIntegrationTest.java`
- [x] T016 [US2] Run all tests with `mvn verify`

## Phase 5: User Story 3 — Rename a Counter (P2)

**Goal**: Users can change a counter's name without affecting its value.
**Independent test**: Create counter, rename it, verify name changed and value unchanged.
**Depends on**: Phase 3 (entity and endpoint exist)

- [x] T017 [US3] Add `changeName(ChangeNameCommand)` command handler to CounterEntity that persists NameChanged event in `src/main/java/com/example/application/CounterEntity.java`
- [x] T018 [US3] Add PUT `/{counterId}/name` route with request record to CounterEndpoint in `src/main/java/com/example/api/CounterEndpoint.java`
- [x] T019 [US3] Add unit tests for name change (preserves value, sets new name) to CounterEntityTest in `src/test/java/com/example/application/CounterEntityTest.java`
- [x] T020 [US3] Add integration test for PUT name to CounterEndpointIntegrationTest in `src/test/java/com/example/api/CounterEndpointIntegrationTest.java`
- [x] T021 [US3] Run all tests with `mvn verify`

## Phase 6: User Story 4 — Overflow at Maximum Value (P3)

**Goal**: Counter overflows to zero when incremented past Integer.MAX_VALUE.
**Independent test**: Set counter to MAX_VALUE, increment, verify value is 0.
**Depends on**: Phase 3 (increment logic exists)

- [x] T022 [US4] Add unit test for overflow behavior (increment at MAX_VALUE → 0, then increment again → 1) to CounterEntityTest in `src/test/java/com/example/application/CounterEntityTest.java`
- [x] T023 [US4] Run all tests with `mvn verify` (overflow logic already in Counter.increment(), this phase validates it)

## Phase 7: Polish

- [x] T024 Run full test suite with `mvn verify` and confirm all tests pass
- [x] T025 Update README.md with feature description and curl examples from quickstart.md

## Dependencies

```text
T001 → T002, T003 (setup before domain)
T002, T003 → T004 (compile check)
T004 → T005 (domain before entity)
T005 → T006 (entity before endpoint)
T006 → T007 (compile check)
T007 → T008, T010 (compile before tests)
T008 → T009 (write tests before running)
T010 → T011 (write tests before running)
T011 → T012, T017 (US1 complete before US2/US3)
T012 → T013 → T014 → T015 → T016 (US2 sequence)
T017 → T018 → T019 → T020 → T021 (US3 sequence)
T011 → T022 → T023 (US1 complete before US4)
T016, T021, T023 → T024 (all stories before polish)
T024 → T025 (tests pass before docs)
```

## Parallel Execution Opportunities

- **T002 + T003**: Counter.java and CounterEvent.java have no dependencies on each other
- **US3 (Phase 5) + US4 (Phase 6)**: Can run in parallel after US1 + US2 complete (US3 adds naming, US4 validates overflow — different concerns)

## Implementation Strategy

1. **MVP**: Complete Phase 1–3 (setup + domain + increment/get). This delivers a working counter that can be incremented and queried.
2. **Core complete**: Add Phase 4 (decrement with zero floor). All P1 stories done.
3. **Full feature**: Add Phase 5 (rename) and Phase 6 (overflow validation).
4. **Ship**: Phase 7 polish and documentation.

**Total tasks**: 25
**Per story**: US1: 7, US2: 5, US3: 5, US4: 2, Setup: 1, Foundation: 3, Polish: 2
