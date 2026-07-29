package ru.luttsev.studio.core.command.schema;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import ru.luttsev.studio.core.command.CommandContext;
import ru.luttsev.studio.core.command.CommandResults;
import ru.luttsev.studio.core.command.DocumentCommand;
import ru.luttsev.studio.core.command.result.CommandCode;
import ru.luttsev.studio.core.command.result.CommandResult;
import ru.luttsev.studio.core.model.schema.Schema;
import ru.luttsev.studio.core.model.schema.SchemaDefinition;
import ru.luttsev.studio.core.navigation.DocumentPath;

public record AddSchemaPropertyCommand(
        DocumentPath schemaPath,
        String propertyName)
        implements DocumentCommand {

    private static final CommandCode SCHEMA_NOT_FOUND =
            new CommandCode("schema.not-found");
    private static final CommandCode BLANK_NAME =
            new CommandCode("schema.property.name.blank");
    private static final CommandCode ALREADY_EXISTS =
            new CommandCode("schema.property.already-exists");

    public AddSchemaPropertyCommand {
        Objects.requireNonNull(schemaPath, "schemaPath must not be null");
        Objects.requireNonNull(propertyName, "propertyName must not be null");
    }

    @Override
    public CommandResult execute(CommandContext context) {
        Objects.requireNonNull(context, "context must not be null");

        Optional<SchemaDefinition> resolvedSchema = context.navigator()
                .find(
                        context.document(),
                        schemaPath,
                        SchemaDefinition.class);
        if (resolvedSchema.isEmpty()) {
            return CommandResults.rejected(
                    SCHEMA_NOT_FOUND,
                    "Schema definition does not exist",
                    schemaPath);
        }

        DocumentPath propertyPath = schemaPath
                .child("properties")
                .child(propertyName);
        if (propertyName.isBlank()) {
            return CommandResults.rejected(
                    BLANK_NAME,
                    "Schema property name must not be blank",
                    propertyPath);
        }

        SchemaDefinition schema = resolvedSchema.orElseThrow();
        Map<String, Schema> properties = schema.getProperties();
        if (properties != null && properties.containsKey(propertyName)) {
            return CommandResults.rejected(
                    ALREADY_EXISTS,
                    "Schema property already exists",
                    propertyPath);
        }

        if (properties == null) {
            properties = new LinkedHashMap<>();
            schema.setProperties(properties);
        }
        properties.put(propertyName, new SchemaDefinition());
        return CommandResults.succeeded(propertyPath);
    }
}
