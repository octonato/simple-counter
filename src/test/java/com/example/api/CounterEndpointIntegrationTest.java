package com.example.api;

import akka.javasdk.testkit.TestKit;
import akka.javasdk.testkit.TestKitSupport;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CounterEndpointIntegrationTest extends TestKitSupport {

  @Test
  void shouldIncrementCounter() {
    var counterId = "test-inc-" + System.nanoTime();

    var response = httpClient
        .POST("/counter/" + counterId + "/increment")
        .responseBodyAs(CounterEndpoint.CounterResponse.class)
        .invoke();

    assertThat(response.status().isSuccess()).isTrue();
    assertThat(response.body().value()).isEqualTo(1);
    assertThat(response.body().name()).isEmpty();
  }

  @Test
  void shouldIncrementMultipleTimes() {
    var counterId = "test-multi-" + System.nanoTime();

    httpClient.POST("/counter/" + counterId + "/increment")
        .responseBodyAs(CounterEndpoint.CounterResponse.class)
        .invoke();
    httpClient.POST("/counter/" + counterId + "/increment")
        .responseBodyAs(CounterEndpoint.CounterResponse.class)
        .invoke();
    var response = httpClient.POST("/counter/" + counterId + "/increment")
        .responseBodyAs(CounterEndpoint.CounterResponse.class)
        .invoke();

    assertThat(response.body().value()).isEqualTo(3);
  }

  @Test
  void shouldGetCounter() {
    var counterId = "test-get-" + System.nanoTime();

    httpClient.POST("/counter/" + counterId + "/increment")
        .responseBodyAs(CounterEndpoint.CounterResponse.class)
        .invoke();

    var response = httpClient.GET("/counter/" + counterId)
        .responseBodyAs(CounterEndpoint.CounterResponse.class)
        .invoke();

    assertThat(response.status().isSuccess()).isTrue();
    assertThat(response.body().value()).isEqualTo(1);
  }

  @Test
  void shouldDecrementCounter() {
    var counterId = "test-dec-" + System.nanoTime();

    httpClient.POST("/counter/" + counterId + "/increment")
        .responseBodyAs(CounterEndpoint.CounterResponse.class)
        .invoke();
    httpClient.POST("/counter/" + counterId + "/increment")
        .responseBodyAs(CounterEndpoint.CounterResponse.class)
        .invoke();

    var response = httpClient.POST("/counter/" + counterId + "/decrement")
        .responseBodyAs(CounterEndpoint.CounterResponse.class)
        .invoke();

    assertThat(response.status().isSuccess()).isTrue();
    assertThat(response.body().value()).isEqualTo(1);
  }

  @Test
  void shouldRejectDecrementAtZero() {
    var counterId = "test-dec-zero-" + System.nanoTime();

    assertThatThrownBy(() ->
        httpClient.POST("/counter/" + counterId + "/decrement")
            .responseBodyAs(String.class)
            .invoke()
    ).isInstanceOf(Exception.class)
     .hasMessageContaining("400");
  }

  @Test
  void shouldChangeCounterName() {
    var counterId = "test-name-" + System.nanoTime();

    httpClient.POST("/counter/" + counterId + "/increment")
        .responseBodyAs(CounterEndpoint.CounterResponse.class)
        .invoke();

    var response = httpClient.PUT("/counter/" + counterId + "/name")
        .withRequestBody(new CounterEndpoint.ChangeNameRequest("my-counter"))
        .responseBodyAs(CounterEndpoint.CounterResponse.class)
        .invoke();

    assertThat(response.status().isSuccess()).isTrue();
    assertThat(response.body().name()).isEqualTo("my-counter");
    assertThat(response.body().value()).isEqualTo(1);

    // Verify via GET
    var getResponse = httpClient.GET("/counter/" + counterId)
        .responseBodyAs(CounterEndpoint.CounterResponse.class)
        .invoke();
    assertThat(getResponse.body().name()).isEqualTo("my-counter");
  }
}
