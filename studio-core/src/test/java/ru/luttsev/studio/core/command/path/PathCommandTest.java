package ru.luttsev.studio.core.command.path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;
import ru.luttsev.studio.core.command.CommandExecutor;
import ru.luttsev.studio.core.command.CommandTestFixture;
import ru.luttsev.studio.core.command.result.CommandRejected;
import ru.luttsev.studio.core.command.result.CommandSucceeded;
import ru.luttsev.studio.core.model.OpenApiDocument;
import ru.luttsev.studio.core.model.path.HttpMethod;
import ru.luttsev.studio.core.model.path.Operation;
import ru.luttsev.studio.core.model.path.PathItem;
import ru.luttsev.studio.core.navigation.DocumentPath;

class PathCommandTest {

    private static final String USERS = "/users";
    private static final DocumentPath USERS_PATH =
            DocumentPath.parse("/paths/~1users");

    private final CommandExecutor executor = new CommandExecutor();

    @Test
    void addsAndRemovesPath() {
        OpenApiDocument document = CommandTestFixture.document();

        CommandSucceeded added = assertInstanceOf(
                CommandSucceeded.class,
                executor.execute(document, new AddPathCommand(USERS)));
        CommandSucceeded removed = assertInstanceOf(
                CommandSucceeded.class,
                executor.execute(document, new RemovePathCommand(USERS)));

        assertEquals(List.of(USERS_PATH), added.changedPaths());
        assertEquals(List.of(USERS_PATH), removed.changedPaths());
        assertFalse(document.getPaths().getItems().containsKey(USERS));
    }

    @Test
    void canonicalizesStandardMethodAndRejectsItsDuplicate() {
        OpenApiDocument document = CommandTestFixture.document();
        document.getPaths().getItems().put(USERS, new PathItem());

        CommandSucceeded added = assertInstanceOf(
                CommandSucceeded.class,
                executor.execute(
                        document,
                        new AddOperationCommand(
                                USERS,
                                new HttpMethod("get"))));
        CommandRejected duplicate = assertInstanceOf(
                CommandRejected.class,
                executor.execute(
                        document,
                        new AddOperationCommand(USERS, HttpMethod.GET)));

        PathItem users = document.getPaths().getItems().get(USERS);
        assertTrue(users.getOperations().containsKey(HttpMethod.GET));
        assertEquals(
                List.of(USERS_PATH.child("get")),
                added.changedPaths());
        assertEquals(
                "operation.already-exists",
                duplicate.issues().getFirst().code().value());
        assertEquals(1, users.getOperations().size());
    }

    @Test
    void recognizesExistingStandardMethodRegardlessOfStoredCase() {
        OpenApiDocument document = CommandTestFixture.document();
        PathItem users = new PathItem();
        users.getOperations().put(new HttpMethod("get"), new Operation());
        document.getPaths().getItems().put(USERS, users);

        CommandRejected result = assertInstanceOf(
                CommandRejected.class,
                executor.execute(
                        document,
                        new AddOperationCommand(USERS, HttpMethod.GET)));

        assertEquals(
                "operation.already-exists",
                result.issues().getFirst().code().value());
        assertEquals(1, users.getOperations().size());
    }

    @Test
    void storesCustomMethodAndReportsAdditionalOperationPath() {
        OpenApiDocument document = CommandTestFixture.document();
        document.getPaths().getItems().put(USERS, new PathItem());
        HttpMethod subscribe = new HttpMethod("SUBSCRIBE");

        CommandSucceeded result = assertInstanceOf(
                CommandSucceeded.class,
                executor.execute(
                        document,
                        new AddOperationCommand(USERS, subscribe)));

        assertTrue(document.getPaths()
                .getItems()
                .get(USERS)
                .getOperations()
                .containsKey(subscribe));
        assertEquals(
                List.of(USERS_PATH
                        .child("additionalOperations")
                        .child("SUBSCRIBE")),
                result.changedPaths());
    }

    @Test
    void removesOperation() {
        OpenApiDocument document = CommandTestFixture.document();
        PathItem users = new PathItem();
        users.getOperations().put(HttpMethod.GET, new Operation());
        document.getPaths().getItems().put(USERS, users);

        CommandSucceeded result = assertInstanceOf(
                CommandSucceeded.class,
                executor.execute(
                        document,
                        new RemoveOperationCommand(
                                USERS,
                                new HttpMethod("get"))));

        assertTrue(users.getOperations().isEmpty());
        assertEquals(List.of(USERS_PATH.child("get")), result.changedPaths());
    }
}
