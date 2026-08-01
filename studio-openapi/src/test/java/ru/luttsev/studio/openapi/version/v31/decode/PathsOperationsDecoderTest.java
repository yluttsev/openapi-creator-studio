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
import ru.luttsev.studio.core.model.media.MediaTypeName;
import ru.luttsev.studio.core.model.parameter.Parameter;
import ru.luttsev.studio.core.model.parameter.ParameterLocation;
import ru.luttsev.studio.core.model.path.HttpMethod;
import ru.luttsev.studio.core.model.path.Operation;
import ru.luttsev.studio.core.model.path.PathItem;
import ru.luttsev.studio.core.model.reference.InlineObject;
import ru.luttsev.studio.core.model.reference.ReferenceObject;
import ru.luttsev.studio.core.model.response.ApiResponse;
import ru.luttsev.studio.core.model.response.ResponseKey;
import ru.luttsev.studio.core.model.security.SecurityRequirement;
import ru.luttsev.studio.core.model.server.Server;
import ru.luttsev.studio.core.model.value.ObjectValue;
import ru.luttsev.studio.core.model.value.StringValue;
import ru.luttsev.studio.openapi.diagnostic.OpenApiDiagnostic;
import ru.luttsev.studio.openapi.diagnostic.OpenApiDiagnosticCodes;
import ru.luttsev.studio.openapi.importing.ImportOptions;
import ru.luttsev.studio.openapi.result.AdapterFailure;
import ru.luttsev.studio.openapi.result.AdapterResult;
import ru.luttsev.studio.openapi.result.AdapterSuccess;
import ru.luttsev.studio.openapi.result.SyntaxSuccess;
import ru.luttsev.studio.openapi.syntax.JacksonOpenApiSyntaxCodec;
import ru.luttsev.studio.openapi.syntax.ParsedDocument;

class PathsOperationsDecoderTest {

    private final OpenApi31Decoder decoder = new OpenApi31Decoder();

    @Test
    void decodesPathsPathItemsAndOperations() {
        ObjectValue source = parse("""
                openapi: 3.1.2
                info:
                  title: Users API
                  version: 1.0.0
                paths:
                  x-paths-owner: platform
                  /users/{id}:
                    summary: User resource
                    description: Operations on a user
                    parameters:
                      - $ref: "#/components/parameters/UserId"
                    servers:
                      - url: https://{environment}.example.com
                        description: Path server
                        variables:
                          environment:
                            default: api
                            enum: [api, staging]
                        x-server-level: path
                    get:
                      tags: [users, read]
                      summary: Get user
                      description: Finds one user
                      externalDocs:
                        description: User guide
                        url: https://example.com/docs/users
                        x-doc-id: users
                      operationId: getUser
                      parameters:
                        - name: include
                          in: query
                          schema:
                            type: string
                      requestBody:
                        $ref: "#/components/requestBodies/SearchOptions"
                      responses:
                        "200":
                          description: User found
                          content:
                            application/json:
                              schema:
                                $ref: "#/components/schemas/User"
                        2XX:
                          $ref: "#/components/responses/Success"
                        default:
                          $ref: "#/components/responses/Error"
                        x-display-group: Users
                      deprecated: false
                      security:
                        - OAuth2: [users:read]
                          ApiKey: []
                        - {}
                      servers:
                        - url: https://api.example.com
                      callbacks: {}
                      x-operation-owner: users-team
                    x-path-item-id: users-by-id
                  /shared:
                    $ref: "#/components/pathItems/Shared"
                    summary: Shared operations
                """);

        OpenApiDocument document = successValue(
                decoder.decode(source, OpenApiVersion.V3_1_2));

        assertNull(document.getAdditionalFields().get("paths"));
        assertEquals(
                new StringValue("platform"),
                document.getPaths().getAdditionalFields().get("x-paths-owner"));

        PathItem pathItem = document.getPaths().getItems().get("/users/{id}");
        assertEquals("User resource", pathItem.getSummary());
        assertEquals("Operations on a user", pathItem.getDescription());
        assertInstanceOf(ReferenceObject.class, pathItem.getParameters().getFirst());
        Server pathServer = pathItem.getServers().getFirst();
        assertEquals("https://{environment}.example.com", pathServer.getUrl());
        assertEquals(
                List.of("api", "staging"),
                pathServer.getVariables().get("environment").getEnumValues());
        assertEquals(
                new StringValue("path"),
                pathServer.getAdditionalFields().get("x-server-level"));
        assertEquals(
                new StringValue("users-by-id"),
                pathItem.getAdditionalFields().get("x-path-item-id"));

        Operation operation = pathItem.getOperations().get(HttpMethod.GET);
        assertEquals(List.of("users", "read"), operation.getTags());
        assertEquals("Get user", operation.getSummary());
        assertEquals("Finds one user", operation.getDescription());
        assertEquals("getUser", operation.getOperationId());
        assertFalse(operation.getDeprecated());
        assertEquals(
                "https://example.com/docs/users",
                operation.getExternalDocs().getUrl().value());
        assertEquals(
                new StringValue("users"),
                operation.getExternalDocs()
                        .getAdditionalFields()
                        .get("x-doc-id"));

        Parameter parameter = inlineValue(
                operation.getParameters().getFirst(),
                Parameter.class);
        assertEquals("include", parameter.getName());
        assertEquals(ParameterLocation.QUERY, parameter.getLocation());
        ReferenceObject<?> requestBody = assertInstanceOf(
                ReferenceObject.class,
                operation.getRequestBody());
        assertEquals(
                "#/components/requestBodies/SearchOptions",
                requestBody.getRef().value());

        ApiResponse response = inlineValue(
                operation.getResponses()
                        .getValues()
                        .get(new ResponseKey("200")),
                ApiResponse.class);
        assertEquals("User found", response.getDescription());
        assertTrue(response.getContent().containsKey(
                MediaTypeName.APPLICATION_JSON));
        assertInstanceOf(
                ReferenceObject.class,
                operation.getResponses()
                        .getValues()
                        .get(new ResponseKey("2XX")));
        assertInstanceOf(
                ReferenceObject.class,
                operation.getResponses()
                        .getValues()
                        .get(ResponseKey.DEFAULT));
        assertEquals(
                new StringValue("Users"),
                operation.getResponses()
                        .getAdditionalFields()
                        .get("x-display-group"));

        List<SecurityRequirement> security = operation.getSecurity();
        assertEquals(List.of("users:read"),
                security.getFirst().getRequirements().get("OAuth2"));
        assertTrue(security.getFirst().getRequirements().get("ApiKey").isEmpty());
        assertTrue(security.get(1).getRequirements().isEmpty());
        assertEquals("https://api.example.com", operation.getServers().getFirst().getUrl());
        assertTrue(operation.getCallbacks().isEmpty());
        assertNull(operation.getAdditionalFields().get("callbacks"));
        assertEquals(
                new StringValue("users-team"),
                operation.getAdditionalFields().get("x-operation-owner"));

        PathItem shared = document.getPaths().getItems().get("/shared");
        assertEquals(
                "#/components/pathItems/Shared",
                shared.getRef().value());
        assertEquals("Shared operations", shared.getSummary());
    }

    @Test
    void decodesEveryOpenApi31HttpMethod() {
        ObjectValue source = parse("""
                openapi: 3.1.2
                info:
                  title: Methods API
                  version: 1.0.0
                paths:
                  /methods:
                    get:
                      responses: { "200": { description: OK } }
                    put:
                      responses: { "200": { description: OK } }
                    post:
                      responses: { "200": { description: OK } }
                    delete:
                      responses: { "200": { description: OK } }
                    options:
                      responses: { "200": { description: OK } }
                    head:
                      responses: { "200": { description: OK } }
                    patch:
                      responses: { "200": { description: OK } }
                    trace:
                      responses: { "200": { description: OK } }
                """);

        OpenApiDocument document = successValue(
                decoder.decode(source, OpenApiVersion.V3_1_2));

        assertEquals(
                Set.of(
                        HttpMethod.GET,
                        HttpMethod.PUT,
                        HttpMethod.POST,
                        HttpMethod.DELETE,
                        HttpMethod.OPTIONS,
                        HttpMethod.HEAD,
                        HttpMethod.PATCH,
                        HttpMethod.TRACE),
                document.getPaths()
                        .getItems()
                        .get("/methods")
                        .getOperations()
                .keySet());
    }

    @Test
    void distinguishesInheritedSecurityFromExplicitEmptyOverride() {
        ObjectValue source = parse("""
                openapi: 3.1.2
                info:
                  title: Security API
                  version: 1.0.0
                paths:
                  /inherits:
                    get:
                      responses: { "200": { description: OK } }
                  /disabled:
                    get:
                      security: []
                      responses: { "200": { description: OK } }
                """);

        OpenApiDocument document = successValue(
                decoder.decode(source, OpenApiVersion.V3_1_2));
        Operation inherited = document.getPaths()
                .getItems()
                .get("/inherits")
                .getOperations()
                .get(HttpMethod.GET);
        Operation disabled = document.getPaths()
                .getItems()
                .get("/disabled")
                .getOperations()
                .get(HttpMethod.GET);

        assertFalse(inherited.hasSecurityOverride());
        assertTrue(disabled.hasSecurityOverride());
        assertTrue(disabled.getSecurity().isEmpty());
    }

    @Test
    void reportsPathAndOperationMappingErrorsAtExactPaths() {
        ObjectValue source = parse("""
                openapi: 3.1.2
                info:
                  title: Broken API
                  version: 1.0.0
                paths:
                  /broken: invalid
                  /users:
                    get:
                      tags: [users, 42]
                      parameters: [invalid]
                      requestBody: 42
                      responses:
                        "200": invalid
                      security:
                        - OAuth2: invalid
                      servers: [invalid]
                  /missing-responses:
                    post:
                      summary: Missing responses
                """);

        AdapterFailure<?> failure = assertInstanceOf(
                AdapterFailure.class,
                decoder.decode(source, OpenApiVersion.V3_1_2));
        Set<String> diagnosticPaths = failure.diagnostics().stream()
                .map(OpenApiDiagnostic::path)
                .map(path -> path.toPointer())
                .collect(Collectors.toSet());

        assertEquals(Set.of(
                "/paths/~1broken",
                "/paths/~1users/get/tags/1",
                "/paths/~1users/get/parameters/0",
                "/paths/~1users/get/requestBody",
                "/paths/~1users/get/responses/200",
                "/paths/~1users/get/security/0/OAuth2",
                "/paths/~1users/get/servers/0",
                "/paths/~1missing-responses/post/responses"), diagnosticPaths);
        assertTrue(failure.diagnostics().stream()
                .anyMatch(diagnostic -> diagnostic.code().equals(
                        OpenApiDiagnosticCodes.MAPPING_MISSING_REQUIRED_FIELD)));
        assertTrue(failure.diagnostics().stream()
                .filter(diagnostic -> diagnostic.code().equals(
                        OpenApiDiagnosticCodes.MAPPING_TYPE_MISMATCH))
                .count() >= 7);
    }

    private static <T> T inlineValue(Object source, Class<T> valueType) {
        InlineObject<?> inline = assertInstanceOf(InlineObject.class, source);
        return assertInstanceOf(valueType, inline.value());
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
