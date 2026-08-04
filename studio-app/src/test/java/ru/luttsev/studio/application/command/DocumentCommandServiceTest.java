package ru.luttsev.studio.application.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.luttsev.studio.application.workspace.DocumentSession;
import ru.luttsev.studio.core.command.path.AddPathCommand;
import ru.luttsev.studio.core.document.NewDocumentParameters;
import ru.luttsev.studio.core.document.OpenApiDocumentFactory;
import ru.luttsev.studio.core.model.OpenApiVersion;
import ru.luttsev.studio.infrastructure.workspace.InMemoryDocumentWorkspace;

class DocumentCommandServiceTest {

    private InMemoryDocumentWorkspace workspace;
    private DocumentCommandService service;
    private DocumentSession session;

    @BeforeEach
    void setUp() {
        workspace = new InMemoryDocumentWorkspace();
        service = new DocumentCommandService(workspace);
        session = workspace.create(new OpenApiDocumentFactory().create(
                new NewDocumentParameters(
                        OpenApiVersion.V3_1_2,
                        "Test API",
                        "1.0.0")));
    }

    @Test
    void returnsNewRevisionAndChangedPathsAfterSuccess() {
        ExecutedDocumentCommand result = service.execute(
                session.id(),
                session.revision(),
                new AddPathCommand("/users"));

        assertThat(result.revision()).isEqualTo(1);
        assertThat(result.changedPaths())
                .extracting(path -> path.toPointer())
                .containsExactly("/paths/~1users");
    }

    @Test
    void exposesIssuesAndKeepsRevisionAfterRejection() {
        service.execute(
                session.id(),
                session.revision(),
                new AddPathCommand("/users"));

        assertThatThrownBy(() -> service.execute(
                session.id(),
                1,
                new AddPathCommand("/users")))
                .isInstanceOf(CommandRejectedException.class)
                .satisfies(exception -> assertThat(
                        ((CommandRejectedException) exception)
                                .getIssues())
                        .hasSize(1));
        assertThat(workspace.find(session.id()).orElseThrow().revision())
                .isEqualTo(1);
    }
}
