package ru.luttsev.studio.web.command.handler;

import org.springframework.stereotype.Component;
import ru.luttsev.studio.core.command.DocumentCommand;
import ru.luttsev.studio.core.command.path.AddOperationCommand;
import ru.luttsev.studio.core.model.path.HttpMethod;
import ru.luttsev.studio.generated.model.AddOperationCommandRequest;
import ru.luttsev.studio.web.command.DocumentCommandRequestHandler;

@Component
public final class AddOperationCommandHandler
        implements DocumentCommandRequestHandler<AddOperationCommandRequest> {

    @Override
    public Class<AddOperationCommandRequest> requestType() {
        return AddOperationCommandRequest.class;
    }

    @Override
    public DocumentCommand createCommand(AddOperationCommandRequest request) {
        return new AddOperationCommand(
                request.getPayload().getPathTemplate(),
                new HttpMethod(request.getPayload().getMethod().getValue()));
    }
}
