# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

A Spring Boot 3.4.1 / Java 17 service that accepts legal judgment XML uploads, validates them against an XSD, transforms them via an XSLT 3.0 (Saxon-HE) stylesheet into a normalized JSON record, derives a plain-text RAG-ready artifact from it, and persists both to disk keyed by `content_id`. Single REST endpoint, no database — the filesystem is the source of truth for "already processed."

## Required environment variables

The app (and `@SpringBootTest`, and therefore `mvn test`) will **fail to start** without these — `application.yaml` references them with no defaults:

- `DISK_SAVER_PATH` — base directory where `<content_id>/<content_id>.json` and `<content_id>.txt` are written.
- `ASYNC_POOL_SIZE` — thread pool size (and queue capacity) for the async processing executor.

Example: `DISK_SAVER_PATH=/tmp/xtj-output ASYNC_POOL_SIZE=4 mvn test`

## Common commands

```bash
# Run tests (env vars above are required)
DISK_SAVER_PATH=/tmp/xtj-output ASYNC_POOL_SIZE=4 mvn test

# Run a single test class
DISK_SAVER_PATH=/tmp/xtj-output ASYNC_POOL_SIZE=4 mvn test -Dtest=XmlToJsonPublisherApplicationTests

# Build
mvn compile

# Run locally
DISK_SAVER_PATH=/tmp/xtj-output ASYNC_POOL_SIZE=4 mvn spring-boot:run

# Containerize to the local Docker daemon (requires Docker installed)
mvn compile jib:dockerBuild

# Build/push to a remote registry (update <image> in pom.xml's jib-maven-plugin config first)
mvn compile jib:build
```

There is no Maven wrapper (`mvnw`) — use a system-installed `mvn`.

## Request/processing pipeline

`POST /upload-xml` (`multipart/form-data`, one or more `file` parts) is the sole entry point (`XmlToJsonPublisherController`). Each uploaded file is dispatched independently and processed **fully in parallel** via `XmlProcessorService.processXmlContent` (annotated `@Async`, backed by the executor in `AsyncConfiguration`). The controller joins all the resulting futures and returns `200 OK` with a `List<FileOutcome>` — per-file success/failure is reported in the response body, not via HTTP status, so one bad file in a batch never fails the whole request.

Per file, in order:

1. **Cheap content_id extraction** (`XmlUtil.getXmlAttributeValueFromStream`, backed by StAX `XMLStreamReader`) — pulls just `content_id` without building a full tree, so a duplicate/already-processed file is rejected before paying for schema validation or XSLT transformation.
2. **Per-content-id lock** (`ContentLockRegistry`, a `ConcurrentHashMap<String, ReentrantLock>`) is acquired, keyed on `content_id` — this is what makes duplicate-detection safe under concurrency, not just under time. Two uploads of the same `content_id` arriving together serialize on this lock; unrelated `content_id`s continue processing fully in parallel. The "already processed?" check is deliberately re-run *inside* the lock (`XmlValidator.validateCanProcess`) because the file could have finished processing while this request was waiting on the lock.
3. **XSD schema validation** (`XmlValidator` / `JavaxXmlConfiguration`) against `src/main/resources/xsd.xml`.
4. **XML → JSON transform** (`XmlTransformer`), via Saxon-HE's XSLT 3.0 processor running `src/main/resources/judgment-to-json.xsl` (uses `xsl:map`/`array {}` and `xsl:output method="json"` to serialize directly to JSON — no manual DOM walking). The resulting JSON is deserialized into the `NormalizedJudgment` record via Jackson.
5. **RAG text derivation** (`RagTransformer` interface, currently one implementation `StrategyOneRagTransformer`) builds a plain-text artifact from the already-parsed `NormalizedJudgment` — title as a header line, paragraphs joined by blank lines — deliberately *not* another XSLT/Saxon pass. `RagTransformer` is an interface specifically so alternative RAG-text strategies can be added and swapped in without touching the pipeline.
6. **Persistence** (`Saver` interface, implementation `DiskSaver`) writes `$DISK_SAVER_PATH/<content_id>/<content_id>.json` and `<content_id>.txt`. `Saver` is also the source of truth for "does this content_id already exist" (`Saver.exists`), which is what step 1's duplicate check and step 2's re-check both call into.

## Saxon / JAXP thread-safety pattern — read before touching config or transformer/validator code

This codebase follows one repeated pattern across both its XML validation and XSLT transformation, and it's easy to accidentally regress:

- **Compile/parse once at startup, execute fresh per call.** `Schema` (`JavaxXmlConfiguration`) and `XsltExecutable` (`SaxonConfiguration`) are the singleton Spring beans — both are documented thread-safe and expensive to rebuild. `Validator` (`schema.newValidator()`) and `Xslt30Transformer` (`xsltExecutable.load30()`) are **not** thread-safe and must never be beans — they're constructed fresh per validation/transform call (see `XmlValidator.getSchemaValidator()` and `XmlTransformer.transform()`).
- **Do not make `Xslt30Transformer` or `Validator` singleton beans.** Saxon's own Javadoc states `Xslt30Transformer` "must not be used concurrently in multiple threads" — reusing one across concurrent requests produces silently wrong output (not just a crash), not a hypothetical concern given this service processes uploads concurrently by design.
- **`XMLInputFactory` is deliberately never a shared bean either** (`XmlUtil.getXmlInputFactory()`) — StAX implementations cache a mutable symbol table on the factory itself, so a shared factory used concurrently from multiple threads corrupts that structure. A fresh factory is constructed per call.
- **DTD support and external entities are explicitly disabled everywhere untrusted XML is parsed** (`XMLInputFactory.SUPPORT_DTD` / `IS_SUPPORTING_EXTERNAL_ENTITIES` in `XmlUtil`; `ACCESS_EXTERNAL_DTD` / `ACCESS_EXTERNAL_SCHEMA` in `JavaxXmlConfiguration`) as XXE/billion-laughs mitigation — any new code path that parses uploaded XML needs the same treatment, not just the existing ones.

If you add a new Saxon or JAXP component, apply the same split: whatever the vendor docs mark thread-safe becomes the bean; whatever isn't gets instantiated per-call from that bean.

## Concurrency / backpressure

`AsyncConfiguration` sizes the executor's queue capacity equal to the pool size (both from `ASYNC_POOL_SIZE`) and sets `CallerRunsPolicy` as the rejection handler — once the pool and its queue are both full, the *calling* thread (the HTTP request thread) runs the task itself instead of the task being rejected with a 500. This bounds memory (no unbounded queuing) while degrading to synchronous processing under load rather than failing the request.

Uploads are capped at 1MB/file and 10MB/request (Spring Boot's multipart defaults, configurable) — this is why the endpoint uses `multipart/form-data` rather than a raw XML body, to get file-count and size limiting for free.

## Design decisions and rationale

`SOLUTION.md`'s "Decisions Made" section documents the reasoning behind less-obvious choices in this codebase (why s9api over JAXP for XSLT, why DOM tree model is avoided, why `NormalizedJudgment` is a record, why `Saver`/`RagTransformer` are interfaces, why a `ReentrantLock` rather than a read/write lock, etc.) — check there before second-guessing an existing pattern. It also covers deployment (Cloud Run via the `jib-maven-plugin` image), the `/actuator/health` and `/actuator/prometheus` endpoints for observability, and a "RAG pipeline" section sketching how the persisted `.txt`/`.json` pairs would feed a downstream ingestion service in the future (not yet implemented).

## Documentation map

- `README.md` — entry point: how to run (env vars, `mvn spring-boot:run`), and links out to everything else.
- `SOLUTION.md` — the "Decisions Made" rationale above, plus the answers to the assignment's Task 3 discussion points.
- `docs/process-flow.puml` — PlantUML activity diagram of the end-to-end upload → validate → transform → save pipeline (rendered copy: `docs/process_flow-XML_to_JSON_Publisher___Process_Flow.png`). Keep this in sync if the pipeline steps in `XmlProcessorService` change.
