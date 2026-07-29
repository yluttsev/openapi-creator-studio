package ru.luttsev.studio.core.command;

import java.util.Objects;
import ru.luttsev.studio.core.command.result.CommandResult;
import ru.luttsev.studio.core.model.OpenApiDocument;
import ru.luttsev.studio.core.navigation.DocumentNavigator;
import ru.luttsev.studio.core.reference.ReferenceEditor;
import ru.luttsev.studio.core.reference.index.ReferenceIndex;
import ru.luttsev.studio.core.reference.index.ReferenceIndexBuilder;

public final class CommandExecutor {

    private final DocumentNavigator navigator;
    private final ReferenceIndexBuilder indexBuilder;
    private final ReferenceEditor referenceEditor;

    public CommandExecutor() {
        this(new DocumentNavigator());
    }

    public CommandExecutor(DocumentNavigator navigator) {
        this.navigator = Objects.requireNonNull(
                navigator,
                "navigator must not be null");
        indexBuilder = new ReferenceIndexBuilder(navigator);
        referenceEditor = new ReferenceEditor(navigator);
    }

    public CommandResult execute(
            OpenApiDocument document,
            DocumentCommand command) {
        Objects.requireNonNull(document, "document must not be null");
        Objects.requireNonNull(command, "command must not be null");

        ReferenceIndex referenceIndex = indexBuilder.build(document);
        CommandContext context = new CommandContext(
                document,
                navigator,
                referenceIndex,
                referenceEditor);
        return command.execute(context);
    }
}
