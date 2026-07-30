package ru.luttsev.studio.openapi.version.v31.decode;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import ru.luttsev.studio.core.model.OpenApiDocument;
import ru.luttsev.studio.core.model.OpenApiVersion;
import ru.luttsev.studio.core.model.info.Info;
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

public final class OpenApi31Decoder {

    private static final Set<String> MAPPED_FIELDS =
            Set.of("openapi", "info", "jsonSchemaDialect");
    private static final DocumentPath VERSION_PATH =
            DocumentPath.root().child("openapi");

    private final InfoDecoder infoDecoder = new InfoDecoder();

    public AdapterResult<OpenApiDocument> decode(
            ObjectValue source,
            OpenApiVersion version) {
        Objects.requireNonNull(source, "source must not be null");
        Objects.requireNonNull(version, "version must not be null");

        if (!OpenApiVersionFamily.V3_1.supports(version)) {
            return unsupportedVersion(version);
        }

        DecodeContext context = DecodeContext.root(version);
        ObjectValueReader reader = new ObjectValueReader(source, context);
        OpenApiDocument document = new OpenApiDocument();
        document.setOpenApiVersion(version);

        String declaredVersion = reader.requiredString("openapi");
        if (declaredVersion != null
                && !version.value().equals(declaredVersion)) {
            context.child("openapi").error(
                    OpenApiDiagnosticCodes.MAPPING_VERSION_MISMATCH,
                    "Detected OpenAPI version "
                            + version.value()
                            + " does not match document version "
                            + declaredVersion);
        }

        ObjectValue infoSource = reader.requiredObject("info");
        if (infoSource != null) {
            Info info = infoDecoder.decode(
                    infoSource,
                    context.child("info"));
            document.setInfo(info);
        }

        document.setJsonSchemaDialect(
                reader.optionalUriReference("jsonSchemaDialect"));
        AdditionalFieldsMapper.copy(source, document, MAPPED_FIELDS);

        if (context.hasErrors()) {
            return new AdapterFailure<>(context.diagnostics());
        }
        return new AdapterSuccess<>(document, context.diagnostics());
    }

    private static AdapterFailure<OpenApiDocument> unsupportedVersion(
            OpenApiVersion version) {
        OpenApiDiagnostic diagnostic = new OpenApiDiagnostic(
                OpenApiDiagnosticCodes.UNSUPPORTED_MAPPING_VERSION,
                DiagnosticSeverity.ERROR,
                DiagnosticPhase.MAPPING,
                "OpenAPI 3.1 decoder does not support version "
                        + version.value(),
                VERSION_PATH);
        return new AdapterFailure<>(List.of(diagnostic));
    }
}
