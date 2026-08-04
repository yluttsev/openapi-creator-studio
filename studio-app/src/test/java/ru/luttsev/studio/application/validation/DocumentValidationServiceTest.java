package ru.luttsev.studio.application.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.luttsev.studio.application.document.DocumentNotFoundException;
import ru.luttsev.studio.application.workspace.DocumentSession;
import ru.luttsev.studio.core.document.NewDocumentParameters;
import ru.luttsev.studio.core.document.OpenApiDocumentFactory;
import ru.luttsev.studio.core.model.OpenApiDocument;
import ru.luttsev.studio.core.model.OpenApiVersion;
import ru.luttsev.studio.core.validation.DocumentValidator;
import ru.luttsev.studio.infrastructure.workspace.InMemoryDocumentWorkspace;

class DocumentValidationServiceTest {

    private InMemoryDocumentWorkspace workspace;
    private DocumentValidationService service;

    @BeforeEach
    void setUp() {
        workspace = new InMemoryDocumentWorkspace();
        service = new DocumentValidationService(
                workspace,
                new DocumentValidator());
    }

    @Test
    void returnsRevisionAndIssuesForInvalidIntermediateState() {
        OpenApiDocument document = document();
        document.setOpenApiVersion(null);
        DocumentSession session = workspace.create(document);

        DocumentValidation validation = service.validate(session.id());

        assertThat(validation.revision()).isZero();
        assertThat(validation.valid()).isFalse();
        assertThat(validation.issues())
                .singleElement()
                .satisfies(issue -> {
                    assertThat(issue.code().value())
                            .isEqualTo("document.openapi-version.missing");
                    assertThat(issue.path().toPointer()).isEqualTo("/openapi");
                });
    }

    @Test
    void rejectsUnknownSession() {
        assertThatThrownBy(() -> service.validate(UUID.randomUUID()))
                .isInstanceOf(DocumentNotFoundException.class);
    }

    private static OpenApiDocument document() {
        return new OpenApiDocumentFactory().create(new NewDocumentParameters(
                OpenApiVersion.V3_1_2,
                "Test API",
                "1.0.0"));
    }
}
