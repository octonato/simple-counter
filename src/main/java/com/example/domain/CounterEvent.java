package com.example.domain;

import akka.javasdk.annotations.TypeName;

public sealed interface CounterEvent {

  @TypeName("value-incremented")
  record ValueIncremented(long newValue) implements CounterEvent {}

  @TypeName("value-decremented")
  record ValueDecremented(long newValue) implements CounterEvent {}

  @TypeName("name-changed")
  record NameChanged(String newName) implements CounterEvent {}
}
