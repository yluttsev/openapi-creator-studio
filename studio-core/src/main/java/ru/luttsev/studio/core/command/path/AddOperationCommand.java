package ru.luttsev.studio.core.command.path;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import ru.luttsev.studio.core.command.CommandContext;
import ru.luttsev.studio.core.command.CommandResults;
import ru.luttsev.studio.core.command.DocumentCommand;
import ru.luttsev.studio.core.command.result.CommandCode;
import ru.luttsev.studio.core.command.result.CommandResult;
import ru.luttsev.studio.core.model.path.HttpMethod;
import ru.luttsev.studio.core.model.path.Operation;
import ru.luttsev.studio.core.model.path.PathItem;
import ru.luttsev.studio.core.model.path.Paths;
import ru.luttsev.studio.core.navigation.DocumentPath;

public record AddOperationCommand(
        String pathTemplate,
        HttpMethod method)
        implements DocumentCommand {

    private static final CommandCode PATH_NOT_FOUND =
            new CommandCode("path.not-found");
    private static final CommandCode BLANK_METHOD =
            new CommandCode("operation.method.blank");
    private static final CommandCode ALREADY_EXISTS =
            new CommandCode("operation.already-exists");

    public AddOperationCommand {
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
        if (method.value().isBlank()) {
            return CommandResults.rejected(
                    BLANK_METHOD,
                    "Operation method must not be blank",
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
        if (storedMethod.isPresent()) {
            return CommandResults.rejected(
                    ALREADY_EXISTS,
                    "Operation already exists",
                    operationPath);
        }

        if (operations == null) {
            operations = new LinkedHashMap<>();
            pathItem.setOperations(operations);
        }
        operations.put(canonicalMethod, new Operation());
        return CommandResults.succeeded(operationPath);
    }
}
