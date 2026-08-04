package ru.luttsev.studio.web.command.handler;

import org.springframework.stereotype.Component;
import ru.luttsev.studio.core.command.DocumentCommand;
import ru.luttsev.studio.core.command.path.RemovePathCommand;
import ru.luttsev.studio.generated.model.RemovePathCommandRequest;
import ru.luttsev.studio.web.command.DocumentCommandRequestHandler;

@Component
public final class RemovePathCommandHandler
        implements DocumentCommandRequestHandler<RemovePathCommandRequest> {

    @Override
    public Class<RemovePathCommandRequest> requestType() {
        return RemovePathCommandRequest.class;
    }

    @Override
    public DocumentCommand createCommand(RemovePathCommandRequest request) {
        return new RemovePathCommand(request.getPayload().getPathTemplate());
    }
}
