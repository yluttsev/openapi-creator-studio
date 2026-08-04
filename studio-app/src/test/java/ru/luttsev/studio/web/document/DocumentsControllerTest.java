package ru.luttsev.studio.web.document;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ru.luttsev.studio.application.document.DocumentLifecycleService;
import ru.luttsev.studio.application.document.DocumentRepresentationService;
import ru.luttsev.studio.application.document.DocumentValueConverter;
import ru.luttsev.studio.core.document.OpenApiDocumentFactory;
import ru.luttsev.studio.generated.model.CreateDocumentRequest;
import ru.luttsev.studio.generated.model.DocumentResponse;
import ru.luttsev.studio.generated.model.OpenDocumentResponse;
import ru.luttsev.studio.infrastructure.workspace.InMemoryDocumentWorkspace;
import ru.luttsev.studio.openapi.importing.DefaultOpenApiImporter;
import ru.luttsev.studio.openapi.version.OpenApiVersionAdapterRegistry;
import ru.luttsev.studio.openapi.version.OpenApiVersionAdapters;

class DocumentsControllerTest {

    private DocumentsController controller;

    @BeforeEach
    void setUp() {
        OpenApiVersionAdapterRegistry adapters = OpenApiVersionAdapters.defaults();
        DocumentLifecycleService lifecycleService = new DocumentLifecycleService(
                new InMemoryDocumentWorkspace(),
                new OpenApiDocumentFactory(),
                new DefaultOpenApiImporter(),
                adapters);
        DocumentResponseMapper responseMapper =
                new DocumentResponseMapperImpl(new DiagnosticMapperImpl());
        controller = new DocumentsController(
                lifecycleService,
                new DocumentRepresentationService(
                        adapters,
                        new DocumentValueConverter()),
                responseMapper);
    }

    @Test
    void createsGetsAndClosesDocument() {
        CreateDocumentRequest request = new CreateDocumentRequest(
                "Orders API",
                "1.0.0");

        ResponseEntity<OpenDocumentResponse> created =
                controller.createDocument(request);

        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(created.getHeaders().getETag()).isEqualTo("\"0\"");
        assertThat(created.getHeaders().getLocation()).isNotNull();
        OpenDocumentResponse opened = created.getBody();
        assertThat(opened).isNotNull();
        assertThat(opened.getOpenApiVersion()).isEqualTo("3.1.2");

        UUID documentId = opened.getId();
        ResponseEntity<DocumentResponse> current =
                controller.getDocument(documentId);

        assertThat(current.getHeaders().getETag()).isEqualTo("\"0\"");
        assertThat(current.getBody()).isNotNull();
        assertThat(current.getBody().getDocument())
                .containsEntry("openapi", "3.1.2")
                .containsKey("info")
                .containsKey("paths");

        assertThat(controller.closeDocument("\"0\"", documentId)
                .getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void importsYamlAndReturnsImportMetadata() {
        ResponseEntity<OpenDocumentResponse> response =
                controller.importDocument("""
                        openapi: 3.1.2
                        info:
                          title: Imported API
                          version: 2.0.0
                        paths: {}
                        """);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTitle()).isEqualTo("Imported API");
        assertThat(response.getBody().getApiVersion()).isEqualTo("2.0.0");
    }
}
