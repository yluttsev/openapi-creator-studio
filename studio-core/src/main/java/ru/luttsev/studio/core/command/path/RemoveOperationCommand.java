package ru.luttsev.studio.core.command.path;

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
import ru.luttsev.studio.core.model.path.HttpMethod;
import ru.luttsev.studio.core.model.path.Operation;
import ru.luttsev.studio.core.model.path.PathItem;
import ru.luttsev.studio.core.model.path.Paths;
import ru.luttsev.studio.core.navigation.DocumentPath;
import ru.luttsev.studio.core.reference.index.ResolvedReferenceUsage;

public record RemoveOperationCommand(
        String pathTemplate,
        HttpMethod method)
        implements DocumentCommand {

    private static final CommandCode PATH_NOT_FOUND =
            new CommandCode("path.not-found");
    private static final CommandCode OPERATION_NOT_FOUND =
            new CommandCode("operation.not-found");
    private static final CommandCode IS_REFERENCED =
            new CommandCode("operation.is-referenced");

    public RemoveOperationCommand {
        Objects.requireNonNull(pathTemplate, "pathTemplate must not be null");
        Objects.requireNonNull(method, "method must not be null");
    }

    @Override
    public CommandResult execute(CommandContext context) {
        Objects.requireNonNull(context, "context must not be null");

        DocumentPath path = PathCommandSupport.pathPath(pathTemplate);
        Paths paths = context.document().getPaths();
        if (paths == null || !paths.getItems().containsKey(pathTemplate)) {
            return CommandResults.rejected(
                    PATH_NOT_FOUND,
                    "Path does not exist",
                    path);
        }

        HttpMethod canonicalMethod =
                PathCommandSupport.canonicalMethod(method);
        DocumentPath operationPath =
                PathCommandSupport.operationPath(pathTemplate, canonicalMethod);
        PathItem pathItem = paths.getItems().get(pathTemplate);
        Map<HttpMethod, Operation> operations = pathItem.getOperations();
        Optional<HttpMethod> storedMethod = operations == null
                ? Optional.empty()
                : PathCommandSupport.findStoredMethod(
                        operations,
                        canonicalMethod);
        if (storedMethod.isEmpty()) {
            return CommandResults.rejected(
                    OPERATION_NOT_FOUND,
                    "Operation does not exist",
                    operationPath);
        }

        List<ResolvedReferenceUsage> usages =
                ReferenceUsageSupport.blockingUsages(
                        context.referenceIndex(),
                        operationPath);
        if (!usages.isEmpty()) {
            return CommandResults.rejected(
                    IS_REFERENCED,
                    "Operation is referenced from other document locations",
                    operationPath,
                    ReferenceUsageSupport.sourcePaths(usages));
        }

        operations.remove(storedMethod.orElseThrow());
        return CommandResults.succeeded(operationPath);
    }
}
