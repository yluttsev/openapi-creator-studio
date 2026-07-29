package ru.luttsev.studio.core.command.result;

import java.util.List;
import java.util.Objects;
import ru.luttsev.studio.core.navigation.DocumentPath;

public record CommandSucceeded(List<DocumentPath> changedPaths)
        implements CommandResult {

    public CommandSucceeded {
        Objects.requireNonNull(changedPaths, "changedPaths must not be null");
        changedPaths = List.copyOf(changedPaths);
    }
}
