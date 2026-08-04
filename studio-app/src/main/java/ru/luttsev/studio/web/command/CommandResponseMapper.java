package ru.luttsev.studio.web.command;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import ru.luttsev.studio.application.command.ExecutedDocumentCommand;
import ru.luttsev.studio.core.command.result.CommandCode;
import ru.luttsev.studio.core.navigation.DocumentPath;
import ru.luttsev.studio.generated.model.CommandIssue;
import ru.luttsev.studio.generated.model.CommandResponse;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CommandResponseMapper {

    CommandResponse map(ExecutedDocumentCommand execution);

    List<CommandIssue> map(
            List<ru.luttsev.studio.core.command.result.CommandIssue> issues);

    CommandIssue map(
            ru.luttsev.studio.core.command.result.CommandIssue issue);

    default String map(DocumentPath path) {
        return path.toPointer();
    }

    default String map(CommandCode code) {
        return code.value();
    }
}
