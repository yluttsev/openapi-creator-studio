package ru.luttsev.studio.web.command;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ru.luttsev.studio.core.command.DocumentCommand;
import ru.luttsev.studio.core.command.component.DeleteComponentCommand;
import ru.luttsev.studio.core.command.component.RenameComponentCommand;
import ru.luttsev.studio.core.command.path.AddOperationCommand;
import ru.luttsev.studio.core.command.path.AddPathCommand;
import ru.luttsev.studio.core.command.path.RemoveOperationCommand;
import ru.luttsev.studio.core.command.path.RemovePathCommand;
import ru.luttsev.studio.core.command.schema.AddSchemaCommand;
import ru.luttsev.studio.core.command.schema.AddSchemaPropertyCommand;
import ru.luttsev.studio.core.command.schema.RemoveSchemaPropertyCommand;
import ru.luttsev.studio.generated.model.AddOperationCommandRequest;
import ru.luttsev.studio.generated.model.AddPathCommandRequest;
import ru.luttsev.studio.generated.model.AddSchemaCommandPayload;
import ru.luttsev.studio.generated.model.AddSchemaCommandRequest;
import ru.luttsev.studio.generated.model.AddSchemaPropertyCommandRequest;
import ru.luttsev.studio.generated.model.CommandType;
import ru.luttsev.studio.generated.model.DeleteComponentCommandPayload;
import ru.luttsev.studio.generated.model.DeleteComponentCommandRequest;
import ru.luttsev.studio.generated.model.DocumentCommandRequest;
import ru.luttsev.studio.generated.model.HttpMethod;
import ru.luttsev.studio.generated.model.OperationCommandPayload;
import ru.luttsev.studio.generated.model.PathCommandPayload;
import ru.luttsev.studio.generated.model.RemoveOperationCommandRequest;
import ru.luttsev.studio.generated.model.RemovePathCommandRequest;
import ru.luttsev.studio.generated.model.RemoveSchemaPropertyCommandRequest;
import ru.luttsev.studio.generated.model.RenameComponentCommandPayload;
import ru.luttsev.studio.generated.model.RenameComponentCommandRequest;
import ru.luttsev.studio.generated.model.SchemaPropertyCommandPayload;
import ru.luttsev.studio.web.command.handler.AddOperationCommandHandler;
import ru.luttsev.studio.web.command.handler.AddPathCommandHandler;
import ru.luttsev.studio.web.command.handler.AddSchemaCommandHandler;
import ru.luttsev.studio.web.command.handler.AddSchemaPropertyCommandHandler;
import ru.luttsev.studio.web.command.handler.DeleteComponentCommandHandler;
import ru.luttsev.studio.web.command.handler.RemoveOperationCommandHandler;
import ru.luttsev.studio.web.command.handler.RemovePathCommandHandler;
import ru.luttsev.studio.web.command.handler.RemoveSchemaPropertyCommandHandler;
import ru.luttsev.studio.web.command.handler.RenameComponentCommandHandler;

class DocumentCommandDispatcherTest {

    private final DocumentCommandDispatcher dispatcher =
            new DocumentCommandDispatcher(List.of(
                    new AddPathCommandHandler(),
                    new RemovePathCommandHandler(),
                    new AddOperationCommandHandler(),
                    new RemoveOperationCommandHandler(),
                    new AddSchemaCommandHandler(),
                    new AddSchemaPropertyCommandHandler(),
                    new RemoveSchemaPropertyCommandHandler(),
                    new RenameComponentCommandHandler(),
                    new DeleteComponentCommandHandler()));

    @ParameterizedTest
    @MethodSource("requests")
    void dispatchesEveryRestCommand(
            DocumentCommandRequest request,
            Class<? extends DocumentCommand> commandType) {
        assertThat(dispatcher.dispatch(request)).isInstanceOf(commandType);
    }

    private static Stream<Arguments> requests() {
        PathCommandPayload path = new PathCommandPayload("/users");
        OperationCommandPayload operation = new OperationCommandPayload(
                "/users",
                HttpMethod.GET);
        SchemaPropertyCommandPayload property =
                new SchemaPropertyCommandPayload(
                        "/components/schemas/User",
                        "id");
        return Stream.of(
                Arguments.of(
                        new AddPathCommandRequest(CommandType.ADD_PATH, path),
                        AddPathCommand.class),
                Arguments.of(
                        new RemovePathCommandRequest(CommandType.REMOVE_PATH, path),
                        RemovePathCommand.class),
                Arguments.of(
                        new AddOperationCommandRequest(
                                CommandType.ADD_OPERATION,
                                operation),
                        AddOperationCommand.class),
                Arguments.of(
                        new RemoveOperationCommandRequest(
                                CommandType.REMOVE_OPERATION,
                                operation),
                        RemoveOperationCommand.class),
                Arguments.of(
                        new AddSchemaCommandRequest(
                                CommandType.ADD_SCHEMA,
                                new AddSchemaCommandPayload("User")),
                        AddSchemaCommand.class),
                Arguments.of(
                        new AddSchemaPropertyCommandRequest(
                                CommandType.ADD_SCHEMA_PROPERTY,
                                property),
                        AddSchemaPropertyCommand.class),
                Arguments.of(
                        new RemoveSchemaPropertyCommandRequest(
                                CommandType.REMOVE_SCHEMA_PROPERTY,
                                property),
                        RemoveSchemaPropertyCommand.class),
                Arguments.of(
                        new RenameComponentCommandRequest(
                                CommandType.RENAME_COMPONENT,
                                new RenameComponentCommandPayload(
                                        "/components/schemas/User",
                                        "Customer")),
                        RenameComponentCommand.class),
                Arguments.of(
                        new DeleteComponentCommandRequest(
                                CommandType.DELETE_COMPONENT,
                                new DeleteComponentCommandPayload(
                                        "/components/schemas/User")),
                        DeleteComponentCommand.class));
    }
}
