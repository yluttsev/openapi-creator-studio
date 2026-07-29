package ru.luttsev.studio.core.command.schema;

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
import ru.luttsev.studio.core.model.schema.SchemaDefinition;
import ru.luttsev.studio.core.navigation.DocumentPath;

class SchemaCommandTest {

    private static final DocumentPath USER_PATH =
            DocumentPath.parse("/components/schemas/User");

    private final CommandExecutor executor = new CommandExecutor();

    @Test
    void addsSchemaAndRejectsDuplicate() {
        OpenApiDocument document = CommandTestFixture.document();

        CommandSucceeded added = assertInstanceOf(
                CommandSucceeded.class,
                executor.execute(document, new AddSchemaCommand("User")));
        CommandRejected duplicate = assertInstanceOf(
                CommandRejected.class,
                executor.execute(document, new AddSchemaCommand("User")));

        assertInstanceOf(
                SchemaDefinition.class,
                document.getComponents().getSchemas().get("User"));
        assertEquals(List.of(USER_PATH), added.changedPaths());
        assertEquals(
                "schema.already-exists",
                duplicate.issues().getFirst().code().value());
        assertEquals(1, document.getComponents().getSchemas().size());
    }

    @Test
    void addsPropertyAndRejectsDuplicate() {
        OpenApiDocument document = CommandTestFixture.document();
        document.getComponents().getSchemas().put("User", new SchemaDefinition());

        CommandSucceeded added = assertInstanceOf(
                CommandSucceeded.class,
                executor.execute(
                        document,
                        new AddSchemaPropertyCommand(USER_PATH, "id")));
        CommandRejected duplicate = assertInstanceOf(
                CommandRejected.class,
                executor.execute(
                        document,
                        new AddSchemaPropertyCommand(USER_PATH, "id")));

        SchemaDefinition user = (SchemaDefinition)
                document.getComponents().getSchemas().get("User");
        assertInstanceOf(SchemaDefinition.class, user.getProperties().get("id"));
        assertEquals(
                List.of(USER_PATH.child("properties").child("id")),
                added.changedPaths());
        assertEquals(
                "schema.property.already-exists",
                duplicate.issues().getFirst().code().value());
        assertEquals(1, user.getProperties().size());
    }

    @Test
    void removesPropertyAndRequiredMarkerTogether() {
        OpenApiDocument document = CommandTestFixture.document();
        SchemaDefinition user = new SchemaDefinition();
        user.getProperties().put("id", new SchemaDefinition());
        user.getRequired().add("id");
        document.getComponents().getSchemas().put("User", user);

        CommandSucceeded result = assertInstanceOf(
                CommandSucceeded.class,
                executor.execute(
                        document,
                        new RemoveSchemaPropertyCommand(USER_PATH, "id")));

        assertFalse(user.getProperties().containsKey("id"));
        assertFalse(user.getRequired().contains("id"));
        assertEquals(
                List.of(
                        USER_PATH.child("properties").child("id"),
                        USER_PATH.child("required")),
                result.changedPaths());
    }

    @Test
    void rejectsRemovingReferencedPropertyWithoutPartialMutation() {
        OpenApiDocument document = CommandTestFixture.document();
        SchemaDefinition user = new SchemaDefinition();
        user.getProperties().put("id", new SchemaDefinition());
        user.getRequired().add("id");
        document.getComponents().getSchemas().put("User", user);
        document.getComponents()
                .getSchemas()
                .put(
                        "UserIdAlias",
                        CommandTestFixture.referenceTo(
                                "#/components/schemas/User/properties/id"));

        CommandRejected result = assertInstanceOf(
                CommandRejected.class,
                executor.execute(
                        document,
                        new RemoveSchemaPropertyCommand(USER_PATH, "id")));

        assertEquals(
                "schema.property.is-referenced",
                result.issues().getFirst().code().value());
        assertEquals(
                List.of(DocumentPath.parse(
                        "/components/schemas/UserIdAlias/$ref")),
                result.issues().getFirst().relatedPaths());
        assertTrue(user.getProperties().containsKey("id"));
        assertTrue(user.getRequired().contains("id"));
    }
}
