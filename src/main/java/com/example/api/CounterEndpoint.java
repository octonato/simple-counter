package com.example.api;

import akka.javasdk.annotations.Acl;
import akka.javasdk.annotations.http.Get;
import akka.javasdk.annotations.http.HttpEndpoint;
import akka.javasdk.annotations.http.Post;
import akka.javasdk.annotations.http.Put;
import akka.javasdk.client.ComponentClient;
import com.example.application.CounterEntity;

@HttpEndpoint("/counter")
@Acl(allow = @Acl.Matcher(principal = Acl.Principal.ALL))
public class CounterEndpoint {

  public record CounterResponse(String name, int value) {}
  public record ChangeNameRequest(String name) {}

  private final ComponentClient componentClient;

  public CounterEndpoint(ComponentClient componentClient) {
    this.componentClient = componentClient;
  }

  @Post("/{counterId}/increment")
  public CounterResponse increment(String counterId) {
    var counter = componentClient.forEventSourcedEntity(counterId)
        .method(CounterEntity::increment)
        .invoke();
    return toApi(counter);
  }

  @Post("/{counterId}/decrement")
  public CounterResponse decrement(String counterId) {
    var counter = componentClient.forEventSourcedEntity(counterId)
        .method(CounterEntity::decrement)
        .invoke();
    return toApi(counter);
  }

  @Put("/{counterId}/name")
  public CounterResponse changeName(String counterId, ChangeNameRequest request) {
    var counter = componentClient.forEventSourcedEntity(counterId)
        .method(CounterEntity::changeName)
        .invoke(new CounterEntity.ChangeNameCommand(request.name()));
    return toApi(counter);
  }

  @Get("/{counterId}")
  public CounterResponse get(String counterId) {
    var counter = componentClient.forEventSourcedEntity(counterId)
        .method(CounterEntity::get)
        .invoke();
    return toApi(counter);
  }

  private CounterResponse toApi(com.example.domain.Counter counter) {
    return new CounterResponse(counter.name(), counter.value());
  }
}
