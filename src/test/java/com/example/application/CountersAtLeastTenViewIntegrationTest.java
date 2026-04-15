package com.example.application;

import akka.javasdk.testkit.TestKit;
import akka.javasdk.testkit.TestKitSupport;
import com.example.domain.CounterEvent;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class CountersAtLeastTenViewIntegrationTest extends TestKitSupport {

  @Override
  protected TestKit.Settings testKitSettings() {
    return TestKit.Settings.DEFAULT
        .withEventSourcedEntityIncomingMessages(CounterEntity.class);
  }

  private void setValue(String counterId, long value) {
    var events = testKit.getEventSourcedEntityIncomingMessages(CounterEntity.class);
    events.publish(new CounterEvent.ValueIncremented(value), counterId);
  }

  private void rename(String counterId, String name) {
    var events = testKit.getEventSourcedEntityIncomingMessages(CounterEntity.class);
    events.publish(new CounterEvent.NameChanged(name), counterId);
  }

  private List<CountersAtLeastTenView.CounterRow> query() {
    return componentClient
        .forView()
        .method(CountersAtLeastTenView::findCountersAtLeastTen)
        .invoke()
        .items();
  }

  @Test
  void shouldReturnOnlyCountersWithValueAtLeastTen() {
    setValue("c-3", 3);
    setValue("c-9", 9);
    setValue("c-10", 10);
    setValue("c-11", 11);
    setValue("c-42", 42);

    Awaitility.await()
        .atMost(10, TimeUnit.SECONDS)
        .untilAsserted(() -> {
          var ids = query().stream().map(CountersAtLeastTenView.CounterRow::counterId).toList();
          assertThat(ids).containsExactlyInAnyOrder("c-10", "c-11", "c-42");
        });
  }

  @Test
  void shouldReturnEmptyListWhenNoCounterQualifies() {
    setValue("low-1", 1);
    setValue("low-2", 5);
    setValue("low-3", 9);

    Awaitility.await()
        .atMost(10, TimeUnit.SECONDS)
        .untilAsserted(() -> {
          var ids = query().stream().map(CountersAtLeastTenView.CounterRow::counterId).toList();
          assertThat(ids).doesNotContain("low-1", "low-2", "low-3");
        });
  }

  @Test
  void shouldIncludeCounterWhenItCrossesThresholdUpwards() {
    var counterId = "rising";
    setValue(counterId, 9);

    Awaitility.await()
        .atMost(10, TimeUnit.SECONDS)
        .untilAsserted(() -> {
          var ids = query().stream().map(CountersAtLeastTenView.CounterRow::counterId).toList();
          assertThat(ids).doesNotContain(counterId);
        });

    setValue(counterId, 10);

    Awaitility.await()
        .atMost(10, TimeUnit.SECONDS)
        .untilAsserted(() -> {
          var row = query().stream()
              .filter(r -> r.counterId().equals(counterId))
              .findFirst();
          assertThat(row).isPresent();
          assertThat(row.get().value()).isEqualTo(10);
        });
  }

  @Test
  void shouldExcludeCounterWhenItCrossesThresholdDownwards() {
    var counterId = "falling";
    setValue(counterId, 10);

    Awaitility.await()
        .atMost(10, TimeUnit.SECONDS)
        .untilAsserted(() ->
            assertThat(query().stream().map(CountersAtLeastTenView.CounterRow::counterId))
                .contains(counterId));

    var events = testKit.getEventSourcedEntityIncomingMessages(CounterEntity.class);
    events.publish(new CounterEvent.ValueDecremented(9), counterId);

    Awaitility.await()
        .atMost(10, TimeUnit.SECONDS)
        .untilAsserted(() ->
            assertThat(query().stream().map(CountersAtLeastTenView.CounterRow::counterId))
                .doesNotContain(counterId));
  }

  @Test
  void shouldExcludeCounterWhenItOverflowsToZero() {
    var counterId = "overflowing";
    setValue(counterId, 100);

    Awaitility.await()
        .atMost(10, TimeUnit.SECONDS)
        .untilAsserted(() ->
            assertThat(query().stream().map(CountersAtLeastTenView.CounterRow::counterId))
                .contains(counterId));

    var events = testKit.getEventSourcedEntityIncomingMessages(CounterEntity.class);
    events.publish(new CounterEvent.ValueIncremented(0), counterId);

    Awaitility.await()
        .atMost(10, TimeUnit.SECONDS)
        .untilAsserted(() ->
            assertThat(query().stream().map(CountersAtLeastTenView.CounterRow::counterId))
                .doesNotContain(counterId));
  }

  @Test
  void shouldReflectRenameForQualifyingCounter() {
    var counterId = "renamed";
    setValue(counterId, 15);
    rename(counterId, "old-name");

    Awaitility.await()
        .atMost(10, TimeUnit.SECONDS)
        .untilAsserted(() -> {
          var row = query().stream()
              .filter(r -> r.counterId().equals(counterId))
              .findFirst();
          assertThat(row).isPresent();
          assertThat(row.get().name()).isEqualTo("old-name");
        });

    rename(counterId, "new-name");

    Awaitility.await()
        .atMost(10, TimeUnit.SECONDS)
        .untilAsserted(() -> {
          var row = query().stream()
              .filter(r -> r.counterId().equals(counterId))
              .findFirst();
          assertThat(row).isPresent();
          assertThat(row.get().name()).isEqualTo("new-name");
        });
  }
}
