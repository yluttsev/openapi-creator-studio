package ru.luttsev.studio.application.exporting;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.luttsev.studio.application.document.DocumentNotFoundException;
import ru.luttsev.studio.application.document.OpenApiProcessingException;
import ru.luttsev.studio.application.workspace.DocumentWorkspace;
import ru.luttsev.studio.core.model.OpenApiDocument;
import ru.luttsev.studio.core.model.OpenApiVersion;
import ru.luttsev.studio.openapi.exporting.ExportOptions;
import ru.luttsev.studio.openapi.exporting.OpenApiExporter;
import ru.luttsev.studio.openapi.format.OpenApiFormat;
import ru.luttsev.studio.openapi.result.ExportFailure;
import ru.luttsev.studio.openapi.result.ExportResult;
import ru.luttsev.studio.openapi.result.ExportSuccess;

@Slf4j
@Service
@RequiredArgsConstructor
public final class DocumentExportService {

    private static final OpenApiVersion DEFAULT_TARGET_VERSION =
            OpenApiVersion.V3_1_2;

    private final DocumentWorkspace workspace;
    private final OpenApiExporter exporter;

    public DocumentExport export(
            UUID documentId,
            OpenApiFormat format,
            OpenApiVersion targetVersion) {
        OpenApiVersion resolvedTargetVersion = targetVersion == null
                ? DEFAULT_TARGET_VERSION
                : targetVersion;
        return workspace.inspect(documentId, session -> export(
                session.revision(),
                session.document(),
                format,
                resolvedTargetVersion))
                .orElseThrow(() -> new DocumentNotFoundException(documentId));
    }

    private DocumentExport export(
            long revision,
            OpenApiDocument document,
            OpenApiFormat format,
            OpenApiVersion targetVersion) {
        ExportResult result = exporter.exportDocument(
                document,
                new ExportOptions(format, targetVersion));
        if (result instanceof ExportFailure failure) {
            log.warn(
                    "OpenAPI document export failed: {} diagnostic(s)",
                    failure.diagnostics().size());
            throw new OpenApiProcessingException(
                    "OpenAPI document export failed",
                    failure.diagnostics());
        }
        ExportSuccess success = (ExportSuccess) result;
        log.info(
                "Exported document at revision {} as {} (target version {})",
                revision,
                format,
                targetVersion.value());
        return new DocumentExport(
                revision,
                format,
                targetVersion,
                fileName(format),
                success.content(),
                success.diagnostics());
    }

    private String fileName(OpenApiFormat format) {
        return switch (format) {
            case YAML -> "openapi.yaml";
            case JSON -> "openapi.json";
        };
    }
}
