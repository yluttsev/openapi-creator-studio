package ru.luttsev.studio.core.validation.support;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import ru.luttsev.studio.core.model.path.Operation;
import ru.luttsev.studio.core.model.path.PathItem;
import ru.luttsev.studio.core.navigation.DocumentEntry;
import ru.luttsev.studio.core.validation.ValidationContext;

public final class OperationIndex {

    private final List<OperationOccurrence> operations;
    private final Map<String, List<OperationOccurrence>> byOperationId;

    public OperationIndex(ValidationContext context) {
        ArrayList<OperationOccurrence> allOperations = new ArrayList<>();
        Map<String, List<OperationOccurrence>> operationsById =
                new LinkedHashMap<>();
        for (DocumentEntry entry : DocumentEntries.ofType(context, Operation.class)) {
            Optional<ResolvedValue<PathItem>> pathItem =
                    DocumentAncestors.findNearest(
                            context,
                            entry.path(),
                            PathItem.class);
            if (pathItem.isEmpty()) {
                continue;
            }

            Operation operation = (Operation) entry.value();
            ResolvedValue<PathItem> owner = pathItem.orElseThrow();
            OperationOccurrence occurrence = new OperationOccurrence(
                    operation,
                    entry.path(),
                    owner.value(),
                    owner.path());
            allOperations.add(occurrence);
            if (operation.getOperationId() != null) {
                operationsById
                        .computeIfAbsent(
                                operation.getOperationId(),
                                ignored -> new ArrayList<>())
                        .add(occurrence);
            }
        }
        operations = List.copyOf(allOperations);
        LinkedHashMap<String, List<OperationOccurrence>> immutableById =
                new LinkedHashMap<>();
        for (Map.Entry<String, List<OperationOccurrence>> entry :
                operationsById.entrySet()) {
            immutableById.put(entry.getKey(), List.copyOf(entry.getValue()));
        }
        byOperationId = Map.copyOf(immutableById);
    }

    public List<OperationOccurrence> all() {
        return operations;
    }

    public List<OperationOccurrence> findByOperationId(String operationId) {
        return byOperationId.getOrDefault(operationId, List.of());
    }

    public Optional<OperationOccurrence> findByPath(
            ru.luttsev.studio.core.navigation.DocumentPath path) {
        return operations.stream()
                .filter(operation -> operation.path().equals(path))
                .findFirst();
    }
}
