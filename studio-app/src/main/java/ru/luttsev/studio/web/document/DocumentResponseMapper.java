package ru.luttsev.studio.web.document;

import java.util.Map;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import ru.luttsev.studio.application.document.OpenedDocument;
import ru.luttsev.studio.application.workspace.DocumentSession;
import ru.luttsev.studio.generated.model.DocumentResponse;
import ru.luttsev.studio.generated.model.OpenDocumentResponse;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        uses = DiagnosticMapper.class)
public interface DocumentResponseMapper {

    @Mapping(target = "id", source = "session.id")
    @Mapping(target = "revision", source = "session.revision")
    @Mapping(target = "title", source = "session.document.info.title")
    @Mapping(
            target = "openApiVersion",
            source = "session.document.openApiVersion.value")
    @Mapping(target = "apiVersion", source = "session.document.info.version")
    OpenDocumentResponse toOpenDocumentResponse(OpenedDocument source);

    @Mapping(target = "id", source = "session.id")
    @Mapping(target = "revision", source = "session.revision")
    @Mapping(target = "document", source = "document")
    DocumentResponse toDocumentResponse(
            DocumentSession session,
            Map<String, Object> document);
}
