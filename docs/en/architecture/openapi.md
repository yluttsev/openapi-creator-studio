# `studio-openapi` architecture

Language: **English** · [Русский](../../ru/architecture/openapi.md)

Status: public contracts, the version adapter boundary, the YAML/JSON syntax
layer, version detection, and OpenAPI 3.1 structural validation are
implemented as of 2026-07-31. The OpenAPI 3.1 decoder foundation and
root/info, paths, operations, callbacks, webhooks, schema, payload, security
scheme, and link mapping are implemented; the remaining mapping is in
progress.

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
- `DetectedVersion` or `VersionDetectionFailure` for version detection;
- `StructuralValidationSuccess` or `StructuralValidationFailure` for
  structural validation;
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

## Version detection

`OpenApiVersionDetector` reads the required root `openapi` field from an
`ObjectValue`. The default implementation accepts a string in
`major.minor.patch` format and preserves the exact value in `OpenApiVersion`.

A missing field, a non-string value, or an invalid format produces a structured
error diagnostic at `/openapi`. Detection does not decide whether the version
is supported. That decision belongs to `OpenApiVersionAdapterRegistry`, so a
well-formed future version can be detected and then rejected as unsupported.

## Structural validation

`OpenApiStructuralValidator` validates the syntax tree before version-specific
mapping. `OpenApiStructuralSchemaRegistry` selects exactly one schema provider
for the detected version, which keeps support for OpenAPI 3.0 and 3.2 additive.

The current provider supports every OpenAPI 3.1 patch release. It uses the
pinned `2025-11-23` official `schema-base` resource and the OpenAPI 3.1 base
dialect, so Schema Objects are validated as well as the rest of the document.
Custom `jsonSchemaDialect` values are outside the first-release scope.

All schema resources and their transitive OpenAPI dialect dependencies are
packaged in the application. Validation is deterministic and performs no
runtime network requests. Compiled schemas are cached by root schema ID.
Expected violations become stable `openapi.structure.*` diagnostics with
`DocumentPath`; unsupported versions are reported at `/openapi`.

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

## OpenAPI 3.1 decoder

`OpenApi31Decoder` converts a structurally validated `ObjectValue` into the
normalized core `OpenApiDocument`. It does not run JSON Schema validation
again. Defensive typed reads still produce mapping diagnostics instead of
unchecked cast failures when the decoder is called directly.

`DecodeContext` carries the current `DocumentPath` and shares one diagnostic
collector with all child contexts. `ObjectValueReader` centralizes typed
field access. Small domain decoders map individual model families, while
`AdditionalFieldsMapper` preserves every field not consumed by that decoder.

The current vertical slice covers the root version, `jsonSchemaDialect`,
`Info`, `Contact`, `License`, root `paths`, Path Item Objects, standard HTTP
operations, and the schema, payload, security scheme, and link sections of
`components`. `SchemaDecoder` recursively maps boolean and object schemas,
including properties, items, constraints, composition, discriminators,
references, and unknown JSON Schema keywords. Singular `example` and the
`examples` array are normalized into the core examples list.

The payload layer maps Example, Parameter, Header, Request Body, Response,
Media Type, and Encoding Objects. A shared `ReferenceOrDecoder` distinguishes
inline values from Reference Objects, while `PayloadDecoder` coordinates the
recursive Media Type, Encoding, and Header graph without cyclic constructor
dependencies. Response links and shared component links are both mapped by
`LinkDecoder`. Reusable decoders map Server, Server Variable, External
Documentation, and Security Requirement Objects at path and operation level.
`SecuritySchemeDecoder` maps API key, HTTP, mutual TLS, OAuth 2.0, and OpenID
Connect schemes together with all OpenAPI 3.1 OAuth flows. Operation responses
are decoded into the extensible core `Responses` model, preserving response
object extensions beside typed status-code entries. In OpenAPI 3.1, media
types are decoded inside `content`; `components.mediaTypes` is intentionally
not accepted because it belongs to a later OpenAPI version.

Operation callbacks, root webhooks, and Callback and Path Item component
sections are mapped. Callback expressions recursively reuse `PathItemDecoder`,
while callback references use the common `ReferenceOrDecoder`. Callback
extensions remain in `Callback.additionalFields`. Standard root fields that
are not mapped yet remain in `OpenApiDocument.additionalFields`; they will move
to typed core fields as the decoder expands.

## Planned implementation sequence

1. YAML/JSON syntax layer. Implemented.
2. Version detector. Implemented.
3. Pinned OpenAPI 3.1 structural schema and validator. Implemented.
4. OpenAPI 3.1 decoder. In progress: foundation, root/info, paths, operations,
   callbacks, webhooks, schemas, payload, security schemes, and links
   implemented.
5. OpenAPI 3.1 encoder.
6. Import/export orchestration.
7. Strict semantic validation integration.
8. Round-trip and fixture-based integration tests.

Official schemas will be stored as versioned resources. They will not be
downloaded at application runtime.

## Testing

Tests enforce option, diagnostic, result, registry, strict syntax, version
detection, structural validation, numeric precision, and JSON/YAML round-trip
invariants. Each later layer must add valid, invalid, and round-trip fixtures.

Run:

```powershell
.\gradlew.bat :studio-openapi:test
```
