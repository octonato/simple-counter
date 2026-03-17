package com.example.application;

import akka.Done;
import akka.javasdk.annotations.Component;
import akka.javasdk.eventsourcedentity.EventSourcedEntity;
import com.example.domain.Counter;
import com.example.domain.CounterEvent;

@Component(id = "counter")
public class CounterEntity extends EventSourcedEntity<Counter, CounterEvent> {

  @Override
  public Counter emptyState() {
    return new Counter("", 0);
  }

  public Effect<Counter> increment() {
    var newState = currentState().increment();
    return effects()
        .persist(new CounterEvent.ValueIncremented(newState.value()))
        .thenReply(state -> state);
  }

  public Effect<Counter> decrement() {
    if (!currentState().canDecrement()) {
      return effects().error("Cannot decrement below zero");
    }
    var newState = currentState().decrement();
    return effects()
        .persist(new CounterEvent.ValueDecremented(newState.value()))
        .thenReply(state -> state);
  }

  public record ChangeNameCommand(String name) {}

  public Effect<Counter> changeName(ChangeNameCommand command) {
    return effects()
        .persist(new CounterEvent.NameChanged(command.name()))
        .thenReply(state -> state);
  }

  public Effect<Counter> get() {
    return effects().reply(currentState());
  }

  @Override
  public Counter applyEvent(CounterEvent event) {
    return switch (event) {
      case CounterEvent.ValueIncremented e -> new Counter(currentState().name(), e.newValue());
      case CounterEvent.ValueDecremented e -> new Counter(currentState().name(), e.newValue());
      case CounterEvent.NameChanged e -> currentState().withName(e.newName());
    };
  }
}
