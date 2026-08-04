package ru.luttsev.studio.web.exporting;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import ru.luttsev.studio.application.exporting.DocumentExportService;
import ru.luttsev.studio.core.document.NewDocumentParameters;
import ru.luttsev.studio.core.document.OpenApiDocumentFactory;
import ru.luttsev.studio.core.model.OpenApiVersion;
import ru.luttsev.studio.generated.model.ExportRequest;
import ru.luttsev.studio.generated.model.ExportResponse;
import ru.luttsev.studio.generated.model.OpenApiFormat;
import ru.luttsev.studio.infrastructure.workspace.InMemoryDocumentWorkspace;
import ru.luttsev.studio.openapi.exporting.DefaultOpenApiExporter;
import ru.luttsev.studio.web.document.DiagnosticMapperImpl;

class ExportControllerTest {

    @Test
    void returnsExportMetadataContentAndCurrentEtag() {
        InMemoryDocumentWorkspace workspace = new InMemoryDocumentWorkspace();
        UUID documentId = workspace.create(
                new OpenApiDocumentFactory().create(
                        new NewDocumentParameters(
                                OpenApiVersion.V3_1_2,
                                "Test API",
                                "1.0.0")))
                .id();
        ExportController controller = new ExportController(
                new DocumentExportService(
                        workspace,
                        new DefaultOpenApiExporter()),
                new ExportMapperImpl(new DiagnosticMapperImpl()));

        ResponseEntity<ExportResponse> response = controller.exportDocument(
                documentId,
                new ExportRequest(OpenApiFormat.JSON));

        assertThat(response.getHeaders().getETag()).isEqualTo("\"0\"");
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getRevision()).isZero();
        assertThat(response.getBody().getFormat()).isEqualTo(OpenApiFormat.JSON);
        assertThat(response.getBody().getTargetVersion()).isEqualTo("3.1.2");
        assertThat(response.getBody().getFileName()).isEqualTo("openapi.json");
        assertThat(response.getBody().getContent()).contains("\"openapi\"");
        assertThat(response.getBody().getDiagnostics()).isEmpty();
    }
}
