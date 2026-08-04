package ru.luttsev.studio.application.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.luttsev.studio.application.document.DocumentNotFoundException;
import ru.luttsev.studio.application.workspace.DocumentSession;
import ru.luttsev.studio.core.validation.DocumentValidator;
import ru.luttsev.studio.infrastructure.workspace.InMemoryDocumentWorkspace;
import ru.luttsev.studio.testsupport.OpenApiDocuments;

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
        DocumentSession session = workspace.create(
                OpenApiDocuments.blankWithoutVersion());

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
}
