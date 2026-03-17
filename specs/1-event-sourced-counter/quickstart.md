# Quickstart: Event Sourced Counter

## Prerequisites

- Java 21
- Maven 3.9+

## Build & Run

```shell
mvn compile exec:java
```

## Try It Out

Create and increment a counter:
```shell
curl -X POST http://localhost:9000/counter/my-counter/increment
```

Increment again:
```shell
curl -X POST http://localhost:9000/counter/my-counter/increment
```

Get current value:
```shell
curl http://localhost:9000/counter/my-counter
```

Decrement:
```shell
curl -X POST http://localhost:9000/counter/my-counter/decrement
```

Rename the counter:
```shell
curl -X PUT http://localhost:9000/counter/my-counter/name \
  -H "Content-Type: application/json" \
  -d '{"name": "My First Counter"}'
```

Try decrementing at zero (should return error):
```shell
# First, get a fresh counter
curl -X POST http://localhost:9000/counter/zero-test/decrement
```
