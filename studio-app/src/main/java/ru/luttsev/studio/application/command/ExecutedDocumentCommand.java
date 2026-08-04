package ru.luttsev.studio.application.command;

import java.util.List;
import ru.luttsev.studio.core.navigation.DocumentPath;

public record ExecutedDocumentCommand(
        long revision,
        List<DocumentPath> changedPaths) {

    public ExecutedDocumentCommand {
        changedPaths = List.copyOf(changedPaths);
    }
}
