package ru.luttsev.studio.web.validation;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import ru.luttsev.studio.application.validation.DocumentValidation;
import ru.luttsev.studio.core.navigation.DocumentPath;
import ru.luttsev.studio.core.validation.ValidationCode;
import ru.luttsev.studio.core.validation.ValidationIssue;
import ru.luttsev.studio.generated.model.Diagnostic;
import ru.luttsev.studio.generated.model.ValidationResponse;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ValidationResponseMapper {

    @Mapping(target = "diagnostics", source = "issues")
    ValidationResponse map(DocumentValidation validation);

    @Mapping(target = "phase", constant = "SEMANTIC_VALIDATION")
    @Mapping(target = "sourcePosition", ignore = true)
    Diagnostic map(ValidationIssue issue);

    default String map(ValidationCode code) {
        return code.value();
    }

    default String map(DocumentPath path) {
        return path.toPointer();
    }
}
