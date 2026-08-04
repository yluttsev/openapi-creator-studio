package ru.luttsev.studio.web.document;

import java.util.List;
import java.util.Optional;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import ru.luttsev.studio.core.navigation.DocumentPath;
import ru.luttsev.studio.generated.model.Diagnostic;
import ru.luttsev.studio.generated.model.SourcePosition;
import ru.luttsev.studio.openapi.diagnostic.DiagnosticCode;
import ru.luttsev.studio.openapi.diagnostic.OpenApiDiagnostic;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface DiagnosticMapper {

    List<Diagnostic> map(List<OpenApiDiagnostic> diagnostics);

    @Mapping(target = "sourcePosition", source = "sourcePosition")
    Diagnostic map(OpenApiDiagnostic diagnostic);

    default String map(DiagnosticCode code) {
        return code.value();
    }

    default String map(DocumentPath path) {
        return path.toPointer();
    }

    default SourcePosition map(
            Optional<ru.luttsev.studio.openapi.diagnostic.SourcePosition>
                    sourcePosition) {
        return sourcePosition
                .map(position -> new SourcePosition(
                        position.line(),
                        position.column()))
                .orElse(null);
    }
}
