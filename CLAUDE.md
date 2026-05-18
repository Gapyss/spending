# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project context

Stage 2 of a Java backend learning roadmap — a personal-spending REST API built in deliberate milestones. `week-1-assignment.md` is the source of truth for what scope belongs to the current milestone; check it before adding features. Milestone 1 (current) is intentionally minimal: app boots, Postgres boots, `/actuator/health` responds, one test passes. JPA, Flyway, and the `/transactions` endpoints are explicitly deferred to later milestones.

## Stack

- Java 21, Spring Boot 3.5, Maven (use the wrapper `./mvnw`)
- Postgres 16 via Docker Compose (running but **not yet wired to Spring** — wiring is Milestone 2)
- PDFBox 3 + Tess4J 5 for the `/pdf/ocr` endpoint

## Commands

```bash
./mvnw spring-boot:run         # run the app on :8080
./mvnw test                    # run all tests
./mvnw -Dtest=HealthCheckTest test           # single test class
./mvnw -Dtest=HealthCheckTest#healthEndpointReturnsUp test   # single method
./mvnw -q -DskipTests compile  # quick compile check
docker compose up -d           # start Postgres (data volume: spending-postgres-data)
curl http://localhost:8080/actuator/health
```

## Architecture

Single Spring Boot module under `com.gapys.spending`:

- `SpendingApplication` — entry point; enables `PdfProperties` and `OcrProperties` via `@EnableConfigurationProperties`.
- `pdf/` — the PDF→OCR feature. `PdfOcrController` exposes `POST /pdf/ocr` (multipart `file`). `PdfOcrService` opens the PDF with the password from `pdf.password`, renders each page at `ocr.dpi` with PDFBox `PDFRenderer`, then runs Tess4J using `ocr.tessdata-path` and `ocr.languages`. Controller-local `@ExceptionHandler`s map `InvalidPasswordException → 422`, `IllegalArgumentException → 400`, `TesseractException → 500`.

The `/pdf/ocr` endpoint is independent of the spending-transactions roadmap and has no DB dependency.

## Configuration

All config lives in `src/main/resources/application.yml`. Env vars override:

- `PDF_PASSWORD` — password used to decrypt incoming PDFs (currently the only supported source — there is no per-request password parameter).
- `TESSDATA_PREFIX` — tessdata directory (default `/opt/homebrew/share/tessdata`, the macOS Homebrew location).
- `OCR_LANGUAGES` — Tesseract language string, default `tha+eng`.

Tesseract must be installed on the host (`brew install tesseract tesseract-lang`) — Tess4J calls the native library, so missing binaries fail at runtime, not build time.

Multipart upload limit is 25MB (`spring.servlet.multipart.max-file-size`).

## Conventions from `week-1-assignment.md`

These are explicit project rules, not generic advice:

- **Constructor injection only.** No `@Autowired` on fields — the assignment calls this out as a thing to "unlearn" later if introduced now.
- **Conventional Commits.** No `WIP` or `fix typo` messages; each commit should be reviewable.
- **One milestone at a time.** Don't pre-add JPA/Hibernate, Flyway, or transaction endpoints — they belong to later milestones and the assignment treats scope creep as an anti-pattern.
- A test exists even for the trivial health endpoint; the point is to keep MockMvc wired up from day one.
