# `studio-app` architecture

Language: **English** · [Русский](../../ru/architecture/app.md)

`studio-app` is the Spring Boot composition root and application layer. It exposes
the REST API, coordinates `studio-core` and `studio-openapi`, and connects replaceable
infrastructure adapters.

## REST contract

The REST API is contract-first. Its OpenAPI 3.1 document is stored in
`studio-app/src/main/resources/static/openapi/studio-api.yaml`. OpenAPI Generator
produces API interfaces and transport models under `build/generated/openapi`; generated
sources are build artifacts and are not committed. Hand-written controllers implement
the generated interfaces.

Generated transport types must not be used by `studio-core`. The application layer maps
between generated DTOs and core types explicitly. Transport mappers use MapStruct with
Spring component and constructor injection; HTTP status and header construction remains
in controllers.

## Document workspace

`DocumentWorkspace` is the application port that owns open editor sessions. A session
contains a UUID, a non-negative revision, and one mutable core `OpenApiDocument` graph.
The initial revision is `0`.

The first release uses `InMemoryDocumentWorkspace`. The port supports creating, finding,
deleting, and atomically executing a `DocumentCommand` against a document. Commands use
optimistic concurrency:

- the supplied revision must equal the current revision;
- a successful core command increments the revision once;
- a rejected core command does not change the revision;
- a stale revision produces `RevisionConflictException` before command execution.

Each stored document serializes command execution independently. The concurrent map only
protects the collection of sessions. The workspace takes ownership of a document passed
to `create`; callers must not mutate that graph outside workspace operations.

The port keeps storage replaceable. A future persistent adapter may implement the same
operations with database transactions or distributed optimistic locking without changing
the REST contract or core model.

Read-only operations use `DocumentWorkspace.inspect`. The inspection callback runs under
the same per-document lock as commands, so the observed revision and the result derived
from the document belong to one consistent snapshot. The callback must not retain or
expose the mutable document graph.

## Document lifecycle

The first application slice implements the generated `DocumentsApi` contract.
It creates blank OpenAPI 3.1 documents, strictly imports YAML or JSON, returns
the current standard JSON representation, and closes document sessions.

`DocumentLifecycleService` coordinates the core document factory, the OpenAPI
importer, and `DocumentWorkspace`. `DocumentRepresentationService` uses the
version adapter without running export orchestration, so an intermediate editor
state can still be returned to the UI. REST mapping and diagnostic conversion
remain in the web layer.

Creation and import return revision `0`, `Location`, and a strong ETag containing
the revision. Closing requires the same revision through `If-Match`; checking
and removal are atomic in the in-memory workspace.

## Command dispatch

The generated polymorphic `DocumentCommandRequest` is handled by a registry of typed
`DocumentCommandRequestHandler` components. Each handler translates exactly one REST
request class into a core `DocumentCommand`; adding a command does not require modifying
the dispatcher. Duplicate registrations are rejected at startup.

`DocumentCommandService` executes the translated command through `DocumentWorkspace`.
A successful command returns the new revision and changed JSON Pointer paths. A rejected
core command keeps the revision unchanged and becomes a `409` Problem Details response
with structured command issues. Missing sessions and stale revisions use the shared
`404` and `412` mappings.

## Manual validation

`DocumentValidationService` runs the core `DocumentValidator` through a workspace
inspection. Validation does not mutate the document or increment its revision. The REST
response contains that revision, the aggregate validity flag, and semantic diagnostics;
the same revision is returned as a strong ETag.

## Document export

`DocumentExportService` runs the strict `OpenApiExporter` pipeline through a workspace
inspection. The client selects YAML or JSON and may select a target OpenAPI version;
when omitted, the target defaults to `3.1.2`. A successful response contains the
consistent revision, format, target version, suggested file name, serialized content,
and non-error diagnostics. Export failures use the shared OpenAPI processing error.

## Problem Details

All application and Spring MVC request failures use the generated Problem Details
models and `application/problem+json`. Malformed or missing JSON, unknown enum values,
invalid parameter formats, and Bean Validation failures produce `400`; validation
violations include stable field names and safe messages. A missing `If-Match` header is
distinguished as `428`, while document, concurrency, command, and OpenAPI processing
failures retain their `404`, `412`, `409`, and `422` responses.

## Planned application layers

Upcoming work adds HTTP integration tests.
