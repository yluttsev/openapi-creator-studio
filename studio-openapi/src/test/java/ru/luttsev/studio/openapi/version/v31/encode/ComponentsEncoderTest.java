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
import ru.luttsev.studio.core.model.media.MediaType;
import ru.luttsev.studio.core.model.reference.InlineObject;
import ru.luttsev.studio.core.model.schema.UriReference;
import ru.luttsev.studio.core.model.security.OAuthFlow;
import ru.luttsev.studio.core.model.security.OAuthFlows;
import ru.luttsev.studio.core.model.security.SecurityScheme;
import ru.luttsev.studio.core.model.security.SecuritySchemeType;
import ru.luttsev.studio.core.model.value.ObjectValue;
import ru.luttsev.studio.openapi.diagnostic.DiagnosticCode;
import ru.luttsev.studio.openapi.diagnostic.OpenApiDiagnostic;
import ru.luttsev.studio.openapi.diagnostic.OpenApiDiagnosticCodes;
import ru.luttsev.studio.openapi.importing.ImportOptions;
import ru.luttsev.studio.openapi.result.AdapterSuccess;
import ru.luttsev.studio.openapi.result.SyntaxSuccess;
import ru.luttsev.studio.openapi.syntax.JacksonOpenApiSyntaxCodec;
import ru.luttsev.studio.openapi.syntax.ParsedDocument;
import ru.luttsev.studio.openapi.version.v31.decode.OpenApi31Decoder;

class ComponentsEncoderTest {

    private final ComponentsEncoder encoder = new ComponentsEncoder();

    @Test
    void roundTripsEveryOpenApi31ComponentSection() {
        ObjectValue source = parse("""
                openapi: 3.1.2
                info:
                  title: Components API
                  version: 1.0.0
                paths: {}
                components:
                  schemas:
                    User:
                      type: object
                      properties:
                        id: { type: string }
                  responses:
                    UserResponse:
                      description: User response
                      content:
                        application/json:
                          schema:
                            $ref: "#/components/schemas/User"
                  parameters:
                    Limit:
                      name: limit
                      in: query
                      schema: { type: integer }
                  examples:
                    UserExample:
                      summary: User
                      value: { id: user-1 }
                  requestBodies:
                    CreateUser:
                      required: true
                      content:
                        application/json:
                          schema:
                            $ref: "#/components/schemas/User"
                  headers:
                    RequestId:
                      description: Request identifier
                      schema: { type: string }
                  securitySchemes:
                    ApiKey:
                      type: apiKey
                      description: API key
                      name: X-API-Key
                      in: header
                    Bearer:
                      type: http
                      scheme: bearer
                      bearerFormat: JWT
                    MutualTls:
                      type: mutualTLS
                    OAuth:
                      type: oauth2
                      flows:
                        implicit:
                          authorizationUrl: https://example.com/authorize
                          refreshUrl: https://example.com/refresh
                          scopes:
                            users:read: Read users
                          x-flow-id: implicit
                        password:
                          tokenUrl: https://example.com/token
                          scopes: {}
                        clientCredentials:
                          tokenUrl: https://example.com/token
                          scopes:
                            users:write: Write users
                        authorizationCode:
                          authorizationUrl: https://example.com/authorize
                          tokenUrl: https://example.com/token
                          scopes: {}
                    OpenId:
                      type: openIdConnect
                      openIdConnectUrl: https://example.com/.well-known/openid-configuration
                    SharedOAuth:
                      $ref: "#/components/securitySchemes/OAuth"
                  links:
                    UserById:
                      operationId: getUser
                      parameters:
                        userId: "$response.body#/id"
                  callbacks:
                    UserCallback:
                      '{$request.body#/callbackUrl}':
                        post:
                          responses:
                            "200": { description: OK }
                    SharedCallback:
                      $ref: "#/components/callbacks/UserCallback"
                  pathItems:
                    UserPath:
                      summary: User operations
                      get:
                        responses:
                          "200":
                            $ref: "#/components/responses/UserResponse"
                  x-components-id: shared
                """);
        OpenApiDocument document = decode(source);
        EncodeContext context = EncodeContext.root(OpenApiVersion.V3_1_2)
                .child("components");

        ObjectValue encoded = encoder.encode(
                document.getComponents(),
                context);

        assertEquals(object(source.values().get("components")), encoded);
        assertTrue(context.diagnostics().isEmpty());
    }

    @Test
    void reportsOpenApi32ComponentAndSecurityFields() {
        Components components = new Components();
        components.setMediaTypes(Map.of(
                "Shared",
                new InlineObject<>(new MediaType())));

        OAuthFlow implicit = new OAuthFlow();
        implicit.setAuthorizationUrl(
                new UriReference("https://example.com/authorize"));
        implicit.setDeviceAuthorizationUrl(
                new UriReference("https://example.com/device"));
        OAuthFlows flows = new OAuthFlows();
        flows.setImplicit(implicit);
        flows.setDeviceAuthorization(new OAuthFlow());

        SecurityScheme securityScheme = new SecurityScheme();
        securityScheme.setType(SecuritySchemeType.OAUTH2);
        securityScheme.setFlows(flows);
        securityScheme.setOauth2MetadataUrl(
                new UriReference("https://example.com/oauth-metadata"));
        securityScheme.setDeprecated(true);
        components.setSecuritySchemes(Map.of(
                "Future",
                new InlineObject<>(securityScheme)));
        EncodeContext context = EncodeContext.root(OpenApiVersion.V3_1_2)
                .child("components");

        ObjectValue encoded = encoder.encode(components, context);

        assertFalse(encoded.values().containsKey("mediaTypes"));
        ObjectValue schemes = object(encoded.values().get("securitySchemes"));
        ObjectValue future = object(schemes.values().get("Future"));
        assertFalse(future.values().containsKey("oauth2MetadataUrl"));
        assertFalse(future.values().containsKey("deprecated"));
        ObjectValue encodedFlows = object(future.values().get("flows"));
        assertFalse(encodedFlows.values().containsKey("deviceAuthorization"));
        ObjectValue encodedImplicit = object(
                encodedFlows.values().get("implicit"));
        assertFalse(encodedImplicit.values().containsKey(
                "deviceAuthorizationUrl"));

        assertEquals(5, context.diagnostics().size());
        assertDiagnostic(
                context.diagnostics(),
                OpenApiDiagnosticCodes.VERSION_UNSUPPORTED_FIELD,
                "/components/mediaTypes");
        assertDiagnostic(
                context.diagnostics(),
                OpenApiDiagnosticCodes.VERSION_UNSUPPORTED_FIELD,
                "/components/securitySchemes/Future/oauth2MetadataUrl");
        assertDiagnostic(
                context.diagnostics(),
                OpenApiDiagnosticCodes.VERSION_UNSUPPORTED_FIELD,
                "/components/securitySchemes/Future/deprecated");
        assertDiagnostic(
                context.diagnostics(),
                OpenApiDiagnosticCodes.VERSION_UNSUPPORTED_FIELD,
                "/components/securitySchemes/Future/flows/deviceAuthorization");
        assertDiagnostic(
                context.diagnostics(),
                OpenApiDiagnosticCodes.VERSION_UNSUPPORTED_FIELD,
                "/components/securitySchemes/Future/flows/implicit/deviceAuthorizationUrl");
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
