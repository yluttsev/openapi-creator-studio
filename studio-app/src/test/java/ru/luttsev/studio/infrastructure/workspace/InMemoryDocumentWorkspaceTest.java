package ru.luttsev.studio.infrastructure.workspace;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.luttsev.studio.application.workspace.DocumentSession;
import ru.luttsev.studio.application.workspace.RevisionConflictException;
import ru.luttsev.studio.application.workspace.WorkspaceCommandExecution;
import ru.luttsev.studio.core.command.path.AddPathCommand;
import ru.luttsev.studio.core.command.result.CommandRejected;
import ru.luttsev.studio.core.command.result.CommandSucceeded;
import ru.luttsev.studio.core.model.OpenApiDocument;
import ru.luttsev.studio.testsupport.OpenApiDocuments;

class InMemoryDocumentWorkspaceTest {

    private InMemoryDocumentWorkspace workspace;

    @BeforeEach
    void setUp() {
        workspace = new InMemoryDocumentWorkspace();
    }

    @Test
    void createsAndFindsDocumentAtInitialRevision() {
        OpenApiDocument document = OpenApiDocuments.blank();

        DocumentSession created = workspace.create(document);

        assertThat(created.id()).isNotNull();
        assertThat(created.revision()).isZero();
        assertThat(created.document()).isSameAs(document);
        assertThat(workspace.find(created.id())).contains(created);
    }

    @Test
    void returnsEmptyForUnknownDocument() {
        Optional<DocumentSession> session = workspace.find(UUID.randomUUID());

        assertThat(session).isEmpty();
    }

    @Test
    void inspectsDocumentAtCurrentRevision() {
        DocumentSession created = workspace.create(OpenApiDocuments.blank());

        String title = workspace.inspect(
                        created.id(),
                        session -> session.document().getInfo().getTitle())
                .orElseThrow();

        assertThat(title).isEqualTo("Test API");
    }

    @Test
    void deletesExistingDocument() {
        DocumentSession created = workspace.create(OpenApiDocuments.blank());

        boolean deleted = workspace.delete(created.id(), created.revision());

        assertThat(deleted).isTrue();
        assertThat(workspace.find(created.id())).isEmpty();
        assertThat(workspace.delete(created.id(), created.revision())).isFalse();
    }

    @Test
    void rejectsDeleteWithStaleRevision() {
        DocumentSession created = workspace.create(OpenApiDocuments.blank());
        workspace.execute(
                created.id(),
                created.revision(),
                new AddPathCommand("/users"));

        assertThatThrownBy(() -> workspace.delete(
                created.id(),
                created.revision()))
                .isInstanceOf(RevisionConflictException.class);

        assertThat(workspace.find(created.id())).isPresent();
    }

    @Test
    void incrementsRevisionAfterSuccessfulCommand() {
        DocumentSession created = workspace.create(OpenApiDocuments.blank());

        WorkspaceCommandExecution execution = workspace.execute(
                        created.id(),
                        created.revision(),
                        new AddPathCommand("/users"))
                .orElseThrow();

        assertThat(execution.result()).isInstanceOf(CommandSucceeded.class);
        assertThat(execution.session().revision()).isEqualTo(1);
        assertThat(execution.session().document().getPaths().getItems())
                .containsKey("/users");
    }

    @Test
    void keepsRevisionAfterRejectedCommand() {
        DocumentSession created = workspace.create(OpenApiDocuments.blank());
        workspace.execute(
                created.id(),
                created.revision(),
                new AddPathCommand("/users"));

        WorkspaceCommandExecution rejected = workspace.execute(
                        created.id(),
                        1,
                        new AddPathCommand("/users"))
                .orElseThrow();

        assertThat(rejected.result()).isInstanceOf(CommandRejected.class);
        assertThat(rejected.session().revision()).isEqualTo(1);
    }

    @Test
    void rejectsCommandWithStaleRevision() {
        DocumentSession created = workspace.create(OpenApiDocuments.blank());
        workspace.execute(
                created.id(),
                created.revision(),
                new AddPathCommand("/users"));

        assertThatThrownBy(() -> workspace.execute(
                created.id(),
                created.revision(),
                new AddPathCommand("/orders")))
                .isInstanceOf(RevisionConflictException.class)
                .satisfies(exception -> {
                    RevisionConflictException conflict =
                            (RevisionConflictException) exception;
                    assertThat(conflict.getDocumentId()).isEqualTo(created.id());
                    assertThat(conflict.getExpectedRevision()).isZero();
                    assertThat(conflict.getActualRevision()).isEqualTo(1);
                });
    }
}
