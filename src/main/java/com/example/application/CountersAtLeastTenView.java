package com.example.application;

import akka.javasdk.annotations.Component;
import akka.javasdk.annotations.Consume;
import akka.javasdk.annotations.Query;
import akka.javasdk.annotations.Table;
import akka.javasdk.view.TableUpdater;
import akka.javasdk.view.View;
import com.example.domain.CounterEvent;

import java.util.List;

@Component(id = "counters-at-least-ten")
public class CountersAtLeastTenView extends View {

  public record CounterRow(String counterId, String name, long value) {
    public CounterRow withValue(long newValue) {
      return new CounterRow(counterId, name, newValue);
    }

    public CounterRow withName(String newName) {
      return new CounterRow(counterId, newName, value);
    }
  }

  public record CounterRows(List<CounterRow> items) {}

  @Table("counters")
  @Consume.FromEventSourcedEntity(CounterEntity.class)
  public static class CountersUpdater extends TableUpdater<CounterRow> {
    public Effect<CounterRow> onEvent(CounterEvent event) {
      var counterId = updateContext().eventSubject().orElse("");
      var current = rowState() != null ? rowState() : new CounterRow(counterId, "", 0);
      return switch (event) {
        case CounterEvent.ValueIncremented e ->
            effects().updateRow(current.withValue(e.newValue()));
        case CounterEvent.ValueDecremented e ->
            effects().updateRow(current.withValue(e.newValue()));
        case CounterEvent.NameChanged e ->
            effects().updateRow(current.withName(e.newName()));
      };
    }
  }

  @Query("SELECT * AS items FROM counters WHERE value >= 10")
  public QueryEffect<CounterRows> findCountersAtLeastTen() {
    return queryResult();
  }
}
