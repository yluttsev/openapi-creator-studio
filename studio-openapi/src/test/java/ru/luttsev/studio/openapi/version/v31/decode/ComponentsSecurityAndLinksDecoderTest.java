package ru.luttsev.studio.openapi.version.v31.decode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;
import ru.luttsev.studio.core.model.Components;
import ru.luttsev.studio.core.model.link.Link;
import ru.luttsev.studio.core.model.OpenApiDocument;
import ru.luttsev.studio.core.model.OpenApiVersion;
import ru.luttsev.studio.core.model.reference.InlineObject;
import ru.luttsev.studio.core.model.reference.ReferenceObject;
import ru.luttsev.studio.core.model.response.ApiResponse;
import ru.luttsev.studio.core.model.security.ApiKeyLocation;
import ru.luttsev.studio.core.model.security.OAuthFlow;
import ru.luttsev.studio.core.model.security.OAuthFlows;
import ru.luttsev.studio.core.model.security.SecurityScheme;
import ru.luttsev.studio.core.model.security.SecuritySchemeType;
import ru.luttsev.studio.core.model.server.Server;
import ru.luttsev.studio.core.model.server.ServerVariable;
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
import ru.luttsev.studio.openapi.testing.TestResources;

class ComponentsSecurityAndLinksDecoderTest {

    private final OpenApi31Decoder decoder = new OpenApi31Decoder();

    @Test
    void decodesSecuritySchemesAndLinks() {
        ObjectValue source = parse(TestResources.readFixture(
                "v31/decode/components-security-links.yaml"));

        OpenApiDocument document = successValue(
                decoder.decode(source, OpenApiVersion.V3_1_2));
        Components components = document.getComponents();

        SecurityScheme apiKey = inlineValue(
                components.getSecuritySchemes().get("ApiKey"),
                SecurityScheme.class);
        assertEquals(SecuritySchemeType.API_KEY, apiKey.getType());
        assertEquals(ApiKeyLocation.HEADER, apiKey.getLocation());
        assertEquals("X-API-Key", apiKey.getName());
        assertEquals(
                new StringValue("api-key"),
                apiKey.getAdditionalFields().get("x-security-id"));

        SecurityScheme bearer = inlineValue(
                components.getSecuritySchemes().get("Bearer"),
                SecurityScheme.class);
        assertEquals(SecuritySchemeType.HTTP, bearer.getType());
        assertEquals("bearer", bearer.getScheme());
        assertEquals("JWT", bearer.getBearerFormat());

        SecurityScheme oauth = inlineValue(
                components.getSecuritySchemes().get("OAuth"),
                SecurityScheme.class);
        assertEquals(SecuritySchemeType.OAUTH2, oauth.getType());
        OAuthFlows flows = oauth.getFlows();
        OAuthFlow implicit = flows.getImplicit();
        assertEquals(
                "https://example.com/authorize",
                implicit.getAuthorizationUrl().value());
        assertEquals(
                "https://example.com/refresh",
                implicit.getRefreshUrl().value());
        assertEquals("Read users", implicit.getScopes().get("users:read"));
        assertEquals(
                new StringValue("implicit"),
                implicit.getAdditionalFields().get("x-flow-id"));
        assertEquals(
                "https://example.com/token",
                flows.getPassword().getTokenUrl().value());
        assertEquals(
                "Write users",
                flows.getClientCredentials().getScopes().get("users:write"));
        assertEquals(
                "https://example.com/authorize",
                flows.getAuthorizationCode().getAuthorizationUrl().value());
        assertNull(flows.getDeviceAuthorization());
        assertNull(oauth.getOauth2MetadataUrl());
        assertNull(oauth.getDeprecated());

        SecurityScheme openId = inlineValue(
                components.getSecuritySchemes().get("OpenId"),
                SecurityScheme.class);
        assertEquals(SecuritySchemeType.OPEN_ID_CONNECT, openId.getType());
        assertEquals(
                "https://example.com/.well-known/openid-configuration",
                openId.getOpenIdConnectUrl().value());

        SecurityScheme mutualTls = inlineValue(
                components.getSecuritySchemes().get("MutualTls"),
                SecurityScheme.class);
        assertEquals(SecuritySchemeType.MUTUAL_TLS, mutualTls.getType());
        assertInstanceOf(
                ReferenceObject.class,
                components.getSecuritySchemes().get("SharedOAuth"));

        Link link = inlineValue(
                components.getLinks().get("UserById"),
                Link.class);
        assertEquals("getUser", link.getOperationId());
        assertEquals(
                new StringValue("$response.body#/id"),
                link.getParameters().get("userId"));
        assertInstanceOf(ObjectValue.class, link.getRequestBody());
        assertEquals("Follow the returned user", link.getDescription());
        assertEquals(
                new StringValue("user"),
                link.getAdditionalFields().get("x-link-id"));

        Server server = link.getServer();
        assertEquals("https://{region}.example.com", server.getUrl());
        assertEquals("Regional API", server.getDescription());
        assertEquals(
                new StringValue("regional"),
                server.getAdditionalFields().get("x-server-id"));
        ServerVariable region = server.getVariables().get("region");
        assertEquals(List.of("eu", "us"), region.getEnumValues());
        assertEquals("eu", region.getDefaultValue());
        assertEquals(
                new StringValue("region"),
                region.getAdditionalFields().get("x-variable-id"));
        assertNull(server.getName());

        Link operationReference = inlineValue(
                components.getLinks().get("UserByOperationRef"),
                Link.class);
        assertEquals(
                "#/paths/~1users~1{id}/get",
                operationReference.getOperationRef().value());
        assertInstanceOf(
                ReferenceObject.class,
                components.getLinks().get("SharedUserLink"));

        ApiResponse response = inlineValue(
                components.getResponses().get("UserResponse"),
                ApiResponse.class);
        assertInstanceOf(
                ReferenceObject.class,
                response.getLinks().get("self"));

        assertNull(components.getAdditionalFields().get("securitySchemes"));
        assertNull(components.getAdditionalFields().get("links"));
    }

    @Test
    void reportsSecurityAndLinkMappingErrorsAtExactPaths() {
        ObjectValue source = parse(TestResources.readFixture(
                "v31/decode/invalid/components-security-links-errors.yaml"));

        AdapterFailure<?> failure = assertInstanceOf(
                AdapterFailure.class,
                decoder.decode(source, OpenApiVersion.V3_1_2));
        List<OpenApiDiagnostic> diagnostics = failure.diagnostics();

        assertEquals(10, diagnostics.size());
        assertHasDiagnostic(
                diagnostics,
                OpenApiDiagnosticCodes.MAPPING_INVALID_VALUE,
                "/components/securitySchemes/Unknown/type");
        assertHasDiagnostic(
                diagnostics,
                OpenApiDiagnosticCodes.MAPPING_MISSING_REQUIRED_FIELD,
                "/components/securitySchemes/MissingApiKeyFields/name");
        assertHasDiagnostic(
                diagnostics,
                OpenApiDiagnosticCodes.MAPPING_MISSING_REQUIRED_FIELD,
                "/components/securitySchemes/MissingApiKeyFields/in");
        assertHasDiagnostic(
                diagnostics,
                OpenApiDiagnosticCodes.MAPPING_TYPE_MISMATCH,
                "/components/securitySchemes/BrokenOAuth/flows/implicit/scopes/users:read");
        assertHasDiagnostic(
                diagnostics,
                OpenApiDiagnosticCodes.MAPPING_MISSING_REQUIRED_FIELD,
                "/components/links/MissingTarget");
        assertHasDiagnostic(
                diagnostics,
                OpenApiDiagnosticCodes.MAPPING_INVALID_VALUE,
                "/components/links/BothTargets");
        assertHasDiagnostic(
                diagnostics,
                OpenApiDiagnosticCodes.MAPPING_TYPE_MISMATCH,
                "/components/links/BrokenParameters/parameters/userId");
        assertHasDiagnostic(
                diagnostics,
                OpenApiDiagnosticCodes.MAPPING_MISSING_REQUIRED_FIELD,
                "/components/links/BrokenServer/server/url");
        assertHasDiagnostic(
                diagnostics,
                OpenApiDiagnosticCodes.MAPPING_TYPE_MISMATCH,
                "/components/links/BrokenServer/server/variables/region/enum/1");
        assertHasDiagnostic(
                diagnostics,
                OpenApiDiagnosticCodes.MAPPING_MISSING_REQUIRED_FIELD,
                "/components/links/BrokenServer/server/variables/region/default");
    }

    private static void assertHasDiagnostic(
            List<OpenApiDiagnostic> diagnostics,
            DiagnosticCode code,
            String path) {
        assertTrue(diagnostics.stream().anyMatch(diagnostic ->
                diagnostic.code().equals(code)
                        && diagnostic.path().toPointer().equals(path)));
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
