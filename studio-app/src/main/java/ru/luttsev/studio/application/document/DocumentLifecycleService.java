package ru.luttsev.studio.application.document;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.luttsev.studio.application.workspace.DocumentSession;
import ru.luttsev.studio.application.workspace.DocumentWorkspace;
import ru.luttsev.studio.core.document.NewDocumentParameters;
import ru.luttsev.studio.core.document.OpenApiDocumentFactory;
import ru.luttsev.studio.core.model.OpenApiDocument;
import ru.luttsev.studio.core.model.OpenApiVersion;
import ru.luttsev.studio.openapi.importing.ImportOptions;
import ru.luttsev.studio.openapi.importing.OpenApiImporter;
import ru.luttsev.studio.openapi.result.ImportFailure;
import ru.luttsev.studio.openapi.result.ImportResult;
import ru.luttsev.studio.openapi.result.ImportSuccess;
import ru.luttsev.studio.openapi.version.OpenApiVersionAdapterRegistry;

@Slf4j
@Service
@RequiredArgsConstructor
public final class DocumentLifecycleService {

    private final DocumentWorkspace workspace;
    private final OpenApiDocumentFactory documentFactory;
    private final OpenApiImporter importer;
    private final OpenApiVersionAdapterRegistry adapterRegistry;

    public OpenedDocument create(
            OpenApiVersion openApiVersion,
            String title,
            String apiVersion,
            String description) {
        if (adapterRegistry.find(openApiVersion).isEmpty()) {
            throw new IllegalArgumentException(
                    "Unsupported OpenAPI version: " + openApiVersion.value());
        }
        OpenApiDocument document = documentFactory.create(
                new NewDocumentParameters(openApiVersion, title, apiVersion));
        document.getInfo().setDescription(description);
        OpenedDocument openedDocument = new OpenedDocument(
                workspace.create(document), List.of());
        log.info(
                "Created document {} (openapi {}, apiVersion {})",
                openedDocument.session().id(),
                openApiVersion.value(),
                apiVersion);
        return openedDocument;
    }

    public OpenedDocument importDocument(String content) {
        ImportResult result = importer.importDocument(
                content,
                ImportOptions.autoDetect());
        if (result instanceof ImportFailure failure) {
            log.warn(
                    "OpenAPI document import failed: {} diagnostic(s)",
                    failure.diagnostics().size());
            throw new OpenApiProcessingException(
                    "OpenAPI document import failed",
                    failure.diagnostics());
        }
        ImportSuccess success = (ImportSuccess) result;
        OpenedDocument openedDocument = new OpenedDocument(
                workspace.create(success.document()),
                success.diagnostics());
        log.info(
                "Imported document {} ({} diagnostic(s))",
                openedDocument.session().id(),
                success.diagnostics().size());
        return openedDocument;
    }

    public DocumentSession get(UUID documentId) {
        log.debug("Fetching document {}", documentId);
        return workspace.find(documentId)
                .orElseThrow(() -> new DocumentNotFoundException(documentId));
    }

    public void close(UUID documentId, long expectedRevision) {
        if (!workspace.delete(documentId, expectedRevision)) {
            throw new DocumentNotFoundException(documentId);
        }
        log.info("Closed document {}", documentId);
    }
}
