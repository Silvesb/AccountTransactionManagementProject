# Welcome to the account management service

## How to set up

### Requirements
- Java 17+
- Something to host docker with (for postgres and rabbitMQ) (Docker Desktop for Windows)

### Local with Docker dependencies
1. Start Postgres and RabbitMQ:
   - `docker compose -f docker-compose.yml up --build`

2. Run the app:
   - `./gradlew bootRun`

3. Access the project at http://localhost:8080.

4. (Optional) Get Postman or something similar to make requests towards the API.

5. Unit tests can be used with `./gradlew test`

## Explanation of important choices

### Account, Balance, Transaction
- `Account` is each customer.
- `Balance` is the bridge between users and different currencies.
- `AccountTransaction` records the transactions of different users and currencies.

This solution keeps the project fairly simple for balances and preserves transaction history without having to recalculate.

### Benefit of RabbitMQ
Every insert/update helps performance by creating events for downstream services.

## Possible improvements
- Compare and swap balance updates to avoid issues.
- Enforce currency limits and account-specific rules
- Add pagination for transaction history.
- Add audit fields like created_By, request_id for traceability.

## Scalability

### Current solution
- Currently, the existing solution could work in a smaller. 

### Horizontal scaling considerations
- Get rid of local session states.
- Use DB constraints and transactions to guarantee correctness across instances.
- Use a consistent message routing key per account to keep ordering where needed.
- Ensure RabbitMQ retries can re-deliver.
- Add indexes on account_id, currency and account_id in transaction history.

## AI usage

I used AI to familiarize myself with the usage of MyBatis and RabbitMQ. The Docker solutions were mostly made with AI, since I've never integrated Batis and RabbitMQ into any of my projects.
Also AI was used to help with tests, since integration tests required 
