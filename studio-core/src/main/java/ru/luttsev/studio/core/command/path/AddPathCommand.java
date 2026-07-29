package ru.luttsev.studio.core.command.path;

import java.util.Objects;
import ru.luttsev.studio.core.command.CommandContext;
import ru.luttsev.studio.core.command.CommandResults;
import ru.luttsev.studio.core.command.DocumentCommand;
import ru.luttsev.studio.core.command.result.CommandCode;
import ru.luttsev.studio.core.command.result.CommandResult;
import ru.luttsev.studio.core.model.path.PathItem;
import ru.luttsev.studio.core.model.path.Paths;
import ru.luttsev.studio.core.navigation.DocumentPath;

public record AddPathCommand(String pathTemplate)
        implements DocumentCommand {

    private static final CommandCode BLANK_TEMPLATE =
            new CommandCode("path.template.blank");
    private static final CommandCode ALREADY_EXISTS =
            new CommandCode("path.already-exists");

    public AddPathCommand {
        Objects.requireNonNull(pathTemplate, "pathTemplate must not be null");
    }

    @Override
    public CommandResult execute(CommandContext context) {
        Objects.requireNonNull(context, "context must not be null");

        DocumentPath path = PathCommandSupport.pathPath(pathTemplate);
        if (pathTemplate.isBlank()) {
            return CommandResults.rejected(
                    BLANK_TEMPLATE,
                    "Path template must not be blank",
                    path);
        }

        Paths paths = context.document().getPaths();
        if (paths != null && paths.getItems().containsKey(pathTemplate)) {
            return CommandResults.rejected(
                    ALREADY_EXISTS,
                    "Path already exists",
                    path);
        }

        if (paths == null) {
            paths = new Paths();
            context.document().setPaths(paths);
        }
        paths.getItems().put(pathTemplate, new PathItem());
        return CommandResults.succeeded(path);
    }
}
