package ru.luttsev.studio.openapi.exporting;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;
import ru.luttsev.studio.core.model.OpenApiDocument;
import ru.luttsev.studio.core.model.OpenApiVersion;
import ru.luttsev.studio.core.model.value.ObjectValue;
import ru.luttsev.studio.openapi.diagnostic.DiagnosticPhase;
import ru.luttsev.studio.openapi.diagnostic.OpenApiDiagnostic;
import ru.luttsev.studio.openapi.internal.orchestration.OrchestrationDiagnostics;
import ru.luttsev.studio.openapi.result.AdapterResult;
import ru.luttsev.studio.openapi.result.AdapterSuccess;
import ru.luttsev.studio.openapi.result.ExportFailure;
import ru.luttsev.studio.openapi.result.ExportResult;
import ru.luttsev.studio.openapi.result.ExportSuccess;
import ru.luttsev.studio.openapi.result.StructuralValidationResult;
import ru.luttsev.studio.openapi.result.StructuralValidationSuccess;
import ru.luttsev.studio.openapi.result.SyntaxResult;
import ru.luttsev.studio.openapi.result.SyntaxSuccess;
import ru.luttsev.studio.openapi.syntax.JacksonOpenApiSyntaxCodec;
import ru.luttsev.studio.openapi.syntax.OpenApiSyntaxWriter;
import ru.luttsev.studio.openapi.validation.DefaultOpenApiSemanticValidator;
import ru.luttsev.studio.openapi.validation.DefaultOpenApiStructuralValidator;
import ru.luttsev.studio.openapi.validation.OpenApiSemanticValidator;
import ru.luttsev.studio.openapi.validation.OpenApiStructuralValidator;
import ru.luttsev.studio.openapi.version.OpenApiVersionAdapter;
import ru.luttsev.studio.openapi.version.OpenApiVersionAdapterRegistry;
import ru.luttsev.studio.openapi.version.OpenApiVersionAdapters;

public final class DefaultOpenApiExporter implements OpenApiExporter {

    private final OpenApiSemanticValidator semanticValidator;
    private final OpenApiVersionAdapterRegistry adapterRegistry;
    private final OpenApiStructuralValidator structuralValidator;
    private final OpenApiSyntaxWriter syntaxWriter;

    public DefaultOpenApiExporter() {
        this(
                new DefaultOpenApiSemanticValidator(),
                OpenApiVersionAdapters.defaults(),
                new DefaultOpenApiStructuralValidator(),
                new JacksonOpenApiSyntaxCodec());
    }

    public DefaultOpenApiExporter(
            OpenApiSemanticValidator semanticValidator,
            OpenApiVersionAdapterRegistry adapterRegistry,
            OpenApiStructuralValidator structuralValidator,
            OpenApiSyntaxWriter syntaxWriter) {
        this.semanticValidator = Objects.requireNonNull(
                semanticValidator,
                "semanticValidator must not be null");
        this.adapterRegistry = Objects.requireNonNull(
                adapterRegistry,
                "adapterRegistry must not be null");
        this.structuralValidator = Objects.requireNonNull(
                structuralValidator,
                "structuralValidator must not be null");
        this.syntaxWriter = Objects.requireNonNull(
                syntaxWriter,
                "syntaxWriter must not be null");
    }

    @Override
    public ExportResult exportDocument(
            OpenApiDocument document,
            ExportOptions options) {
        Objects.requireNonNull(document, "document must not be null");
        Objects.requireNonNull(options, "options must not be null");
        ArrayList<OpenApiDiagnostic> diagnostics = new ArrayList<>();

        diagnostics.addAll(semanticValidator.validate(document));
        if (OrchestrationDiagnostics.hasErrors(diagnostics)) {
            return new ExportFailure(diagnostics);
        }

        OpenApiVersion targetVersion = options.targetVersion();
        Optional<OpenApiVersionAdapter> adapter =
                adapterRegistry.find(targetVersion);
        if (adapter.isEmpty()) {
            diagnostics.add(OrchestrationDiagnostics.unsupportedAdapter(
                    targetVersion,
                    DiagnosticPhase.VERSION_COMPATIBILITY));
            return new ExportFailure(diagnostics);
        }

        AdapterResult<ObjectValue> adapterResult = adapter.orElseThrow()
                .encode(document, targetVersion);
        diagnostics.addAll(adapterResult.diagnostics());
        if (!(adapterResult instanceof AdapterSuccess<?> adapterSuccess)) {
            return new ExportFailure(diagnostics);
        }
        ObjectValue encoded = (ObjectValue) adapterSuccess.value();

        StructuralValidationResult structuralResult =
                structuralValidator.validate(encoded, targetVersion);
        diagnostics.addAll(structuralResult.diagnostics());
        if (!(structuralResult instanceof StructuralValidationSuccess)) {
            return new ExportFailure(diagnostics);
        }

        SyntaxResult<String> syntaxResult =
                syntaxWriter.write(encoded, options.format());
        diagnostics.addAll(syntaxResult.diagnostics());
        if (!(syntaxResult instanceof SyntaxSuccess<?> syntaxSuccess)) {
            return new ExportFailure(diagnostics);
        }
        String content = (String) syntaxSuccess.value();
        return new ExportSuccess(content, diagnostics);
    }
}
