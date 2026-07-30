package ru.luttsev.studio.openapi.version.v31.decode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import ru.luttsev.studio.core.model.Components;
import ru.luttsev.studio.core.model.OpenApiDocument;
import ru.luttsev.studio.core.model.OpenApiVersion;
import ru.luttsev.studio.core.model.media.Encoding;
import ru.luttsev.studio.core.model.media.Example;
import ru.luttsev.studio.core.model.media.MediaType;
import ru.luttsev.studio.core.model.media.MediaTypeName;
import ru.luttsev.studio.core.model.media.RequestBody;
import ru.luttsev.studio.core.model.parameter.Header;
import ru.luttsev.studio.core.model.parameter.Parameter;
import ru.luttsev.studio.core.model.parameter.ParameterLocation;
import ru.luttsev.studio.core.model.parameter.ParameterStyle;
import ru.luttsev.studio.core.model.reference.InlineObject;
import ru.luttsev.studio.core.model.reference.ReferenceObject;
import ru.luttsev.studio.core.model.response.ApiResponse;
import ru.luttsev.studio.core.model.schema.JsonType;
import ru.luttsev.studio.core.model.schema.SchemaDefinition;
import ru.luttsev.studio.core.model.value.NumberValue;
import ru.luttsev.studio.core.model.value.ObjectValue;
import ru.luttsev.studio.core.model.value.StringValue;
import ru.luttsev.studio.openapi.diagnostic.DiagnosticCode;
import ru.luttsev.studio.openapi.diagnostic.OpenApiDiagnostic;
import ru.luttsev.studio.openapi.diagnostic.OpenApiDiagnosticCodes;
import ru.luttsev.studio.openapi.importing.ImportOptions;
import ru.luttsev.studio.openapi.result.AdapterFailure;
import ru.luttsev.studio.openapi.result.AdapterResult;
import ru.luttsev.studio.openapi.result.AdapterSuccess;
import ru.luttsev.studio.openapi.result.SyntaxSuccess;
import ru.luttsev.studio.openapi.syntax.JacksonOpenApiSyntaxCodec;
import ru.luttsev.studio.openapi.syntax.ParsedDocument;

class ComponentsPayloadDecoderTest {

    private final OpenApi31Decoder decoder = new OpenApi31Decoder();

    @Test
    void decodesInlineAndReferencedPayloadComponents() {
        ObjectValue source = parse("""
                openapi: 3.1.2
                info:
                  title: Users API
                  version: 1.0.0
                paths: {}
                components:
                  schemas:
                    User:
                      type: object
                      properties:
                        id:
                          type: string
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
                            $ref: "#/components/schemas/User"
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
                            $ref: "#/components/schemas/User"
                          example:
                            id: user-1
                      links:
                        self:
                          operationId: getUser
                      x-response-id: user
                    SharedResponse:
                      $ref: "#/components/responses/UserResponse"
                  securitySchemes:
                    ApiKey:
                      type: apiKey
                      in: header
                      name: X-API-Key
                """);

        OpenApiDocument document = successValue(
                decoder.decode(source, OpenApiVersion.V3_1_2));
        Components components = document.getComponents();

        Example example = inlineValue(
                components.getExamples().get("UserExample"),
                Example.class);
        assertEquals("User", example.getSummary());
        assertEquals("Example user", example.getDescription());
        assertInstanceOf(ObjectValue.class, example.getValue());
        assertEquals(
                new StringValue("primary"),
                example.getAdditionalFields().get("x-example-id"));

        ReferenceObject<?> exampleReference = assertInstanceOf(
                ReferenceObject.class,
                components.getExamples().get("SharedExample"));
        assertEquals(
                "#/components/examples/UserExample",
                exampleReference.getRef().value());
        assertEquals("Shared user", exampleReference.getSummary());
        assertEquals(
                new StringValue("shared"),
                exampleReference.getAdditionalFields().get("x-reference-id"));

        Parameter parameter = inlineValue(
                components.getParameters().get("Limit"),
                Parameter.class);
        assertEquals("limit", parameter.getName());
        assertEquals(ParameterLocation.QUERY, parameter.getLocation());
        assertEquals(ParameterStyle.SPACE_DELIMITED, parameter.getStyle());
        assertEquals(false, parameter.getRequired());
        assertEquals(true, parameter.getDeprecated());
        assertEquals(false, parameter.getAllowEmptyValue());
        assertEquals(true, parameter.getExplode());
        assertEquals(true, parameter.getAllowReserved());
        assertEquals(new BigDecimal("1"), assertInstanceOf(
                SchemaDefinition.class,
                parameter.getSchema()).getMinimum());
        assertEquals(
                10,
                assertInstanceOf(
                        NumberValue.class,
                        parameter.getExample()).value().intValueExact());
        assertInstanceOf(
                ReferenceObject.class,
                parameter.getExamples().get("Alternative"));
        assertEquals(
                new StringValue("limit"),
                parameter.getAdditionalFields().get("x-parameter-id"));

        ReferenceObject<?> parameterReference = assertInstanceOf(
                ReferenceObject.class,
                components.getParameters().get("SharedLimit"));
        assertEquals(
                "#/components/parameters/Limit",
                parameterReference.getRef().value());

        Header header = inlineValue(
                components.getHeaders().get("RequestId"),
                Header.class);
        assertEquals("Request identifier", header.getDescription());
        assertEquals(true, header.getRequired());
        assertEquals(false, header.getDeprecated());
        assertEquals(ParameterStyle.SIMPLE, header.getStyle());
        assertEquals(false, header.getExplode());
        assertEquals(
                Set.of(JsonType.STRING),
                assertInstanceOf(
                        SchemaDefinition.class,
                        header.getSchema()).getTypes());
        assertEquals(new StringValue("trace-1"), header.getExample());

        RequestBody requestBody = inlineValue(
                components.getRequestBodies().get("CreateUser"),
                RequestBody.class);
        assertEquals("User payload", requestBody.getDescription());
        assertEquals(true, requestBody.getRequired());
        MediaType mediaType = inlineValue(
                requestBody.getContent().get(MediaTypeName.APPLICATION_JSON),
                MediaType.class);
        assertEquals(
                "#/components/schemas/User",
                assertInstanceOf(
                        SchemaDefinition.class,
                        mediaType.getSchema()).getRef().value());
        assertInstanceOf(ObjectValue.class, mediaType.getExample());
        assertInstanceOf(
                ReferenceObject.class,
                mediaType.getExamples().get("Created"));
        assertEquals(
                new StringValue("json"),
                mediaType.getAdditionalFields().get("x-media-id"));

        Encoding encoding = mediaType.getEncoding().get("profile");
        assertEquals("application/json", encoding.getContentType());
        assertEquals(ParameterStyle.FORM, encoding.getStyle());
        assertEquals(true, encoding.getExplode());
        assertEquals(false, encoding.getAllowReserved());
        assertInstanceOf(
                ReferenceObject.class,
                encoding.getHeaders().get("X-Trace"));
        assertEquals(
                new StringValue("profile"),
                encoding.getAdditionalFields().get("x-encoding-id"));

        assertInstanceOf(
                ReferenceObject.class,
                components.getRequestBodies().get("SharedBody"));

        ApiResponse response = inlineValue(
                components.getResponses().get("UserResponse"),
                ApiResponse.class);
        assertEquals("User response", response.getDescription());
        assertInstanceOf(
                ReferenceObject.class,
                response.getHeaders().get("X-Request-Id"));
        assertTrue(response.getContent().containsKey(
                MediaTypeName.APPLICATION_JSON));
        assertTrue(response.getLinks().isEmpty());

        ObjectValue componentsSource = assertInstanceOf(
                ObjectValue.class,
                source.values().get("components"));
        ObjectValue responsesSource = assertInstanceOf(
                ObjectValue.class,
                componentsSource.values().get("responses"));
        ObjectValue responseSource = assertInstanceOf(
                ObjectValue.class,
                responsesSource.values().get("UserResponse"));
        assertSame(
                responseSource.values().get("links"),
                response.getAdditionalFields().get("links"));
        assertEquals(
                new StringValue("user"),
                response.getAdditionalFields().get("x-response-id"));
        assertInstanceOf(
                ReferenceObject.class,
                components.getResponses().get("SharedResponse"));

        assertSame(
                componentsSource.values().get("securitySchemes"),
                components.getAdditionalFields().get("securitySchemes"));
        assertNull(components.getAdditionalFields().get("examples"));
        assertNull(components.getAdditionalFields().get("parameters"));
        assertNull(components.getAdditionalFields().get("headers"));
        assertNull(components.getAdditionalFields().get("requestBodies"));
        assertNull(components.getAdditionalFields().get("responses"));
    }

    @Test
    void reportsPayloadMappingErrorsAtExactPaths() {
        ObjectValue source = parse("""
                openapi: 3.1.2
                info:
                  title: Broken API
                  version: 1.0.0
                paths: {}
                components:
                  parameters:
                    InvalidShape: string
                  examples:
                    InvalidReference:
                      $ref: 42
                  headers:
                    InvalidStyle:
                      style: unsupported
                      schema: true
                  requestBodies:
                    MissingContent:
                      description: Missing content
                  responses:
                    MissingDescription: {}
                """);

        AdapterFailure<?> failure = assertInstanceOf(
                AdapterFailure.class,
                decoder.decode(source, OpenApiVersion.V3_1_2));
        List<OpenApiDiagnostic> diagnostics = failure.diagnostics();

        assertEquals(5, diagnostics.size());
        assertDiagnostic(
                diagnostics.get(0),
                OpenApiDiagnosticCodes.MAPPING_MISSING_REQUIRED_FIELD,
                "/components/responses/MissingDescription/description");
        assertDiagnostic(
                diagnostics.get(1),
                OpenApiDiagnosticCodes.MAPPING_TYPE_MISMATCH,
                "/components/parameters/InvalidShape");
        assertDiagnostic(
                diagnostics.get(2),
                OpenApiDiagnosticCodes.MAPPING_TYPE_MISMATCH,
                "/components/examples/InvalidReference/$ref");
        assertDiagnostic(
                diagnostics.get(3),
                OpenApiDiagnosticCodes.MAPPING_MISSING_REQUIRED_FIELD,
                "/components/requestBodies/MissingContent/content");
        assertDiagnostic(
                diagnostics.get(4),
                OpenApiDiagnosticCodes.MAPPING_INVALID_VALUE,
                "/components/headers/InvalidStyle/style");
    }

    private static void assertDiagnostic(
            OpenApiDiagnostic diagnostic,
            DiagnosticCode code,
            String path) {
        assertEquals(code, diagnostic.code());
        assertEquals(path, diagnostic.path().toPointer());
    }

    private static <T> T inlineValue(
            Object source,
            Class<T> valueType) {
        InlineObject<?> inline = assertInstanceOf(InlineObject.class, source);
        return assertInstanceOf(valueType, inline.value());
    }

    private static OpenApiDocument successValue(
            AdapterResult<OpenApiDocument> result) {
        AdapterSuccess<?> success = assertInstanceOf(
                AdapterSuccess.class,
                result);
        assertTrue(success.diagnostics().isEmpty());
        return assertInstanceOf(
                OpenApiDocument.class,
                success.value());
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
