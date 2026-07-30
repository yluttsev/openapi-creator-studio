# Backend context

Language: **English** · [Русский](../ru/backend-context.md)

Status: current backend architecture context as of 2026-07-30.

This document records accepted decisions and module boundaries. Details that
have not been designed yet are marked as planned and must not be treated as
implemented contracts.

## Product purpose

OpenAPI Creator Studio is a visual OpenAPI document editor. Users build an API
through forms: they create paths and operations, parameters, schemas, request
bodies, responses, examples, servers, and security settings. The editor should
display the document structure, validation errors, and documentation preview,
and support importing and exporting OpenAPI documents.

The current priority is a correct backend model for the editor. It must allow
the document to be changed through small operations without coupling domain
logic to YAML, JSON, HTTP, Spring, or a particular storage technology.

## Modules

| Module | Responsibility | Current state |
| --- | --- | --- |
| `studio-core` | In-memory document model, creation, navigation, references, semantic validation, and editing commands | Implemented and covered by tests |
| `studio-openapi` | Conversion between OpenAPI YAML/JSON and the normalized core model, plus version-specific validation | Public contracts, version adapter boundary, and strict YAML/JSON syntax layer implemented; version detection, schema validation, and 3.1 mapping planned |
| `studio-app` | Spring Boot composition root, future REST API, application services, and infrastructure integration | Runnable Spring Boot application created; application architecture is not designed yet |

Dependencies point inward:

```text
studio-app -> studio-openapi -> studio-core
           -> studio-core
```

`studio-core` knows nothing about the two outer modules.

Detailed architecture documentation:

- [architecture/core.md](architecture/core.md);
- [architecture/openapi.md](architecture/openapi.md).

## OpenAPI version strategy

The target and default version for the first release is OpenAPI `3.1.2`. The
current core validator selects semantic rules for the `3.1.x` family. The
syntax parser and writer exist, but version-specific mapping does not, so this
does not mean that end-to-end import and export support is complete.

The architecture is intended to support `3.0.x` and `3.2.x` later:

1. Core stores a normalized document representation.
2. A version adapter converts that version's syntax into the core model.
3. On export, an adapter converts the core model into syntax valid for the
   selected version and reports data that cannot be represented without loss.
4. Structural validation against an official OpenAPI schema belongs in
   `studio-openapi`; checks that require document relationships and semantics
   belong in `studio-core`.

The core model represents the agreed superset of capabilities we need,
including known OpenAPI 3.2 fields such as `Tag.parent`, `Tag.kind`, and
`OpenApiDocument.self`. The presence of these fields does not mean that 3.2
support is implemented. They can be absent for an older version, while the
adapter decides whether they may be serialized.

Version differences are normalized at the boundary. For example:

- OpenAPI 3.0 `nullable: true` is represented in core by the `STRING` and
  `NULL` types;
- schema examples are stored as a collection in core;
- version-specific restrictions must not spread across domain models.

## Preserving unknown data

The editor must not silently discard extensions or fields that it does not
support yet. Descendants of `ExtensibleObject` contain `additionalFields`, and
`SchemaDefinition` contains `additionalKeywords`. Their values are represented
by `DocumentValue`.

A format adapter must place unknown values into these containers and emit them
again during serialization when the target format permits it.

## Editing a document

A document in `studio-core` is a mutable in-memory object graph. User actions
should be expressed as small `DocumentCommand` implementations rather than
arbitrary model mutations at the REST boundary.

A command:

- knows which change it performs;
- receives a `CommandContext` with the document, navigator, and `$ref` index;
- checks expected rejection reasons before making a mutation;
- either changes the document atomically and returns changed paths, or returns
  structured issues.

The future application layer will be responsible for loading the document,
transactions, authorization, concurrent editing, and persisting the result.
These responsibilities do not belong in core.

## Validation

Validation is split into two levels:

- `studio-openapi`: YAML/JSON parsing and conformance to the structure of the
  selected OpenAPI version;
- `studio-core`: semantic rules that need a connected document graph, such as
  uniqueness, `$ref` integrity, path templates, security requirements, links,
  callbacks, encoding, discriminators, and servers.

Temporarily invalid document states are allowed during visual editing.
Validation reports issues but does not mutate the document. Commands reject
only actions that cannot be performed safely or unambiguously.

Import is stricter than interactive editing. Any parsing, structural, mapping,
or semantic error produces `ImportFailure` and no document is returned.
Warnings do not block an import.

## Storage and document size

For the first release, the entire document lives in memory. Core contains no
repository, ORM, or other persistence abstractions. Storage is connected from
the outside and can change without modifying the domain model.

Partial loading, a database, or graph storage is not a current decision. These
options should be considered only after measuring real problems with document
size, traversal time, or reference indexing.

## Frontend boundary

A separate React interface is planned. In production, its static build may be
served by the Spring Boot application, but JavaScript still executes in the
browser and communicates with the backend through a REST API. REST contracts
and application services have not been defined yet.

## What already exists in `studio-core`

- the base model for a complete OpenAPI document and arbitrary document
  values;
- a new-document factory;
- JSON Pointer addressing and graph traversal;
- local `$ref` resolution, a usage index, and safe reference updates;
- a set of OpenAPI 3.1 semantic rules;
- commands for components, schemas, paths, and operations;
- unit tests for these subsystems.

## Deferred decisions

- version detection and import/export orchestration in `studio-openapi`;
- pinned structural schemas and OpenAPI 3.1 mapping;
- REST API and DTO shape;
- document sessions, persistence, and concurrent editing;
- undo/redo and command history;
- force deletion of objects with active references;
- index optimization for very large documents;
- detailed OpenAPI 3.0 and 3.2 support.

When another module is designed, create a separate document for it under
`docs/architecture/` and leave only an overview and a link here.
