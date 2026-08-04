# OpenAPI Creator Studio

Backend for a visual OpenAPI document editor. The project consists of an
independent domain core, OpenAPI format adapters, and a Spring Boot
application.

Documentation:

- English: [`backend-context.md`](docs/en/backend-context.md) and
  architecture docs for
  [`studio-core`](docs/en/architecture/core.md) and
  [`studio-openapi`](docs/en/architecture/openapi.md), and
  [`studio-app`](docs/en/architecture/app.md);
- Русский: [`backend-context.md`](docs/ru/backend-context.md) и
  архитектура
  [`studio-core`](docs/ru/architecture/core.md) и
  [`studio-openapi`](docs/ru/architecture/openapi.md), и
  [`studio-app`](docs/ru/architecture/app.md).

Before changing the backend architecture or code, read the backend context in
either language. For work in `studio-core`, also read the corresponding core
architecture document.

Key rules:

- `studio-core` must not depend on Spring, serialization formats, or storage;
- do not use `var`;
- keep models, DTOs, operation results, and other independent types in
  separate files instead of nesting them inside other classes;
- keep HTTP request and response body fixtures in test resources instead of
  embedding JSON, YAML, or other payloads directly in Java test code;
- when documentation and code disagree, the code is the source of truth;
  update the relevant documentation after an architectural decision changes;
- verify `studio-core` changes with `.\gradlew.bat :studio-core:test` and the
  whole project with `.\gradlew.bat test`.

<!-- CODEGRAPH_START -->
## CodeGraph

In repositories indexed by CodeGraph (a `.codegraph/` directory exists at the repo root), reach for it BEFORE grep/find or reading files when you need to understand or locate code:

- **MCP tool** (when available): `codegraph_explore` answers most code questions in one call — the relevant symbols' verbatim source plus the call paths between them, including dynamic-dispatch hops grep can't follow. Name a file or symbol in the query to read its current line-numbered source. If it's listed but deferred, load it by name via tool search.
- **Shell** (always works): `codegraph explore "<symbol names or question>"` prints the same output.

If there is no `.codegraph/` directory, skip CodeGraph entirely — indexing is the user's decision.
<!-- CODEGRAPH_END -->
