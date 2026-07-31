package ru.luttsev.studio.openapi.version.v31.decode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import ru.luttsev.studio.core.model.OpenApiDocument;
import ru.luttsev.studio.core.model.OpenApiVersion;
import ru.luttsev.studio.core.model.security.SecurityRequirement;
import ru.luttsev.studio.core.model.server.Server;
import ru.luttsev.studio.core.model.tag.Tag;
import ru.luttsev.studio.core.model.value.ObjectValue;
import ru.luttsev.studio.core.model.value.StringValue;
import ru.luttsev.studio.openapi.diagnostic.OpenApiDiagnostic;
import ru.luttsev.studio.openapi.importing.ImportOptions;
import ru.luttsev.studio.openapi.result.AdapterFailure;
import ru.luttsev.studio.openapi.result.AdapterResult;
import ru.luttsev.studio.openapi.result.AdapterSuccess;
import ru.luttsev.studio.openapi.result.SyntaxSuccess;
import ru.luttsev.studio.openapi.syntax.JacksonOpenApiSyntaxCodec;
import ru.luttsev.studio.openapi.syntax.ParsedDocument;

class RootFieldsDecoderTest {

    private final OpenApi31Decoder decoder = new OpenApi31Decoder();

    @Test
    void decodesRemainingOpenApi31RootFields() {
        ObjectValue source = parse("""
                openapi: 3.1.2
                info:
                  title: Events API
                  version: 1.0.0
                servers:
                  - url: https://{environment}.example.com
                    description: Main server
                    variables:
                      environment:
                        default: api
                        enum: [api, staging]
                    x-server-owner: platform
                paths: {}
                security:
                  - OAuth2: [events:read]
                    ApiKey: []
                  - {}
                tags:
                  - name: events
                    description: Event operations
                    externalDocs:
                      description: Event guide
                      url: https://example.com/docs/events
                      x-doc-id: events
                    summary: OpenAPI 3.2 summary
                    parent: platform
                    kind: nav
                    x-tag-owner: events-team
                externalDocs:
                  description: API documentation
                  url: https://example.com/docs
                  x-doc-version: current
                x-root-owner: platform
                """);

        OpenApiDocument document = successValue(
                decoder.decode(source, OpenApiVersion.V3_1_2));

        Server server = document.getServers().getFirst();
        assertEquals("https://{environment}.example.com", server.getUrl());
        assertEquals(
                List.of("api", "staging"),
                server.getVariables().get("environment").getEnumValues());
        assertEquals(
                new StringValue("platform"),
                server.getAdditionalFields().get("x-server-owner"));

        List<SecurityRequirement> security = document.getSecurity();
        assertEquals(
                List.of("events:read"),
                security.getFirst().getRequirements().get("OAuth2"));
        assertTrue(security.getFirst().getRequirements().get("ApiKey").isEmpty());
        assertTrue(security.get(1).getRequirements().isEmpty());

        Tag tag = document.getTags().getFirst();
        assertEquals("events", tag.getName());
        assertEquals("Event operations", tag.getDescription());
        assertEquals(
                "https://example.com/docs/events",
                tag.getExternalDocs().getUrl().value());
        assertEquals(
                new StringValue("events"),
                tag.getExternalDocs().getAdditionalFields().get("x-doc-id"));
        assertNull(tag.getSummary());
        assertNull(tag.getParent());
        assertNull(tag.getKind());
        assertEquals(
                new StringValue("OpenAPI 3.2 summary"),
                tag.getAdditionalFields().get("summary"));
        assertEquals(
                new StringValue("platform"),
                tag.getAdditionalFields().get("parent"));
        assertEquals(
                new StringValue("nav"),
                tag.getAdditionalFields().get("kind"));
        assertEquals(
                new StringValue("events-team"),
                tag.getAdditionalFields().get("x-tag-owner"));

        assertEquals(
                "https://example.com/docs",
                document.getExternalDocs().getUrl().value());
        assertEquals(
                new StringValue("current"),
                document.getExternalDocs()
                        .getAdditionalFields()
                        .get("x-doc-version"));
        assertEquals(
                new StringValue("platform"),
                document.getAdditionalFields().get("x-root-owner"));
        assertFalse(document.getAdditionalFields().containsKey("servers"));
        assertFalse(document.getAdditionalFields().containsKey("security"));
        assertFalse(document.getAdditionalFields().containsKey("tags"));
        assertFalse(document.getAdditionalFields().containsKey("externalDocs"));
    }

    @Test
    void reportsRootFieldMappingErrorsAtExactPaths() {
        ObjectValue source = parse("""
                openapi: 3.1.2
                info:
                  title: Broken API
                  version: 1.0.0
                paths: {}
                servers:
                  - invalid
                  - description: Missing URL
                security:
                  - invalid
                  - OAuth2: invalid
                tags:
                  - invalid
                  - description: Missing name
                  - name: Invalid docs
                    externalDocs: invalid
                  - name: Missing docs URL
                    externalDocs:
                      description: Missing URL
                externalDocs: invalid
                """);

        AdapterFailure<?> failure = assertInstanceOf(
                AdapterFailure.class,
                decoder.decode(source, OpenApiVersion.V3_1_2));
        Set<String> diagnosticPaths = failure.diagnostics().stream()
                .map(OpenApiDiagnostic::path)
                .map(path -> path.toPointer())
                .collect(Collectors.toSet());

        assertEquals(Set.of(
                "/servers/0",
                "/servers/1/url",
                "/security/0",
                "/security/1/OAuth2",
                "/tags/0",
                "/tags/1/name",
                "/tags/2/externalDocs",
                "/tags/3/externalDocs/url",
                "/externalDocs"), diagnosticPaths);
    }

    private static OpenApiDocument successValue(
            AdapterResult<OpenApiDocument> result) {
        AdapterSuccess<?> success = assertInstanceOf(
                AdapterSuccess.class,
                result);
        assertTrue(success.diagnostics().isEmpty());
        return assertInstanceOf(OpenApiDocument.class, success.value());
    }

    private static ObjectValue parse(String content) {
        JacksonOpenApiSyntaxCodec codec = new JacksonOpenApiSyntaxCodec();
        SyntaxSuccess<?> success = assertInstanceOf(
                SyntaxSuccess.class,
                codec.parse(content, ImportOptions.autoDetect()));
        ParsedDocument parsedDocument = assertInstanceOf(
                ParsedDocument.class,
                success.value());
        return parsedDocument.root();
    }
}
