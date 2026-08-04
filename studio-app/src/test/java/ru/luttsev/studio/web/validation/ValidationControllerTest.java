package ru.luttsev.studio.web.validation;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import ru.luttsev.studio.application.validation.DocumentValidationService;
import ru.luttsev.studio.core.document.NewDocumentParameters;
import ru.luttsev.studio.core.document.OpenApiDocumentFactory;
import ru.luttsev.studio.core.model.OpenApiDocument;
import ru.luttsev.studio.core.model.OpenApiVersion;
import ru.luttsev.studio.core.validation.DocumentValidator;
import ru.luttsev.studio.generated.model.DiagnosticPhase;
import ru.luttsev.studio.generated.model.ValidationResponse;
import ru.luttsev.studio.infrastructure.workspace.InMemoryDocumentWorkspace;

class ValidationControllerTest {

    @Test
    void returnsSemanticDiagnosticsAndCurrentEtag() {
        InMemoryDocumentWorkspace workspace = new InMemoryDocumentWorkspace();
        OpenApiDocument document = new OpenApiDocumentFactory().create(
                new NewDocumentParameters(
                        OpenApiVersion.V3_1_2,
                        "Test API",
                        "1.0.0"));
        document.setOpenApiVersion(null);
        UUID documentId = workspace.create(document).id();
        ValidationController controller = new ValidationController(
                new DocumentValidationService(
                        workspace,
                        new DocumentValidator()),
                new ValidationResponseMapperImpl());

        ResponseEntity<ValidationResponse> response =
                controller.validateDocument(documentId);

        assertThat(response.getHeaders().getETag()).isEqualTo("\"0\"");
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getValid()).isFalse();
        assertThat(response.getBody().getDiagnostics())
                .singleElement()
                .satisfies(diagnostic -> {
                    assertThat(diagnostic.getPhase())
                            .isEqualTo(DiagnosticPhase.SEMANTIC_VALIDATION);
                    assertThat(diagnostic.getPath()).isEqualTo("/openapi");
                });
    }
}
