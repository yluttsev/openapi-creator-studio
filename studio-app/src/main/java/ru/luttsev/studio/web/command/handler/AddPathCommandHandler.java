package ru.luttsev.studio.web.command.handler;

import org.springframework.stereotype.Component;
import ru.luttsev.studio.core.command.DocumentCommand;
import ru.luttsev.studio.core.command.path.AddPathCommand;
import ru.luttsev.studio.generated.model.AddPathCommandRequest;
import ru.luttsev.studio.web.command.DocumentCommandRequestHandler;

@Component
public final class AddPathCommandHandler
        implements DocumentCommandRequestHandler<AddPathCommandRequest> {

    @Override
    public Class<AddPathCommandRequest> requestType() {
        return AddPathCommandRequest.class;
    }

    @Override
    public DocumentCommand createCommand(AddPathCommandRequest request) {
        return new AddPathCommand(request.getPayload().getPathTemplate());
    }
}
