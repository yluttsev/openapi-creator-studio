package ru.luttsev.studio.core.validation;

import java.util.Set;
import java.util.stream.Collectors;
import ru.luttsev.studio.core.document.NewDocumentParameters;
import ru.luttsev.studio.core.document.OpenApiDocumentFactory;
import ru.luttsev.studio.core.model.OpenApiDocument;
import ru.luttsev.studio.core.model.OpenApiVersion;
import ru.luttsev.studio.core.model.path.HttpMethod;
import ru.luttsev.studio.core.model.path.Operation;
import ru.luttsev.studio.core.model.path.PathItem;
import ru.luttsev.studio.core.model.reference.InlineObject;

final class ValidationFixture {

    private ValidationFixture() {
    }

    static OpenApiDocument document() {
        return new OpenApiDocumentFactory().create(new NewDocumentParameters(
                OpenApiVersion.V3_1_2,
                "Users API",
                "1.0.0"));
    }

    static Operation addOperation(
            OpenApiDocument document,
            String path,
            HttpMethod method,
            String operationId) {
        PathItem pathItem = document.getPaths()
                .getItems()
                .computeIfAbsent(path, ignored -> new PathItem());
        Operation operation = new Operation();
        operation.setOperationId(operationId);
        pathItem.getOperations().put(method, operation);
        return operation;
    }

    static <T> InlineObject<T> inline(T value) {
        return new InlineObject<>(value);
    }

    static Set<String> codes(ValidationResult result) {
        return result.issues().stream()
                .map(issue -> issue.code().value())
                .collect(Collectors.toSet());
    }
}
