package ru.luttsev.studio.core.command.component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import ru.luttsev.studio.core.command.CommandContext;
import ru.luttsev.studio.core.command.CommandResults;
import ru.luttsev.studio.core.command.DocumentCommand;
import ru.luttsev.studio.core.command.result.CommandCode;
import ru.luttsev.studio.core.command.result.CommandResult;
import ru.luttsev.studio.core.command.support.ReferenceUsageSupport;
import ru.luttsev.studio.core.model.Components;
import ru.luttsev.studio.core.navigation.DocumentPath;
import ru.luttsev.studio.core.reference.index.ResolvedReferenceUsage;

public record DeleteComponentCommand(DocumentPath componentPath)
        implements DocumentCommand {

    private static final CommandCode INVALID_PATH =
            new CommandCode("component.path.invalid");
    private static final CommandCode NOT_FOUND =
            new CommandCode("component.not-found");
    private static final CommandCode IS_REFERENCED =
            new CommandCode("component.is-referenced");

    public DeleteComponentCommand {
        Objects.requireNonNull(componentPath, "componentPath must not be null");
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

        List<ResolvedReferenceUsage> usages =
                ReferenceUsageSupport.blockingUsages(
                        context.referenceIndex(),
                        componentPath);
        if (!usages.isEmpty()) {
            return CommandResults.rejected(
                    IS_REFERENCED,
                    "Component is referenced from other document locations",
                    componentPath,
                    ReferenceUsageSupport.sourcePaths(usages));
        }

        resolvedCollection.orElseThrow().remove(address.name());
        return CommandResults.succeeded(componentPath);
    }
}
