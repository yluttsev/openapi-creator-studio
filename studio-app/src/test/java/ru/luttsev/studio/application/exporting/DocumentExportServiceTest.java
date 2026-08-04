package ru.luttsev.studio.application.exporting;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.luttsev.studio.application.document.DocumentNotFoundException;
import ru.luttsev.studio.application.document.OpenApiProcessingException;
import ru.luttsev.studio.application.workspace.DocumentSession;
import ru.luttsev.studio.core.document.NewDocumentParameters;
import ru.luttsev.studio.core.document.OpenApiDocumentFactory;
import ru.luttsev.studio.core.model.OpenApiDocument;
import ru.luttsev.studio.core.model.OpenApiVersion;
import ru.luttsev.studio.infrastructure.workspace.InMemoryDocumentWorkspace;
import ru.luttsev.studio.openapi.exporting.DefaultOpenApiExporter;
import ru.luttsev.studio.openapi.format.OpenApiFormat;

class DocumentExportServiceTest {

    private InMemoryDocumentWorkspace workspace;
    private DocumentExportService service;

    @BeforeEach
    void setUp() {
        workspace = new InMemoryDocumentWorkspace();
        service = new DocumentExportService(
                workspace,
                new DefaultOpenApiExporter());
    }

    @Test
    void exportsCurrentDocumentWithDefaultTargetVersion() {
        DocumentSession session = workspace.create(document());

        DocumentExport result = service.export(
                session.id(),
                OpenApiFormat.YAML,
                null);

        assertThat(result.revision()).isZero();
        assertThat(result.format()).isEqualTo(OpenApiFormat.YAML);
        assertThat(result.targetVersion()).isEqualTo(OpenApiVersion.V3_1_2);
        assertThat(result.fileName()).isEqualTo("openapi.yaml");
        assertThat(result.content()).contains("openapi: 3.1.2");
        assertThat(result.diagnostics()).isEmpty();
    }

    @Test
    void rejectsInvalidIntermediateDocument() {
        OpenApiDocument document = document();
        document.setOpenApiVersion(null);
        DocumentSession session = workspace.create(document);

        assertThatThrownBy(() -> service.export(
                session.id(),
                OpenApiFormat.JSON,
                OpenApiVersion.V3_1_2))
                .isInstanceOf(OpenApiProcessingException.class)
                .satisfies(exception -> assertThat(
                        ((OpenApiProcessingException) exception)
                                .getDiagnostics())
                        .isNotEmpty());
    }

    @Test
    void rejectsUnknownSession() {
        assertThatThrownBy(() -> service.export(
                UUID.randomUUID(),
                OpenApiFormat.JSON,
                null))
                .isInstanceOf(DocumentNotFoundException.class);
    }

    private static OpenApiDocument document() {
        return new OpenApiDocumentFactory().create(new NewDocumentParameters(
                OpenApiVersion.V3_1_2,
                "Test API",
                "1.0.0"));
    }
}
