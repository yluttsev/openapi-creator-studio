package ru.luttsev.studio.core.validation.support;

import java.util.List;
import java.util.Objects;
import ru.luttsev.studio.core.model.path.Operation;
import ru.luttsev.studio.core.model.path.PathItem;
import ru.luttsev.studio.core.navigation.DocumentPath;
import ru.luttsev.studio.core.validation.ValidationContext;

public record OperationOccurrence(
        Operation operation,
        DocumentPath path,
        PathItem pathItem,
        DocumentPath pathItemPath) {

    public OperationOccurrence {
        Objects.requireNonNull(operation, "operation must not be null");
        Objects.requireNonNull(path, "path must not be null");
        Objects.requireNonNull(pathItem, "pathItem must not be null");
        Objects.requireNonNull(pathItemPath, "pathItemPath must not be null");
    }

    public List<ParameterOccurrence> effectiveParameters(
            ValidationContext context) {
        return ParameterSupport.effective(
                context,
                pathItem,
                pathItemPath,
                operation,
                path);
    }
}
