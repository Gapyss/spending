# Week 1 Assignment — Java Backend Roadmap

**Owner:** Gapys
**Phase:** 1 (Backend depth foundations)
**Stage:** 2 — REST Transaction Spending API
**Milestone this week:** 1 of 6
**Time budget:** 3–5 hours (two 90-min sessions)

---

## What you're building

A backend service that records and queries personal spending transactions.

**Core data model:**
```
Transaction
  id          UUID (PK)
  amount      DECIMAL(15,2)   -- e.g. 250.00
  category    VARCHAR         -- "food", "transport", "utilities", etc.
  description VARCHAR         -- "lunch at MK", "BTS card top-up"
  date        DATE            -- transaction date
  created_at  TIMESTAMP
```

**Endpoints you'll build across milestones:**

| Method | Path | Description |
|--------|------|-------------|
| POST   | `/transactions` | Record a new transaction |
| GET    | `/transactions` | List all (with optional `?category=` and `?start=`/`?end=` filters) |
| GET    | `/transactions/{id}` | Get one transaction |
| PUT    | `/transactions/{id}` | Update a transaction |
| DELETE | `/transactions/{id}` | Delete a transaction |
| GET    | `/transactions/summary` | Total spending grouped by category |

**Milestone 1 scope:** Spring Boot boots, Postgres boots, health endpoint responds, one test passes. No business logic yet.

---

## Ground rules

- **No AI assistance for Session 1.** Reason it out, then verify by running.
- **No googling answers first.** If stuck for 10+ minutes, look at the hint. If still stuck, write what confuses you in a comment and move on.
- **Tests are non-negotiable.** Even in Milestone 1.
- **Commits matter.** Conventional Commits format. No `WIP`. No `fix typo` over previous commits.

---

## Session 1 — Java fundamentals warmup (90 min)
Create a single file: `Scratch.java`

### ✅ Exercise A — Equality (15 min)

Write code demonstrating all four cases. Each case = one method or labeled block.

- [ ] **Case 1:** `==` true AND `.equals()` true (same reference — assign one variable to another)
- [ ] **Case 2:** `==` false AND `.equals()` true (same content, different objects — use `new String("hello")` twice)
- [ ] **Case 3:** `==` true AND `.equals()` false → **impossible.** Explain WHY in a comment.
- [ ] **Case 4:** `==` false AND `.equals()` false (different content entirely)

**Reflection (write as comment at the bottom):**
> *When would I ever want to use `==` on objects?* (Hint: it's rare, but not never. Think about checking for null, or comparing enum values.)

---

### ✅ Exercise B — Word frequency (30 min)

Write a method:
```java
public static Map<String, Integer> wordFrequency(String paragraph)
```

Then print the top 5 most frequent words, sorted by count descending.

**Test paragraph to use:**
```
the quick brown fox jumps over the lazy dog the fox is quick and the dog is lazy
```

Expected output (your formatting can vary):
```
the: 4
quick: 2
fox: 2
dog: 2
is: 2
```

**Hints — read only if blocked 10+ min:**
- Split on whitespace: `paragraph.split("\\s+")`
- For each word, increment its count in a `Map<String, Integer>`. Look up `Map.merge`.
- Sort: `map.entrySet().stream().sorted(Comparator.comparingInt(...).reversed()).limit(5).forEach(...)`

**After it works, answer (as a comment):**
- What's the average-case Big-O of `HashMap.get(key)`?
- What's the worst case? Why?

---

### ✅ Exercise C — Polymorphism (15 min)

Build this hierarchy:
- `abstract class Animal` with abstract method `void makeSound()`
- `class Dog extends Animal` → prints "Woof"
- `class Cat extends Animal` → prints "Meow"
- `class Cow extends Animal` → prints "Moo"

In `main`:
```java
List<Animal> animals = List.of(new Dog(), new Cat(), new Cow());
for (Animal a : animals) {
    a.makeSound();
}
```

**Before running, write this as a comment at the top of the file:**
> *"What would happen if I declared `makeSound()` as `private` in the subclasses?"*

Reason about it (don't run it yet). Then change one subclass to `private` and try to compile. Was your reasoning right?

---

### ✅ Exercise D — Immutability (30 min, bonus)

```java
List<String> a = List.of("a", "b", "c");
// 1. Try: a.add("d");  → observe the exception, write down its name as a comment
// 2. Create mutable version:
List<String> b = new ArrayList<>(List.of("a", "b", "c"));
// b.add("d");  → works
```

**Then research (this one you CAN google):**
- What's the difference between `Collections.unmodifiableList(list)` and `List.copyOf(list)`?
- Write a 3-line comment summarizing.

---

### Session 1 commit

```bash
git add Scratch.java
git commit -m "chore: java fundamentals warmup exercises"
```

---

## Session 2 — Stage 2 Milestone 1 (90 min)

**Goal:** Spring Boot project boots, Postgres boots, health endpoint responds, one test passes.

**Do NOT do in this session:** JPA, Flyway, business logic, transaction controllers. Those are Milestone 2.

### Step 1 — Init the project (10 min)

Go to [start.spring.io](https://start.spring.io) and select:

- [ ] Project: **Maven**
- [ ] Language: **Java**
- [ ] Spring Boot: **3.x** (latest stable)
- [ ] Java: **21**
- [ ] Group: `com.gapys`
- [ ] Artifact: `spending`
- [ ] Dependencies:
  - [ ] Spring Web
  - [ ] Spring Boot DevTools
  - [ ] Spring Boot Actuator

Generate, download, unzip. Run once to confirm it boots:
```bash
./mvnw spring-boot:run
```
Visit `http://localhost:8080` — you'll get a 404 page. That's fine. It means it's running.

**Commit:**
```
chore: init spring boot project
```

---

### Step 2 — Docker Compose Postgres (20 min)

Create `docker-compose.yml` at the repo root:

```yaml
services:
  postgres:
    image: postgres:16
    container_name: spending-postgres
    environment:
      POSTGRES_USER: spending
      POSTGRES_PASSWORD: spending
      POSTGRES_DB: spending
    ports:
      - "5432:5432"
    volumes:
      - spending-postgres-data:/var/lib/postgresql/data

volumes:
  spending-postgres-data:
```

Run:
```bash
docker compose up -d
docker compose ps   # should show "running"
docker compose logs postgres   # should show "database system is ready to accept connections"
```

> **Don't** connect Spring to it yet. The point of this step is to verify the infrastructure boots independently before wiring it up.

**Commit:**
```
chore: add postgres docker compose
```

---

### Step 3 — Expose Actuator health endpoint (15 min)

Rename `application.properties` → `application.yml`.

Add:
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health

spring:
  application:
    name: spending
```

Restart the app:
```bash
./mvnw spring-boot:run
```

Hit it:
```bash
curl http://localhost:8080/actuator/health
```

Expected:
```json
{"status":"UP"}
```

---

### Step 4 — Write the test (30 min)

Create `src/test/java/com/gapys/spending/HealthCheckTest.java`:

```java
package com.gapys.spending;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class HealthCheckTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void healthEndpointReturnsUp() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }
}
```

Run:
```bash
./mvnw test
```

Should pass. If not, debug before moving on.

**Commit:**
```
feat: expose actuator health endpoint with test
```

---

### Step 5 — README (15 min)

Create a minimal `README.md`:

````markdown
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
````

**Commit:**
```
docs: add readme
```

---

## End-of-week deliverables

When done, bring these to your next session:

- [ ] **GitHub repo link** (public)
- [ ] **`Scratch.java`** committed to the repo (in a `scratch/` folder is fine)
- [ ] **One paragraph** answering: *"Why is constructor injection preferred over `@Autowired` field injection?"* (write it WITHOUT googling first)
- [ ] **Honest note** on what was hard, what surprised you, what you don't yet trust in your own code

---

## What comes next (Milestones 2–6 preview)

| Milestone | Focus |
|-----------|-------|
| 2 | JPA entity + Flyway migration for `transactions` table |
| 3 | POST `/transactions` with validation |
| 4 | GET `/transactions` with category + date-range filters |
| 5 | PUT/DELETE + error handling (404, 400) |
| 6 | GET `/transactions/summary` — aggregate by category |

---

## Anti-patterns to avoid this week

| Don't | Why |
|---|---|
| Add JPA/Hibernate now | Milestone 2 territory. One thing at a time. |
| Use `@Autowired` on fields | You'll spend Milestone 2 unlearning it. Constructor injection from day one. |
| Skip the test because "it's just a health check" | The point isn't the test, it's wiring up MockMvc before things get complex. |
| Commit with `WIP` or `update stuff` | This is real-software-engineer training. Every commit reviewable. |
| Try to do both sessions in one day | 3–5 hrs/week is the budget. Spreading them out cements better than cramming. |

---

## When you're done

Open a new chat and say:
> *"Week 1 of Stage 2 done. Repo is here: <link>. Review against the assignment in this repo."*

Then we'll either advance to Milestone 2, or backfill anything that didn't land.
