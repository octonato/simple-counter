package com.example.api;

import akka.javasdk.testkit.TestKitSupport;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class CountersEndpointIntegrationTest extends TestKitSupport {

  private void increment(String counterId, int times) {
    for (int i = 0; i < times; i++) {
      httpClient.POST("/counter/" + counterId + "/increment")
          .responseBodyAs(CounterEndpoint.CounterResponse.class)
          .invoke();
    }
  }

  private void decrement(String counterId) {
    httpClient.POST("/counter/" + counterId + "/decrement")
        .responseBodyAs(CounterEndpoint.CounterResponse.class)
        .invoke();
  }

  private void rename(String counterId, String name) {
    httpClient.PUT("/counter/" + counterId + "/name")
        .withRequestBody(new CounterEndpoint.ChangeNameRequest(name))
        .responseBodyAs(CounterEndpoint.CounterResponse.class)
        .invoke();
  }

  private CountersEndpoint.CounterList listAtLeastTen() {
    var response = httpClient.GET("/counters/at-least-ten")
        .responseBodyAs(CountersEndpoint.CounterList.class)
        .invoke();
    assertThat(response.status().isSuccess()).isTrue();
    return response.body();
  }

  @Test
  void shouldReturnQualifyingCountersAndExcludeOthers() {
    var qualifyingId = "endpoint-qualifies-" + System.nanoTime();
    var belowId = "endpoint-below-" + System.nanoTime();

    increment(qualifyingId, 10);
    increment(belowId, 5);

    Awaitility.await()
        .atMost(10, TimeUnit.SECONDS)
        .untilAsserted(() -> {
          var list = listAtLeastTen();
          var ids = list.counters().stream()
              .map(CountersEndpoint.CounterSummary::counterId)
              .toList();
          assertThat(ids).contains(qualifyingId);
          assertThat(ids).doesNotContain(belowId);

          var qualifying = list.counters().stream()
              .filter(c -> c.counterId().equals(qualifyingId))
              .findFirst();
          assertThat(qualifying).isPresent();
          assertThat(qualifying.get().value()).isEqualTo(10);
        });
  }

  @Test
  void shouldRemoveCounterFromListAfterCrossingBelowThreshold() {
    var counterId = "endpoint-falling-" + System.nanoTime();

    increment(counterId, 10);

    Awaitility.await()
        .atMost(10, TimeUnit.SECONDS)
        .untilAsserted(() ->
            assertThat(listAtLeastTen().counters().stream()
                .map(CountersEndpoint.CounterSummary::counterId))
                .contains(counterId));

    decrement(counterId);

    Awaitility.await()
        .atMost(10, TimeUnit.SECONDS)
        .untilAsserted(() ->
            assertThat(listAtLeastTen().counters().stream()
                .map(CountersEndpoint.CounterSummary::counterId))
                .doesNotContain(counterId));
  }

  @Test
  void shouldReflectRenameInResponse() {
    var counterId = "endpoint-renamed-" + System.nanoTime();

    increment(counterId, 12);
    rename(counterId, "my-big-counter");

    Awaitility.await()
        .atMost(10, TimeUnit.SECONDS)
        .untilAsserted(() -> {
          var row = listAtLeastTen().counters().stream()
              .filter(c -> c.counterId().equals(counterId))
              .findFirst();
          assertThat(row).isPresent();
          assertThat(row.get().name()).isEqualTo("my-big-counter");
          assertThat(row.get().value()).isEqualTo(12);
        });
  }

  @Test
  void shouldReturnSuccessEvenWhenNoCountersQualify() {
    var response = httpClient.GET("/counters/at-least-ten")
        .responseBodyAs(CountersEndpoint.CounterList.class)
        .invoke();

    assertThat(response.status().isSuccess()).isTrue();
    assertThat(response.body().counters()).isNotNull();
  }
}
