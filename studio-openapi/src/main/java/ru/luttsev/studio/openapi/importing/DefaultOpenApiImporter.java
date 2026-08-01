package ru.luttsev.studio.openapi.importing;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;
import ru.luttsev.studio.core.model.OpenApiDocument;
import ru.luttsev.studio.core.model.OpenApiVersion;
import ru.luttsev.studio.openapi.diagnostic.DiagnosticPhase;
import ru.luttsev.studio.openapi.diagnostic.OpenApiDiagnostic;
import ru.luttsev.studio.openapi.internal.orchestration.OrchestrationDiagnostics;
import ru.luttsev.studio.openapi.result.AdapterResult;
import ru.luttsev.studio.openapi.result.AdapterSuccess;
import ru.luttsev.studio.openapi.result.DetectedVersion;
import ru.luttsev.studio.openapi.result.ImportFailure;
import ru.luttsev.studio.openapi.result.ImportResult;
import ru.luttsev.studio.openapi.result.ImportSuccess;
import ru.luttsev.studio.openapi.result.StructuralValidationResult;
import ru.luttsev.studio.openapi.result.StructuralValidationSuccess;
import ru.luttsev.studio.openapi.result.SyntaxResult;
import ru.luttsev.studio.openapi.result.SyntaxSuccess;
import ru.luttsev.studio.openapi.result.VersionDetectionResult;
import ru.luttsev.studio.openapi.syntax.JacksonOpenApiSyntaxCodec;
import ru.luttsev.studio.openapi.syntax.OpenApiSyntaxParser;
import ru.luttsev.studio.openapi.syntax.ParsedDocument;
import ru.luttsev.studio.openapi.validation.DefaultOpenApiSemanticValidator;
import ru.luttsev.studio.openapi.validation.DefaultOpenApiStructuralValidator;
import ru.luttsev.studio.openapi.validation.OpenApiSemanticValidator;
import ru.luttsev.studio.openapi.validation.OpenApiStructuralValidator;
import ru.luttsev.studio.openapi.version.DefaultOpenApiVersionDetector;
import ru.luttsev.studio.openapi.version.OpenApiVersionAdapter;
import ru.luttsev.studio.openapi.version.OpenApiVersionAdapterRegistry;
import ru.luttsev.studio.openapi.version.OpenApiVersionAdapters;
import ru.luttsev.studio.openapi.version.OpenApiVersionDetector;

public final class DefaultOpenApiImporter implements OpenApiImporter {

    private final OpenApiSyntaxParser syntaxParser;
    private final OpenApiVersionDetector versionDetector;
    private final OpenApiStructuralValidator structuralValidator;
    private final OpenApiVersionAdapterRegistry adapterRegistry;
    private final OpenApiSemanticValidator semanticValidator;

    public DefaultOpenApiImporter() {
        this(
                new JacksonOpenApiSyntaxCodec(),
                new DefaultOpenApiVersionDetector(),
                new DefaultOpenApiStructuralValidator(),
                OpenApiVersionAdapters.defaults(),
                new DefaultOpenApiSemanticValidator());
    }

    public DefaultOpenApiImporter(
            OpenApiSyntaxParser syntaxParser,
            OpenApiVersionDetector versionDetector,
            OpenApiStructuralValidator structuralValidator,
            OpenApiVersionAdapterRegistry adapterRegistry,
            OpenApiSemanticValidator semanticValidator) {
        this.syntaxParser = Objects.requireNonNull(
                syntaxParser,
                "syntaxParser must not be null");
        this.versionDetector = Objects.requireNonNull(
                versionDetector,
                "versionDetector must not be null");
        this.structuralValidator = Objects.requireNonNull(
                structuralValidator,
                "structuralValidator must not be null");
        this.adapterRegistry = Objects.requireNonNull(
                adapterRegistry,
                "adapterRegistry must not be null");
        this.semanticValidator = Objects.requireNonNull(
                semanticValidator,
                "semanticValidator must not be null");
    }

    @Override
    public ImportResult importDocument(
            String content,
            ImportOptions options) {
        Objects.requireNonNull(content, "content must not be null");
        Objects.requireNonNull(options, "options must not be null");
        ArrayList<OpenApiDiagnostic> diagnostics = new ArrayList<>();

        SyntaxResult<ParsedDocument> syntaxResult =
                syntaxParser.parse(content, options);
        diagnostics.addAll(syntaxResult.diagnostics());
        if (!(syntaxResult instanceof SyntaxSuccess<?> syntaxSuccess)) {
            return new ImportFailure(diagnostics);
        }
        ParsedDocument parsedDocument =
                (ParsedDocument) syntaxSuccess.value();

        VersionDetectionResult versionResult =
                versionDetector.detect(parsedDocument.root());
        diagnostics.addAll(versionResult.diagnostics());
        if (!(versionResult instanceof DetectedVersion detectedVersion)) {
            return new ImportFailure(diagnostics);
        }
        OpenApiVersion version = detectedVersion.version();

        Optional<OpenApiVersionAdapter> adapter = adapterRegistry.find(version);
        if (adapter.isEmpty()) {
            diagnostics.add(OrchestrationDiagnostics.unsupportedAdapter(
                    version,
                    DiagnosticPhase.MAPPING));
            return new ImportFailure(diagnostics);
        }

        StructuralValidationResult structuralResult =
                structuralValidator.validate(parsedDocument.root(), version);
        diagnostics.addAll(structuralResult.diagnostics());
        if (!(structuralResult instanceof StructuralValidationSuccess)) {
            return new ImportFailure(diagnostics);
        }

        AdapterResult<OpenApiDocument> adapterResult = adapter.orElseThrow()
                .decode(parsedDocument.root(), version);
        diagnostics.addAll(adapterResult.diagnostics());
        if (!(adapterResult instanceof AdapterSuccess<?> adapterSuccess)) {
            return new ImportFailure(diagnostics);
        }
        OpenApiDocument document =
                (OpenApiDocument) adapterSuccess.value();

        diagnostics.addAll(semanticValidator.validate(document));
        if (OrchestrationDiagnostics.hasErrors(diagnostics)) {
            return new ImportFailure(diagnostics);
        }
        return new ImportSuccess(document, diagnostics);
    }
}
