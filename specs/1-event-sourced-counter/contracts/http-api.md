# HTTP API Contract: Counter Endpoint

**Base path**: `/counter`

## Endpoints

### POST /counter/{counterId}/increment

Increment the counter by 1. Creates the counter if it doesn't exist.

- **Path params**: `counterId` (String) — unique counter identifier
- **Request body**: none
- **Response 200**: `{ "name": "", "value": 1 }`
- **Overflow**: When value is at Integer.MAX_VALUE, increments to 0

### POST /counter/{counterId}/decrement

Decrement the counter by 1.

- **Path params**: `counterId` (String) — unique counter identifier
- **Request body**: none
- **Response 200**: `{ "name": "", "value": 4 }`
- **Error 400**: `"Cannot decrement below zero"` — when counter value is 0

### PUT /counter/{counterId}/name

Change the counter's name.

- **Path params**: `counterId` (String) — unique counter identifier
- **Request body**: `{ "name": "new-name" }`
- **Response 200**: `{ "name": "new-name", "value": 5 }`

### GET /counter/{counterId}

Retrieve the current counter value and name.

- **Path params**: `counterId` (String) — unique counter identifier
- **Response 200**: `{ "name": "my-counter", "value": 42 }`
