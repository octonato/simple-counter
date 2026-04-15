package com.example.api;

import akka.javasdk.annotations.Acl;
import akka.javasdk.annotations.http.Get;
import akka.javasdk.annotations.http.HttpEndpoint;
import akka.javasdk.client.ComponentClient;
import com.example.application.CountersAtLeastTenView;

import java.util.List;

@HttpEndpoint("/counters")
@Acl(allow = @Acl.Matcher(principal = Acl.Principal.ALL))
public class CountersEndpoint {

  public record CounterSummary(String counterId, String name, long value) {}
  public record CounterList(List<CounterSummary> counters) {}

  private final ComponentClient componentClient;

  public CountersEndpoint(ComponentClient componentClient) {
    this.componentClient = componentClient;
  }

  @Get("/at-least-ten")
  public CounterList listAtLeastTen() {
    var result = componentClient
        .forView()
        .method(CountersAtLeastTenView::findCountersAtLeastTen)
        .invoke();
    var counters = result.items().stream()
        .map(row -> new CounterSummary(row.counterId(), row.name(), row.value()))
        .toList();
    return new CounterList(counters);
  }
}
