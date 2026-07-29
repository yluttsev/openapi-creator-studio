package ru.luttsev.studio.core.command.path;

import java.util.List;
import java.util.Objects;
import ru.luttsev.studio.core.command.CommandContext;
import ru.luttsev.studio.core.command.CommandResults;
import ru.luttsev.studio.core.command.DocumentCommand;
import ru.luttsev.studio.core.command.result.CommandCode;
import ru.luttsev.studio.core.command.result.CommandResult;
import ru.luttsev.studio.core.command.support.ReferenceUsageSupport;
import ru.luttsev.studio.core.model.path.Paths;
import ru.luttsev.studio.core.navigation.DocumentPath;
import ru.luttsev.studio.core.reference.index.ResolvedReferenceUsage;

public record RemovePathCommand(String pathTemplate)
        implements DocumentCommand {

    private static final CommandCode NOT_FOUND =
            new CommandCode("path.not-found");
    private static final CommandCode IS_REFERENCED =
            new CommandCode("path.is-referenced");

    public RemovePathCommand {
        Objects.requireNonNull(pathTemplate, "pathTemplate must not be null");
    }

    @Override
    public CommandResult execute(CommandContext context) {
        Objects.requireNonNull(context, "context must not be null");

        DocumentPath path = PathCommandSupport.pathPath(pathTemplate);
        Paths paths = context.document().getPaths();
        if (paths == null || !paths.getItems().containsKey(pathTemplate)) {
            return CommandResults.rejected(
                    NOT_FOUND,
                    "Path does not exist",
                    path);
        }

        List<ResolvedReferenceUsage> usages =
                ReferenceUsageSupport.blockingUsages(
                        context.referenceIndex(),
                        path);
        if (!usages.isEmpty()) {
            return CommandResults.rejected(
                    IS_REFERENCED,
                    "Path is referenced from other document locations",
                    path,
                    ReferenceUsageSupport.sourcePaths(usages));
        }

        paths.getItems().remove(pathTemplate);
        return CommandResults.succeeded(path);
    }
}
