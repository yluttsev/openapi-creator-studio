package ru.luttsev.studio.core.validation.rule;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import ru.luttsev.studio.core.model.path.Operation;
import ru.luttsev.studio.core.navigation.DocumentEntry;
import ru.luttsev.studio.core.validation.ValidationCode;
import ru.luttsev.studio.core.validation.ValidationContext;
import ru.luttsev.studio.core.validation.ValidationIssue;
import ru.luttsev.studio.core.validation.ValidationRule;
import ru.luttsev.studio.core.validation.ValidationSeverity;
import ru.luttsev.studio.core.validation.support.DocumentEntries;

public final class OperationIdUniquenessRule implements ValidationRule {

    private static final ValidationCode DUPLICATE_OPERATION_ID =
            new ValidationCode("operation.operation-id.duplicate");

    @Override
    public List<ValidationIssue> validate(ValidationContext context) {
        Map<String, List<DocumentEntry>> byOperationId = new LinkedHashMap<>();
        for (DocumentEntry entry : DocumentEntries.ofType(context, Operation.class)) {
            Operation operation = (Operation) entry.value();
            if (operation.getOperationId() != null) {
                byOperationId
                        .computeIfAbsent(operation.getOperationId(), ignored -> new ArrayList<>())
                        .add(entry);
            }
        }

        ArrayList<ValidationIssue> issues = new ArrayList<>();
        for (Map.Entry<String, List<DocumentEntry>> group : byOperationId.entrySet()) {
            if (group.getValue().size() < 2) {
                continue;
            }
            for (DocumentEntry entry : group.getValue()) {
                issues.add(new ValidationIssue(
                        DUPLICATE_OPERATION_ID,
                        ValidationSeverity.ERROR,
                        "Operation ID must be unique: " + group.getKey(),
                        entry.path().child("operationId")));
            }
        }
        return List.copyOf(issues);
    }
}
