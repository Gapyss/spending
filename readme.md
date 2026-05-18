# Transaction Spending API

Stage 2 of Java backend learning roadmap.

## Stack
- Java 21, Spring Boot 3.x, Maven
- Postgres 16 (Docker Compose)

## Run

Start Postgres:
```bash
docker compose up -d
```

Start the app:
```bash
./mvnw spring-boot:run
```

Verify:
```bash
curl http://localhost:8080/actuator/health
```

## Test
```bash
./mvnw test
```

## Planned endpoints
| Method | Path | Description |
|--------|------|-------------|
| POST | `/transactions` | Record a new transaction |
| GET | `/transactions` | List all (filter by category, date range) |
| GET | `/transactions/{id}` | Get one transaction |
| PUT | `/transactions/{id}` | Update a transaction |
| DELETE | `/transactions/{id}` | Delete a transaction |
| GET | `/transactions/summary` | Total spending by category |