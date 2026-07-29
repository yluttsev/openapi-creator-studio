package ru.luttsev.studio.core.command.schema;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import ru.luttsev.studio.core.command.CommandContext;
import ru.luttsev.studio.core.command.CommandResults;
import ru.luttsev.studio.core.command.DocumentCommand;
import ru.luttsev.studio.core.command.result.CommandCode;
import ru.luttsev.studio.core.command.result.CommandResult;
import ru.luttsev.studio.core.model.Components;
import ru.luttsev.studio.core.model.schema.Schema;
import ru.luttsev.studio.core.model.schema.SchemaDefinition;
import ru.luttsev.studio.core.navigation.DocumentPath;

public record AddSchemaCommand(String name) implements DocumentCommand {

    private static final CommandCode BLANK_NAME =
            new CommandCode("schema.name.blank");
    private static final CommandCode ALREADY_EXISTS =
            new CommandCode("schema.already-exists");

    public AddSchemaCommand {
        Objects.requireNonNull(name, "name must not be null");
    }

    @Override
    public CommandResult execute(CommandContext context) {
        Objects.requireNonNull(context, "context must not be null");

        DocumentPath schemaPath = DocumentPath.root()
                .child("components")
                .child("schemas")
                .child(name);
        if (name.isBlank()) {
            return CommandResults.rejected(
                    BLANK_NAME,
                    "Schema name must not be blank",
                    schemaPath);
        }

        Components components = context.document().getComponents();
        Map<String, Schema> schemas =
                components == null ? null : components.getSchemas();
        if (schemas != null && schemas.containsKey(name)) {
            return CommandResults.rejected(
                    ALREADY_EXISTS,
                    "Schema already exists",
                    schemaPath);
        }

        if (components == null) {
            components = new Components();
            context.document().setComponents(components);
        }
        if (schemas == null) {
            schemas = new LinkedHashMap<>();
            components.setSchemas(schemas);
        }
        schemas.put(name, new SchemaDefinition());
        return CommandResults.succeeded(schemaPath);
    }
}
