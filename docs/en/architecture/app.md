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
between generated DTOs and core types explicitly.

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

## Planned application layers

Upcoming work adds document lifecycle services, REST/core mappers, typed command handlers,
manual validation and export services, controllers, and Problem Details error mapping.
