package com.example.domain;

public record Counter(String name, long value) {

  public Counter increment() {
    if (value == Long.MAX_VALUE) {
      return new Counter(name, 0);
    }
    return new Counter(name, value + 1);
  }

  public Counter decrement() {
    if (value == 0) {
      throw new IllegalStateException("Cannot decrement below zero");
    }
    return new Counter(name, value - 1);
  }

  public boolean canDecrement() {
    return value > 0;
  }

  public Counter withName(String newName) {
    return new Counter(newName, value);
  }
}
