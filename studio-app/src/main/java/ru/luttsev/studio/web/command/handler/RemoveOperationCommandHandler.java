package ru.luttsev.studio.web.command.handler;

import org.springframework.stereotype.Component;
import ru.luttsev.studio.core.command.DocumentCommand;
import ru.luttsev.studio.core.command.path.RemoveOperationCommand;
import ru.luttsev.studio.core.model.path.HttpMethod;
import ru.luttsev.studio.generated.model.RemoveOperationCommandRequest;
import ru.luttsev.studio.web.command.DocumentCommandRequestHandler;

@Component
public final class RemoveOperationCommandHandler
        implements DocumentCommandRequestHandler<RemoveOperationCommandRequest> {

    @Override
    public Class<RemoveOperationCommandRequest> requestType() {
        return RemoveOperationCommandRequest.class;
    }

    @Override
    public DocumentCommand createCommand(RemoveOperationCommandRequest request) {
        return new RemoveOperationCommand(
                request.getPayload().getPathTemplate(),
                new HttpMethod(request.getPayload().getMethod().getValue()));
    }
}
