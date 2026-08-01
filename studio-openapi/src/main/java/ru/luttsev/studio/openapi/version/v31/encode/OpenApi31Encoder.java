package ru.luttsev.studio.openapi.version.v31.encode;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import ru.luttsev.studio.core.model.OpenApiDocument;
import ru.luttsev.studio.core.model.OpenApiVersion;
import ru.luttsev.studio.core.model.value.ObjectValue;
import ru.luttsev.studio.core.navigation.DocumentPath;
import ru.luttsev.studio.openapi.diagnostic.DiagnosticPhase;
import ru.luttsev.studio.openapi.diagnostic.DiagnosticSeverity;
import ru.luttsev.studio.openapi.diagnostic.OpenApiDiagnostic;
import ru.luttsev.studio.openapi.diagnostic.OpenApiDiagnosticCodes;
import ru.luttsev.studio.openapi.result.AdapterFailure;
import ru.luttsev.studio.openapi.result.AdapterResult;
import ru.luttsev.studio.openapi.result.AdapterSuccess;
import ru.luttsev.studio.openapi.version.OpenApiVersionFamily;

public final class OpenApi31Encoder {

    private static final Set<String> MAPPED_FIELDS = Set.of(
            "openapi",
            "self",
            "info",
            "jsonSchemaDialect",
            "servers",
            "paths",
            "webhooks",
            "components",
            "security",
            "tags",
            "externalDocs");
    private static final DocumentPath VERSION_PATH =
            DocumentPath.root().child("openapi");

    private final InfoEncoder infoEncoder = new InfoEncoder();
    private final ServerEncoder serverEncoder = new ServerEncoder();
    private final PathsEncoder pathsEncoder = new PathsEncoder();
    private final PathItemEncoder pathItemEncoder = new PathItemEncoder(
            new PayloadEncoder(new SchemaEncoder()));
    private final ComponentsEncoder componentsEncoder = new ComponentsEncoder();
    private final SecurityRequirementEncoder securityRequirementEncoder =
            new SecurityRequirementEncoder();
    private final TagEncoder tagEncoder = new TagEncoder();
    private final ExternalDocumentationEncoder externalDocumentationEncoder =
            new ExternalDocumentationEncoder();

    public AdapterResult<ObjectValue> encode(
            OpenApiDocument document,
            OpenApiVersion targetVersion) {
        Objects.requireNonNull(document, "document must not be null");
        Objects.requireNonNull(
                targetVersion,
                "targetVersion must not be null");
        if (!OpenApiVersionFamily.V3_1.supports(targetVersion)) {
            return unsupportedVersion(targetVersion);
        }

        EncodeContext context = EncodeContext.root(targetVersion);
        ObjectValueBuilder target = new ObjectValueBuilder()
                .putString("openapi", targetVersion.value());
        if (document.getSelf() != null) {
            context.unsupportedField("self");
        }
        if (document.getInfo() != null) {
            target.put(
                    "info",
                    infoEncoder.encode(
                            document.getInfo(),
                            context.child("info")));
        }
        if (document.getJsonSchemaDialect() != null) {
            target.putString(
                    "jsonSchemaDialect",
                    document.getJsonSchemaDialect().value());
        }
        if (document.getServers() != null
                && !document.getServers().isEmpty()) {
            target.put(
                    "servers",
                    serverEncoder.encodeList(
                            document.getServers(),
                            context.child("servers")));
        }
        if (document.getPaths() != null) {
            target.put(
                    "paths",
                    pathsEncoder.encode(
                            document.getPaths(),
                            context.child("paths")));
        }
        if (document.getWebhooks() != null
                && !document.getWebhooks().isEmpty()) {
            target.put(
                    "webhooks",
                    pathItemEncoder.encodeMap(
                            document.getWebhooks(),
                            context.child("webhooks")));
        }
        if (document.getComponents() != null) {
            target.put(
                    "components",
                    componentsEncoder.encode(
                            document.getComponents(),
                            context.child("components")));
        }
        if (document.getSecurity() != null
                && !document.getSecurity().isEmpty()) {
            target.put(
                    "security",
                    securityRequirementEncoder.encodeList(
                            document.getSecurity(),
                            context.child("security")));
        }
        if (document.getTags() != null && !document.getTags().isEmpty()) {
            target.put(
                    "tags",
                    tagEncoder.encodeList(
                            document.getTags(),
                            context.child("tags")));
        }
        if (document.getExternalDocs() != null) {
            target.put(
                    "externalDocs",
                    externalDocumentationEncoder.encode(
                            document.getExternalDocs(),
                            context.child("externalDocs")));
        }
        AdditionalFieldsEncoder.copy(
                document,
                target,
                MAPPED_FIELDS,
                context);
        ObjectValue encoded = target.build();

        if (context.hasErrors()) {
            return new AdapterFailure<>(context.diagnostics());
        }
        return new AdapterSuccess<>(encoded, context.diagnostics());
    }

    private static AdapterFailure<ObjectValue> unsupportedVersion(
            OpenApiVersion targetVersion) {
        OpenApiDiagnostic diagnostic = new OpenApiDiagnostic(
                OpenApiDiagnosticCodes.UNSUPPORTED_MAPPING_VERSION,
                DiagnosticSeverity.ERROR,
                DiagnosticPhase.VERSION_COMPATIBILITY,
                "OpenAPI 3.1 encoder does not support version "
                        + targetVersion.value(),
                VERSION_PATH);
        return new AdapterFailure<>(List.of(diagnostic));
    }
}
