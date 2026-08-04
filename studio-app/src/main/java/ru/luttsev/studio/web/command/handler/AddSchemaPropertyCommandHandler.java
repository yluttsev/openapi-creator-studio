package ru.luttsev.studio.web.command.handler;

import org.springframework.stereotype.Component;
import ru.luttsev.studio.core.command.DocumentCommand;
import ru.luttsev.studio.core.command.schema.AddSchemaPropertyCommand;
import ru.luttsev.studio.core.navigation.DocumentPath;
import ru.luttsev.studio.generated.model.AddSchemaPropertyCommandRequest;
import ru.luttsev.studio.web.command.DocumentCommandRequestHandler;

@Component
public final class AddSchemaPropertyCommandHandler
        implements DocumentCommandRequestHandler<AddSchemaPropertyCommandRequest> {

    @Override
    public Class<AddSchemaPropertyCommandRequest> requestType() {
        return AddSchemaPropertyCommandRequest.class;
    }

    @Override
    public DocumentCommand createCommand(
            AddSchemaPropertyCommandRequest request) {
        return new AddSchemaPropertyCommand(
                DocumentPath.parse(request.getPayload().getSchemaPath()),
                request.getPayload().getPropertyName());
    }
}
