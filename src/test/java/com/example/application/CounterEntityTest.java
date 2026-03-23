package com.example.application;

import akka.javasdk.testkit.EventSourcedTestKit;
import com.example.domain.Counter;
import com.example.domain.CounterEvent;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CounterEntityTest {

  @Test
  void shouldStartAtZero() {
    var testKit = EventSourcedTestKit.of("counter-1", CounterEntity::new);
    var result = testKit.method(CounterEntity::get).invoke();
    assertThat(result.isReply()).isTrue();
    assertThat(result.getReply().value()).isEqualTo(0);
    assertThat(result.getReply().name()).isEmpty();
  }

  @Test
  void shouldIncrementFromZero() {
    var testKit = EventSourcedTestKit.of("counter-1", CounterEntity::new);
    var result = testKit.method(CounterEntity::increment).invoke();
    assertThat(result.isReply()).isTrue();
    assertThat(result.getReply().value()).isEqualTo(1);
    var event = result.getNextEventOfType(CounterEvent.ValueIncremented.class);
    assertThat(event.newValue()).isEqualTo(1);
  }

  @Test
  void shouldIncrementMultipleTimes() {
    var testKit = EventSourcedTestKit.of("counter-1", CounterEntity::new);
    testKit.method(CounterEntity::increment).invoke();
    testKit.method(CounterEntity::increment).invoke();
    var result = testKit.method(CounterEntity::increment).invoke();
    assertThat(result.getReply().value()).isEqualTo(3);
    assertThat(testKit.getState().value()).isEqualTo(3);
  }

  @Test
  void shouldDecrementFromPositiveValue() {
    var testKit = EventSourcedTestKit.of("counter-1", CounterEntity::new);
    testKit.method(CounterEntity::increment).invoke();
    testKit.method(CounterEntity::increment).invoke();
    testKit.method(CounterEntity::increment).invoke();

    var result = testKit.method(CounterEntity::decrement).invoke();
    assertThat(result.isReply()).isTrue();
    assertThat(result.getReply().value()).isEqualTo(2);
    var event = result.getNextEventOfType(CounterEvent.ValueDecremented.class);
    assertThat(event.newValue()).isEqualTo(2);
  }

  @Test
  void shouldRejectDecrementAtZero() {
    var testKit = EventSourcedTestKit.of("counter-1", CounterEntity::new);
    var result = testKit.method(CounterEntity::decrement).invoke();
    assertThat(result.isError()).isTrue();
    assertThat(result.getError()).isEqualTo("Cannot decrement below zero");
  }

  @Test
  void shouldDecrementToZero() {
    var testKit = EventSourcedTestKit.of("counter-1", CounterEntity::new);
    testKit.method(CounterEntity::increment).invoke();
    var result = testKit.method(CounterEntity::decrement).invoke();
    assertThat(result.getReply().value()).isEqualTo(0);
  }

  @Test
  void shouldChangeName() {
    var testKit = EventSourcedTestKit.of("counter-1", CounterEntity::new);
    testKit.method(CounterEntity::increment).invoke();
    testKit.method(CounterEntity::increment).invoke();

    var result = testKit.method(CounterEntity::changeName)
        .invoke(new CounterEntity.ChangeNameCommand("my-counter"));
    assertThat(result.isReply()).isTrue();
    assertThat(result.getReply().name()).isEqualTo("my-counter");
    assertThat(result.getReply().value()).isEqualTo(2);
    var event = result.getNextEventOfType(CounterEvent.NameChanged.class);
    assertThat(event.newName()).isEqualTo("my-counter");
  }

  @Test
  void shouldChangeNameWithoutAffectingValue() {
    var testKit = EventSourcedTestKit.of("counter-1", CounterEntity::new);
    testKit.method(CounterEntity::increment).invoke();
    testKit.method(CounterEntity::changeName)
        .invoke(new CounterEntity.ChangeNameCommand("first"));
    testKit.method(CounterEntity::changeName)
        .invoke(new CounterEntity.ChangeNameCommand("second"));

    assertThat(testKit.getState().name()).isEqualTo("second");
    assertThat(testKit.getState().value()).isEqualTo(1);
  }

  @Test
  void shouldOverflowAtMaxValue() {
    var testKit = EventSourcedTestKit.of("counter-1", CounterEntity::new);
    // Simulate being at MAX_VALUE by applying events directly
    // We need to increment to MAX_VALUE - use domain logic to verify
    var counter = new Counter("", Long.MAX_VALUE);
    var incremented = counter.increment();
    assertThat(incremented.value()).isEqualTo(0L);

    // And verify normal behavior resumes
    var incrementedAgain = incremented.increment();
    assertThat(incrementedAgain.value()).isEqualTo(1L);
  }
}
