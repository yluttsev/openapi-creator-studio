package ru.luttsev.studio.web.command.handler;

import org.springframework.stereotype.Component;
import ru.luttsev.studio.core.command.DocumentCommand;
import ru.luttsev.studio.core.command.schema.AddSchemaCommand;
import ru.luttsev.studio.generated.model.AddSchemaCommandRequest;
import ru.luttsev.studio.web.command.DocumentCommandRequestHandler;

@Component
public final class AddSchemaCommandHandler
        implements DocumentCommandRequestHandler<AddSchemaCommandRequest> {

    @Override
    public Class<AddSchemaCommandRequest> requestType() {
        return AddSchemaCommandRequest.class;
    }

    @Override
    public DocumentCommand createCommand(AddSchemaCommandRequest request) {
        return new AddSchemaCommand(request.getPayload().getName());
    }
}
