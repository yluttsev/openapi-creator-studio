package ru.luttsev.studio.core.command.schema;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import ru.luttsev.studio.core.command.CommandContext;
import ru.luttsev.studio.core.command.CommandResults;
import ru.luttsev.studio.core.command.DocumentCommand;
import ru.luttsev.studio.core.command.result.CommandCode;
import ru.luttsev.studio.core.command.result.CommandResult;
import ru.luttsev.studio.core.command.support.ReferenceUsageSupport;
import ru.luttsev.studio.core.model.schema.Schema;
import ru.luttsev.studio.core.model.schema.SchemaDefinition;
import ru.luttsev.studio.core.navigation.DocumentPath;
import ru.luttsev.studio.core.reference.index.ResolvedReferenceUsage;

public record RemoveSchemaPropertyCommand(
        DocumentPath schemaPath,
        String propertyName)
        implements DocumentCommand {

    private static final CommandCode SCHEMA_NOT_FOUND =
            new CommandCode("schema.not-found");
    private static final CommandCode PROPERTY_NOT_FOUND =
            new CommandCode("schema.property.not-found");
    private static final CommandCode IS_REFERENCED =
            new CommandCode("schema.property.is-referenced");

    public RemoveSchemaPropertyCommand {
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
        SchemaDefinition schema = resolvedSchema.orElseThrow();
        Map<String, Schema> properties = schema.getProperties();
        if (properties == null || !properties.containsKey(propertyName)) {
            return CommandResults.rejected(
                    PROPERTY_NOT_FOUND,
                    "Schema property does not exist",
                    propertyPath);
        }

        List<ResolvedReferenceUsage> usages =
                ReferenceUsageSupport.blockingUsages(
                        context.referenceIndex(),
                        propertyPath);
        if (!usages.isEmpty()) {
            return CommandResults.rejected(
                    IS_REFERENCED,
                    "Schema property is referenced from other document locations",
                    propertyPath,
                    ReferenceUsageSupport.sourcePaths(usages));
        }

        properties.remove(propertyName);
        ArrayList<DocumentPath> changedPaths = new ArrayList<>();
        changedPaths.add(propertyPath);
        Set<String> required = schema.getRequired();
        if (required != null && required.remove(propertyName)) {
            changedPaths.add(schemaPath.child("required"));
        }
        return CommandResults.succeeded(
                changedPaths.toArray(DocumentPath[]::new));
    }
}
