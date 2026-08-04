package ru.luttsev.studio.web.command;

import ru.luttsev.studio.core.command.DocumentCommand;
import ru.luttsev.studio.generated.model.DocumentCommandRequest;

public interface DocumentCommandRequestHandler<R extends DocumentCommandRequest> {

    Class<R> requestType();

    DocumentCommand createCommand(R request);
}
