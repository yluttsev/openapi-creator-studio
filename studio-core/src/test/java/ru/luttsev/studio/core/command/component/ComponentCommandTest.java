package ru.luttsev.studio.core.command.component;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import ru.luttsev.studio.core.command.CommandExecutor;
import ru.luttsev.studio.core.command.CommandTestFixture;
import ru.luttsev.studio.core.command.result.CommandRejected;
import ru.luttsev.studio.core.command.result.CommandSucceeded;
import ru.luttsev.studio.core.model.OpenApiDocument;
import ru.luttsev.studio.core.model.parameter.Parameter;
import ru.luttsev.studio.core.model.reference.InlineObject;
import ru.luttsev.studio.core.model.reference.ReferenceObject;
import ru.luttsev.studio.core.model.schema.SchemaDefinition;
import ru.luttsev.studio.core.model.schema.UriReference;
import ru.luttsev.studio.core.navigation.DocumentPath;

class ComponentCommandTest {

    private static final DocumentPath USER_PATH =
            DocumentPath.parse("/components/schemas/User");

    private final CommandExecutor executor = new CommandExecutor();

    @Test
    void renamesComponentAndUpdatesReferencesToItsSubtree() {
        OpenApiDocument document = CommandTestFixture.document();
        SchemaDefinition user = new SchemaDefinition();
        user.getProperties().put("id", new SchemaDefinition());
        SchemaDefinition userAlias =
                CommandTestFixture.referenceTo("#/components/schemas/User");
        SchemaDefinition userIdAlias = CommandTestFixture.referenceTo(
                "#/components/schemas/User/properties/id");
        SchemaDefinition external = CommandTestFixture.referenceTo(
                "shared.yaml#/components/schemas/User");

        document.getComponents().getSchemas().put("Before", new SchemaDefinition());
        document.getComponents().getSchemas().put("User", user);
        document.getComponents().getSchemas().put("After", new SchemaDefinition());
        document.getComponents().getSchemas().put("UserAlias", userAlias);
        document.getComponents().getSchemas().put("UserIdAlias", userIdAlias);
        document.getComponents().getSchemas().put("External", external);

        CommandSucceeded result = assertInstanceOf(
                CommandSucceeded.class,
                executor.execute(
                        document,
                        new RenameComponentCommand(USER_PATH, "Customer")));

        assertEquals(
                List.of(
                        "Before",
                        "Customer",
                        "After",
                        "UserAlias",
                        "UserIdAlias",
                        "External"),
                new ArrayList<>(document.getComponents().getSchemas().keySet()));
        assertSame(
                user,
                document.getComponents().getSchemas().get("Customer"));
        assertEquals(
                "#/components/schemas/Customer",
                userAlias.getRef().value());
        assertEquals(
                "#/components/schemas/Customer/properties/id",
                userIdAlias.getRef().value());
        assertEquals(
                "shared.yaml#/components/schemas/User",
                external.getRef().value());
        assertTrue(result.changedPaths().contains(
                DocumentPath.parse("/components/schemas/Customer")));
        assertTrue(result.changedPaths().contains(
                DocumentPath.parse("/components/schemas/UserAlias/$ref")));
    }

    @Test
    void rejectsConflictingRenameWithoutChangingDocument() {
        OpenApiDocument document = CommandTestFixture.document();
        SchemaDefinition user = new SchemaDefinition();
        SchemaDefinition existingCustomer = new SchemaDefinition();
        SchemaDefinition alias =
                CommandTestFixture.referenceTo("#/components/schemas/User");
        document.getComponents().getSchemas().put("User", user);
        document.getComponents().getSchemas().put("Customer", existingCustomer);
        document.getComponents().getSchemas().put("Alias", alias);

        CommandRejected result = assertInstanceOf(
                CommandRejected.class,
                executor.execute(
                        document,
                        new RenameComponentCommand(USER_PATH, "Customer")));

        assertEquals("component.name.conflict", result.issues().getFirst().code().value());
        assertSame(user, document.getComponents().getSchemas().get("User"));
        assertSame(
                existingCustomer,
                document.getComponents().getSchemas().get("Customer"));
        assertEquals("#/components/schemas/User", alias.getRef().value());
    }

    @Test
    void renamesReferenceOrComponentAndUpdatesReferenceObject() {
        OpenApiDocument document = CommandTestFixture.document();
        Parameter parameter = new Parameter();
        ReferenceObject<Parameter> alias = new ReferenceObject<>();
        alias.setRef(new UriReference("#/components/parameters/UserId"));
        document.getComponents()
                .getParameters()
                .put("UserId", new InlineObject<>(parameter));
        document.getComponents().getParameters().put("Alias", alias);

        CommandSucceeded result = assertInstanceOf(
                CommandSucceeded.class,
                executor.execute(
                        document,
                        new RenameComponentCommand(
                                DocumentPath.parse(
                                        "/components/parameters/UserId"),
                                "AccountId")));

        InlineObject<?> renamed = assertInstanceOf(
                InlineObject.class,
                document.getComponents()
                        .getParameters()
                        .get("AccountId"));
        assertSame(parameter, renamed.value());
        assertEquals(
                "#/components/parameters/AccountId",
                alias.getRef().value());
        assertTrue(result.changedPaths().contains(DocumentPath.parse(
                "/components/parameters/Alias/$ref")));
    }

    @Test
    void rejectsDeletionWhenComponentOrItsChildIsReferenced() {
        OpenApiDocument document = CommandTestFixture.document();
        SchemaDefinition user = new SchemaDefinition();
        user.getProperties().put("id", new SchemaDefinition());
        document.getComponents().getSchemas().put("User", user);
        document.getComponents()
                .getSchemas()
                .put(
                        "UserIdAlias",
                        CommandTestFixture.referenceTo(
                                "#/components/schemas/User/properties/id"));

        CommandRejected result = assertInstanceOf(
                CommandRejected.class,
                executor.execute(document, new DeleteComponentCommand(USER_PATH)));

        assertEquals("component.is-referenced", result.issues().getFirst().code().value());
        assertEquals(
                List.of(DocumentPath.parse(
                        "/components/schemas/UserIdAlias/$ref")),
                result.issues().getFirst().relatedPaths());
        assertTrue(document.getComponents().getSchemas().containsKey("User"));
    }

    @Test
    void allowsDeletingSelfReferencingComponent() {
        OpenApiDocument document = CommandTestFixture.document();
        SchemaDefinition node = new SchemaDefinition();
        node.getProperties().put(
                "next",
                CommandTestFixture.referenceTo("#/components/schemas/Node"));
        document.getComponents().getSchemas().put("Node", node);

        CommandSucceeded result = assertInstanceOf(
                CommandSucceeded.class,
                executor.execute(
                        document,
                        new DeleteComponentCommand(
                                DocumentPath.parse("/components/schemas/Node"))));

        assertFalse(document.getComponents().getSchemas().containsKey("Node"));
        assertEquals(
                List.of(DocumentPath.parse("/components/schemas/Node")),
                result.changedPaths());
    }
}
