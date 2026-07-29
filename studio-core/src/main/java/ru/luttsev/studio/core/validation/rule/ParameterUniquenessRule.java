package ru.luttsev.studio.core.validation.rule;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import ru.luttsev.studio.core.model.parameter.Parameter;
import ru.luttsev.studio.core.model.path.Operation;
import ru.luttsev.studio.core.model.path.PathItem;
import ru.luttsev.studio.core.model.reference.ReferenceOr;
import ru.luttsev.studio.core.navigation.DocumentEntry;
import ru.luttsev.studio.core.navigation.DocumentPath;
import ru.luttsev.studio.core.validation.ValidationCode;
import ru.luttsev.studio.core.validation.ValidationContext;
import ru.luttsev.studio.core.validation.ValidationIssue;
import ru.luttsev.studio.core.validation.ValidationRule;
import ru.luttsev.studio.core.validation.ValidationSeverity;
import ru.luttsev.studio.core.validation.support.DocumentEntries;
import ru.luttsev.studio.core.validation.support.ParameterKey;
import ru.luttsev.studio.core.validation.support.ParameterOccurrence;
import ru.luttsev.studio.core.validation.support.ParameterSupport;

public final class ParameterUniquenessRule implements ValidationRule {

    private static final ValidationCode DUPLICATE_PARAMETER =
            new ValidationCode("parameter.name-and-location.duplicate");

    @Override
    public List<ValidationIssue> validate(ValidationContext context) {
        ArrayList<ValidationIssue> issues = new ArrayList<>();
        for (DocumentEntry entry : DocumentEntries.ofType(context, PathItem.class)) {
            PathItem pathItem = (PathItem) entry.value();
            issues.addAll(validateList(
                    context,
                    pathItem.getParameters(),
                    entry.path().child("parameters")));
        }
        for (DocumentEntry entry : DocumentEntries.ofType(context, Operation.class)) {
            Operation operation = (Operation) entry.value();
            issues.addAll(validateList(
                    context,
                    operation.getParameters(),
                    entry.path().child("parameters")));
        }
        return List.copyOf(issues);
    }

    private static List<ValidationIssue> validateList(
            ValidationContext context,
            List<ReferenceOr<Parameter>> parameters,
            DocumentPath parametersPath) {
        Map<ParameterKey, List<ParameterOccurrence>> byKey = new LinkedHashMap<>();
        for (ParameterOccurrence occurrence :
                ParameterSupport.resolve(context, parameters, parametersPath)) {
            ParameterKey key = occurrence.key();
            if (key.name() != null && key.location() != null) {
                byKey.computeIfAbsent(key, ignored -> new ArrayList<>())
                        .add(occurrence);
            }
        }

        ArrayList<ValidationIssue> issues = new ArrayList<>();
        for (Map.Entry<ParameterKey, List<ParameterOccurrence>> group : byKey.entrySet()) {
            if (group.getValue().size() < 2) {
                continue;
            }
            for (ParameterOccurrence occurrence : group.getValue()) {
                issues.add(new ValidationIssue(
                        DUPLICATE_PARAMETER,
                        ValidationSeverity.ERROR,
                        "Parameter must be unique by name and location: "
                                + group.getKey().name()
                                + " in "
                                + group.getKey().location().name().toLowerCase(),
                        occurrence.usagePath()));
            }
        }
        return List.copyOf(issues);
    }
}
