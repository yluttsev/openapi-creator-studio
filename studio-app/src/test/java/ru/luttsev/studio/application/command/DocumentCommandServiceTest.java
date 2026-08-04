package ru.luttsev.studio.application.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.luttsev.studio.application.workspace.DocumentSession;
import ru.luttsev.studio.core.command.path.AddPathCommand;
import ru.luttsev.studio.infrastructure.workspace.InMemoryDocumentWorkspace;
import ru.luttsev.studio.testsupport.OpenApiDocuments;

class DocumentCommandServiceTest {

    private InMemoryDocumentWorkspace workspace;
    private DocumentCommandService service;
    private DocumentSession initialSession;

    @BeforeEach
    void setUp() {
        workspace = new InMemoryDocumentWorkspace();
        service = new DocumentCommandService(workspace);
        initialSession = workspace.create(OpenApiDocuments.blank());
    }

    @Test
    void returnsNewRevisionAndChangedPathsAfterSuccess() {
        ExecutedDocumentCommand result = service.execute(
                initialSession.id(),
                initialSession.revision(),
                new AddPathCommand("/users"));

        assertThat(result.revision()).isEqualTo(1);
        assertThat(result.changedPaths())
                .extracting(path -> path.toPointer())
                .containsExactly("/paths/~1users");
    }

    @Test
    void exposesIssuesAndKeepsRevisionAfterRejection() {
        service.execute(
                initialSession.id(),
                initialSession.revision(),
                new AddPathCommand("/users"));

        assertThatThrownBy(() -> service.execute(
                initialSession.id(),
                1,
                new AddPathCommand("/users")))
                .isInstanceOf(CommandRejectedException.class)
                .satisfies(exception -> assertThat(
                        ((CommandRejectedException) exception)
                                .getIssues())
                        .hasSize(1));
        assertThat(workspace.find(initialSession.id()).orElseThrow().revision())
                .isEqualTo(1);
    }
}
