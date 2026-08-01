package ru.luttsev.studio.openapi.version.v31.encode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import ru.luttsev.studio.core.model.Components;
import ru.luttsev.studio.core.model.OpenApiDocument;
import ru.luttsev.studio.core.model.OpenApiVersion;
import ru.luttsev.studio.core.model.link.Link;
import ru.luttsev.studio.core.model.media.Encoding;
import ru.luttsev.studio.core.model.media.Example;
import ru.luttsev.studio.core.model.media.MediaType;
import ru.luttsev.studio.core.model.media.MediaTypeName;
import ru.luttsev.studio.core.model.media.RequestBody;
import ru.luttsev.studio.core.model.parameter.Parameter;
import ru.luttsev.studio.core.model.parameter.ParameterLocation;
import ru.luttsev.studio.core.model.path.HttpMethod;
import ru.luttsev.studio.core.model.reference.InlineObject;
import ru.luttsev.studio.core.model.reference.ReferenceObject;
import ru.luttsev.studio.core.model.response.Responses;
import ru.luttsev.studio.core.model.schema.LogicalSchema;
import ru.luttsev.studio.core.model.schema.UriReference;
import ru.luttsev.studio.core.model.server.Server;
import ru.luttsev.studio.core.model.value.BooleanValue;
import ru.luttsev.studio.core.model.value.ObjectValue;
import ru.luttsev.studio.core.model.value.StringValue;
import ru.luttsev.studio.openapi.diagnostic.DiagnosticCode;
import ru.luttsev.studio.openapi.diagnostic.OpenApiDiagnostic;
import ru.luttsev.studio.openapi.diagnostic.OpenApiDiagnosticCodes;
import ru.luttsev.studio.openapi.importing.ImportOptions;
import ru.luttsev.studio.openapi.result.AdapterSuccess;
import ru.luttsev.studio.openapi.result.SyntaxSuccess;
import ru.luttsev.studio.openapi.syntax.JacksonOpenApiSyntaxCodec;
import ru.luttsev.studio.openapi.syntax.ParsedDocument;
import ru.luttsev.studio.openapi.version.v31.decode.OpenApi31Decoder;

class PayloadEncoderTest {

    private final PayloadEncoder encoder =
            new PayloadEncoder(new SchemaEncoder());

    @Test
    void roundTripsInlineAndReferencedPayloadGraph() {
        ObjectValue source = parse("""
                openapi: 3.1.2
                info:
                  title: Users API
                  version: 1.0.0
                paths:
                  /users:
                    get:
                      responses:
                        "200":
                          description: Users
                        x-responses-id: list-users
                components:
                  examples:
                    UserExample:
                      summary: User
                      description: Example user
                      value:
                        id: user-1
                      x-example-id: primary
                    SharedExample:
                      $ref: "#/components/examples/UserExample"
                      summary: Shared user
                      x-reference-id: shared
                  parameters:
                    Limit:
                      name: limit
                      in: query
                      description: Result limit
                      required: false
                      deprecated: true
                      allowEmptyValue: false
                      style: spaceDelimited
                      explode: true
                      allowReserved: true
                      schema:
                        type: integer
                        minimum: 1
                      example: 10
                      examples:
                        Alternative:
                          $ref: "#/components/examples/UserExample"
                      x-parameter-id: limit
                    SharedLimit:
                      $ref: "#/components/parameters/Limit"
                  headers:
                    RequestId:
                      description: Request identifier
                      required: true
                      deprecated: false
                      style: simple
                      explode: false
                      schema:
                        type: string
                      example: trace-1
                  requestBodies:
                    CreateUser:
                      description: User payload
                      required: true
                      content:
                        application/json:
                          schema:
                            type: object
                          example:
                            id: user-1
                          examples:
                            Created:
                              $ref: "#/components/examples/UserExample"
                          encoding:
                            profile:
                              contentType: application/json
                              style: form
                              explode: true
                              allowReserved: false
                              headers:
                                X-Trace:
                                  $ref: "#/components/headers/RequestId"
                              x-encoding-id: profile
                          x-media-id: json
                    SharedBody:
                      $ref: "#/components/requestBodies/CreateUser"
                  responses:
                    UserResponse:
                      description: User response
                      headers:
                        X-Request-Id:
                          $ref: "#/components/headers/RequestId"
                      content:
                        application/json:
                          schema:
                            type: object
                      links:
                        self:
                          $ref: "#/components/links/UserById"
                      x-response-id: user
                    SharedResponse:
                      $ref: "#/components/responses/UserResponse"
                  links:
                    UserById:
                      operationId: getUser
                      parameters:
                        userId: "$response.body#/id"
                      requestBody:
                        id: "$response.body#/id"
                      description: Follow the returned user
                      server:
                        url: "https://{region}.example.com"
                        description: Regional API
                        variables:
                          region:
                            enum: [eu, us]
                            default: eu
                            description: API region
                            x-variable-id: region
                        x-server-id: regional
                      x-link-id: user
                    SharedUserLink:
                      $ref: "#/components/links/UserById"
                """);
        OpenApiDocument document = decode(source);
        Components components = document.getComponents();
        ObjectValue sourceComponents = object(source.values().get("components"));
        EncodeContext context = EncodeContext.root(OpenApiVersion.V3_1_2)
                .child("components");

        assertEquals(
                sourceComponents.values().get("examples"),
                encoder.encodeExamples(
                        components.getExamples(),
                        context.child("examples")));
        assertEquals(
                sourceComponents.values().get("parameters"),
                encoder.encodeParameters(
                        components.getParameters(),
                        context.child("parameters")));
        assertEquals(
                sourceComponents.values().get("headers"),
                encoder.encodeHeaders(
                        components.getHeaders(),
                        context.child("headers")));
        assertEquals(
                sourceComponents.values().get("requestBodies"),
                encoder.encodeRequestBodies(
                        components.getRequestBodies(),
                        context.child("requestBodies")));
        assertEquals(
                sourceComponents.values().get("responses"),
                encoder.encodeResponses(
                        components.getResponses(),
                        context.child("responses")));
        assertEquals(
                sourceComponents.values().get("links"),
                encoder.encodeLinks(
                        components.getLinks(),
                        context.child("links")));

        Responses responses = document.getPaths()
                .getItems()
                .get("/users")
                .getOperations()
                .get(HttpMethod.GET)
                .getResponses();
        ObjectValue sourceResponses = object(object(object(object(
                source.values().get("paths"))
                .values().get("/users"))
                .values().get("get"))
                .values().get("responses"));
        assertEquals(
                sourceResponses,
                new ResponsesEncoder(encoder).encode(
                        responses,
                        EncodeContext.root(OpenApiVersion.V3_1_2)
                                .child("paths")
                                .child("/users")
                                .child("get")
                                .child("responses")));
        assertTrue(context.diagnostics().isEmpty());
    }

    @Test
    void reportsOpenApi32OnlyPayloadFeaturesAtExactPaths() {
        EncodeContext context = EncodeContext.root(OpenApiVersion.V3_1_2);

        Example example = new Example();
        example.setDataValue(new BooleanValue(true));
        example.setSerializedValue("flag=true");
        encoder.encodeExamples(
                Map.of("Future", new InlineObject<>(example)),
                context.child("examples"));

        Parameter parameter = new Parameter();
        parameter.setName("query");
        parameter.setLocation(ParameterLocation.QUERYSTRING);
        encoder.encodeParameters(
                Map.of("Query", new InlineObject<>(parameter)),
                context.child("parameters"));

        Encoding nestedEncoding = new Encoding();
        nestedEncoding.setEncoding(Map.of("nested", new Encoding()));
        nestedEncoding.setPrefixEncoding(List.of(new Encoding()));
        nestedEncoding.setItemEncoding(new Encoding());
        MediaType mediaType = new MediaType();
        mediaType.setItemSchema(new LogicalSchema(true));
        mediaType.setPrefixEncoding(List.of(new Encoding()));
        mediaType.setItemEncoding(new Encoding());
        mediaType.setEncoding(Map.of("property", nestedEncoding));
        ReferenceObject<MediaType> mediaTypeReference = new ReferenceObject<>();
        mediaTypeReference.setRef(new UriReference(
                "#/components/mediaTypes/Shared"));
        RequestBody requestBody = new RequestBody();
        requestBody.setContent(Map.of(
                MediaTypeName.APPLICATION_JSON,
                new InlineObject<>(mediaType),
                MediaTypeName.APPLICATION_XML,
                mediaTypeReference));
        ObjectValue encodedRequestBody = encoder.encodeRequestBody(
                new InlineObject<>(requestBody),
                context.child("requestBody"));

        Server server = new Server();
        server.setUrl("https://example.com");
        server.setName("production");
        Link link = new Link();
        link.setOperationId("getUser");
        link.setServer(server);
        encoder.encodeLinks(
                Map.of("Future", new InlineObject<>(link)),
                context.child("links"));

        ObjectValue content = object(encodedRequestBody.values().get("content"));
        assertFalse(content.values().containsKey("application/xml"));
        assertDiagnostic(
                context.diagnostics(),
                OpenApiDiagnosticCodes.VERSION_UNSUPPORTED_FIELD,
                "/examples/Future/dataValue");
        assertDiagnostic(
                context.diagnostics(),
                OpenApiDiagnosticCodes.VERSION_UNSUPPORTED_FIELD,
                "/examples/Future/serializedValue");
        assertDiagnostic(
                context.diagnostics(),
                OpenApiDiagnosticCodes.VERSION_UNSUPPORTED_FIELD,
                "/parameters/Query/in");
        assertDiagnostic(
                context.diagnostics(),
                OpenApiDiagnosticCodes.VERSION_UNSUPPORTED_FIELD,
                "/requestBody/content/application~1json/itemSchema");
        assertDiagnostic(
                context.diagnostics(),
                OpenApiDiagnosticCodes.VERSION_UNSUPPORTED_FIELD,
                "/requestBody/content/application~1json/prefixEncoding");
        assertDiagnostic(
                context.diagnostics(),
                OpenApiDiagnosticCodes.VERSION_UNSUPPORTED_FIELD,
                "/requestBody/content/application~1json/itemEncoding");
        assertDiagnostic(
                context.diagnostics(),
                OpenApiDiagnosticCodes.VERSION_UNSUPPORTED_FIELD,
                "/requestBody/content/application~1json/encoding/property/encoding");
        assertDiagnostic(
                context.diagnostics(),
                OpenApiDiagnosticCodes.VERSION_UNSUPPORTED_FIELD,
                "/requestBody/content/application~1json/encoding/property/prefixEncoding");
        assertDiagnostic(
                context.diagnostics(),
                OpenApiDiagnosticCodes.VERSION_UNSUPPORTED_FIELD,
                "/requestBody/content/application~1json/encoding/property/itemEncoding");
        assertDiagnostic(
                context.diagnostics(),
                OpenApiDiagnosticCodes.VERSION_UNSUPPORTED_REFERENCE,
                "/requestBody/content/application~1xml");
        assertDiagnostic(
                context.diagnostics(),
                OpenApiDiagnosticCodes.VERSION_UNSUPPORTED_FIELD,
                "/links/Future/server/name");
        assertEquals(11, context.diagnostics().size());
        assertTrue(context.hasErrors());
    }

    @Test
    void keepsTypedResponseAuthoritativeOnAdditionalFieldConflict() {
        ObjectValue source = parse("""
                openapi: 3.1.2
                info:
                  title: API
                  version: 1.0.0
                paths:
                  /users:
                    get:
                      responses:
                        "200":
                          description: Users
                """);
        Responses responses = decode(source)
                .getPaths()
                .getItems()
                .get("/users")
                .getOperations()
                .get(HttpMethod.GET)
                .getResponses();
        responses.getAdditionalFields().put(
                "200",
                new StringValue("stale"));
        EncodeContext context = EncodeContext.root(OpenApiVersion.V3_1_2)
                .child("paths")
                .child("/users")
                .child("get")
                .child("responses");

        ObjectValue encoded = new ResponsesEncoder(encoder).encode(
                responses,
                context);

        assertInstanceOf(ObjectValue.class, encoded.values().get("200"));
        assertEquals(1, context.diagnostics().size());
        assertEquals(
                OpenApiDiagnosticCodes.VERSION_FIELD_CONFLICT,
                context.diagnostics().getFirst().code());
        assertEquals(
                "/paths/~1users/get/responses/200",
                context.diagnostics().getFirst().path().toPointer());
    }

    private static void assertDiagnostic(
            List<OpenApiDiagnostic> diagnostics,
            DiagnosticCode code,
            String path) {
        assertTrue(diagnostics.stream().anyMatch(diagnostic ->
                diagnostic.code().equals(code)
                        && diagnostic.path().toPointer().equals(path)));
    }

    private static ObjectValue object(Object value) {
        return assertInstanceOf(ObjectValue.class, value);
    }

    private static OpenApiDocument decode(ObjectValue source) {
        AdapterSuccess<?> success = assertInstanceOf(
                AdapterSuccess.class,
                new OpenApi31Decoder().decode(
                        source,
                        OpenApiVersion.V3_1_2));
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
