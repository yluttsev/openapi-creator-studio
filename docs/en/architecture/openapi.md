# `studio-openapi` architecture

Language: **English** · [Русский](../../ru/architecture/openapi.md)

Status: public contracts, the version adapter boundary, and the YAML/JSON
syntax layer are implemented as of 2026-07-30. Version detection, structural
validation, and OpenAPI 3.1 mapping are planned.

The source code is located under
`studio-openapi/src/main/java/ru/luttsev/studio/openapi`.

## Purpose

`studio-openapi` is the boundary between external OpenAPI YAML/JSON and the
normalized `studio-core` model. It understands syntax and version-specific
representation, while core understands document meaning and relationships.

The module is responsible for:

- parsing and serializing YAML/JSON;
- detecting the OpenAPI version;
- structural validation against a pinned official schema;
- mapping a version-specific representation to and from core;
- preserving unknown fields and extensions;
- reporting structured import and export diagnostics.

It is not responsible for persistence, HTTP, authorization, request logging,
or UI concerns.

## Processing pipeline

Strict import:

```text
content
-> parse
-> detect version
-> select adapter
-> structural validation
-> decode into core
-> core semantic validation
-> ImportSuccess or ImportFailure
```

Any error produces `ImportFailure` without a document. Warnings do not block
the operation.

Export follows the reverse direction. It validates the core document and
target-version compatibility before serialization. Export never silently
drops unsupported data.

Semantic round-trip is required. Original comments, YAML anchors, quoting,
whitespace, and field formatting are not preserved.

## Public contracts

`OpenApiImporter` accepts text content and `ImportOptions`. The format can be
given explicitly or detected automatically.

`OpenApiExporter` accepts an `OpenApiDocument` and `ExportOptions` containing
the target format and OpenAPI version.

The sealed results are:

- `ImportSuccess` or `ImportFailure`;
- `ExportSuccess` or `ExportFailure`;
- `SyntaxSuccess<T>` or `SyntaxFailure<T>` for the internal syntax boundary;
- `AdapterSuccess<T>` or `AdapterFailure<T>` for the internal adapter
  boundary.

A successful result may contain warnings but cannot contain errors. A failed
result must contain at least one error.

## Diagnostics

`OpenApiDiagnostic` contains:

- a stable `DiagnosticCode`;
- `ERROR` or `WARNING` severity;
- processing phase;
- a user-facing message;
- `DocumentPath`;
- an optional one-based source line and column.

Phases cover parsing, version detection, structural validation, mapping,
semantic validation, target-version compatibility, and serialization.

Expected document problems are returned as diagnostics. Unexpected programming
or infrastructure failures may propagate as exceptions and are logged by
`studio-app`, not by this module.

## Syntax tree boundary

Parser-specific types such as Jackson `JsonNode` must not appear in public
contracts or version adapters. The shared syntax tree passed to adapters is
the core `ObjectValue`, whose nested values are `DocumentValue`.

This keeps parser replacement possible and lets unknown values move into
`additionalFields` or `additionalKeywords` without losing their structure.

`OpenApiSyntaxCodec` combines the parser and writer contracts. Its current
implementation, `JacksonOpenApiSyntaxCodec`, uses Jackson 3.2.1 internally
for strict JSON and YAML processing. Jackson types remain package-internal
implementation details.

An explicit `ImportOptions` format hint takes precedence. Without a hint,
content beginning with `{` or `[` after an optional byte-order mark and
whitespace is treated as JSON; other content is treated as YAML. Malformed
JSON is not retried as YAML.

The syntax layer rejects empty input, a non-object document root, duplicate
properties, trailing content, and multiple YAML documents. Numbers are parsed
without conversion through `double`. Serialization produces readable,
deterministic JSON or YAML, but does not preserve source formatting.

## Version adapters

`OpenApiVersionAdapter` is the strategy for one version family. It declares
whether it supports a version and provides:

- `decode(ObjectValue, OpenApiVersion)`;
- `encode(OpenApiDocument, OpenApiVersion)`.

`OpenApiVersionAdapterRegistry` selects exactly one matching adapter. Missing
support is an expected import/export diagnostic. More than one matching
adapter is a configuration error.

The first implementation will target OpenAPI 3.1.2. OpenAPI 3.0 and 3.2 will
be added as separate adapters rather than conditional branches spread across
the module.

## Planned implementation sequence

1. YAML/JSON syntax layer. Implemented.
2. Version detector and import/export orchestration.
3. Pinned OpenAPI 3.1 structural schema and validator.
4. OpenAPI 3.1 decoder.
5. OpenAPI 3.1 encoder.
6. Strict semantic validation integration.
7. Round-trip and fixture-based integration tests.

Official schemas will be stored as versioned resources. They will not be
downloaded at application runtime.

## Testing

Tests enforce option, diagnostic, result, registry, strict syntax, numeric
precision, and JSON/YAML round-trip invariants. Each later layer must add
valid, invalid, and round-trip fixtures.

Run:

```powershell
.\gradlew.bat :studio-openapi:test
```
