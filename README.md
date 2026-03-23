# Event Sourced Counter

An Akka service implementing a named, event-sourced counter with increment/decrement operations, a zero floor constraint, and overflow-to-zero behavior at Long.MAX_VALUE.

## Features

- Counters start at zero with an empty name
- Increment and decrement by 1
- Counter value never goes below zero (decrement at zero is rejected)
- Counter overflows to zero when incremented past Long.MAX_VALUE
- Counter name can be changed at any time without affecting the value

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
curl -X POST http://localhost:9000/counter/zero-test/decrement
```

## Testing

Run unit tests:
```shell
mvn test
```

Run all tests (unit + integration):
```shell
mvn verify
```

## Deployment

Build container image:
```shell
mvn clean install -DskipTests
```

Deploy using the Akka CLI:
```shell
akka service deploy simple-counter simple-counter:tag-name --push
```
