package ru.luttsev.studio.core.command.component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import ru.luttsev.studio.core.command.CommandContext;
import ru.luttsev.studio.core.command.CommandResults;
import ru.luttsev.studio.core.command.DocumentCommand;
import ru.luttsev.studio.core.command.result.CommandCode;
import ru.luttsev.studio.core.command.result.CommandResult;
import ru.luttsev.studio.core.model.Components;
import ru.luttsev.studio.core.navigation.DocumentPath;

public record RenameComponentCommand(
        DocumentPath componentPath,
        String newName)
        implements DocumentCommand {

    private static final CommandCode INVALID_PATH =
            new CommandCode("component.path.invalid");
    private static final CommandCode NOT_FOUND =
            new CommandCode("component.not-found");
    private static final CommandCode BLANK_NAME =
            new CommandCode("component.name.blank");
    private static final CommandCode NAME_CONFLICT =
            new CommandCode("component.name.conflict");

    public RenameComponentCommand {
        Objects.requireNonNull(componentPath, "componentPath must not be null");
        Objects.requireNonNull(newName, "newName must not be null");
    }

    @Override
    public CommandResult execute(CommandContext context) {
        Objects.requireNonNull(context, "context must not be null");

        Optional<ComponentAddress> parsedAddress =
                ComponentAddress.from(componentPath);
        if (parsedAddress.isEmpty()) {
            return CommandResults.rejected(
                    INVALID_PATH,
                    "Path must identify a component",
                    componentPath);
        }
        if (newName.isBlank()) {
            return CommandResults.rejected(
                    BLANK_NAME,
                    "Component name must not be blank",
                    componentPath);
        }

        ComponentAddress address = parsedAddress.orElseThrow();
        Components components = context.document().getComponents();
        Optional<Map<String, ?>> resolvedCollection =
                ComponentCollectionResolver.resolve(components, address.section());
        if (resolvedCollection.isEmpty()
                || !resolvedCollection.orElseThrow().containsKey(address.name())) {
            return CommandResults.rejected(
                    NOT_FOUND,
                    "Component does not exist",
                    componentPath);
        }
        if (newName.equals(address.name())) {
            return CommandResults.succeeded();
        }

        Map<String, ?> collection = resolvedCollection.orElseThrow();
        DocumentPath newPath = address.withName(newName);
        if (collection.containsKey(newName)) {
            return CommandResults.rejected(
                    NAME_CONFLICT,
                    "A component with the new name already exists",
                    newPath);
        }

        List<DocumentPath> updatedReferences =
                context.referenceEditor().replaceTargetPrefix(
                        context.document(),
                        context.referenceIndex(),
                        componentPath,
                        newPath);
        ComponentCollectionResolver.renameKey(
                collection,
                address.name(),
                newName);

        ArrayList<DocumentPath> changedPaths = new ArrayList<>();
        changedPaths.add(componentPath);
        changedPaths.add(newPath);
        for (DocumentPath referencePath : updatedReferences) {
            changedPaths.add(referencePath.startsWith(componentPath)
                    ? referencePath.replacePrefix(componentPath, newPath)
                    : referencePath);
        }
        return CommandResults.succeeded(
                changedPaths.toArray(DocumentPath[]::new));
    }
}
