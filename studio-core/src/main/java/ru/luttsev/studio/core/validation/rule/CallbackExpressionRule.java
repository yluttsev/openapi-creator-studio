package ru.luttsev.studio.core.validation.rule;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import ru.luttsev.studio.core.model.callback.Callback;
import ru.luttsev.studio.core.model.parameter.Parameter;
import ru.luttsev.studio.core.model.reference.InlineObject;
import ru.luttsev.studio.core.model.reference.ReferenceObject;
import ru.luttsev.studio.core.model.reference.ReferenceOr;
import ru.luttsev.studio.core.navigation.DocumentEntry;
import ru.luttsev.studio.core.navigation.DocumentPath;
import ru.luttsev.studio.core.validation.ValidationCode;
import ru.luttsev.studio.core.validation.ValidationContext;
import ru.luttsev.studio.core.validation.ValidationIssue;
import ru.luttsev.studio.core.validation.ValidationRule;
import ru.luttsev.studio.core.validation.ValidationSeverity;
import ru.luttsev.studio.core.validation.support.DocumentEntries;
import ru.luttsev.studio.core.validation.support.OperationIndex;
import ru.luttsev.studio.core.validation.support.OperationOccurrence;
import ru.luttsev.studio.core.validation.support.ParameterOccurrence;
import ru.luttsev.studio.core.validation.support.ReferenceValueResolver;
import ru.luttsev.studio.core.validation.support.ResolvedValue;
import ru.luttsev.studio.core.validation.support.RuntimeExpressionSupport;
import ru.luttsev.studio.core.validation.support.RuntimeParameterReference;

public final class CallbackExpressionRule implements ValidationRule {

    private static final ValidationCode INVALID_EXPRESSION =
            new ValidationCode("callback.expression.invalid");
    private static final ValidationCode UNKNOWN_PARAMETER =
            new ValidationCode("callback.expression.parameter.unknown");

    @Override
    public List<ValidationIssue> validate(ValidationContext context) {
        ArrayList<ValidationIssue> issues = new ArrayList<>();
        validateSyntax(context, issues);
        validateParameterReferences(context, issues);
        return List.copyOf(issues);
    }

    private static void validateSyntax(
            ValidationContext context,
            List<ValidationIssue> issues) {
        for (DocumentEntry entry : DocumentEntries.ofType(context, Callback.class)) {
            Callback callback = (Callback) entry.value();
            for (String expression : callback.getExpressions().keySet()) {
                if (!RuntimeExpressionSupport.isValidCallbackKey(expression)) {
                    issues.add(new ValidationIssue(
                            INVALID_EXPRESSION,
                            ValidationSeverity.ERROR,
                            "Callback key is not a valid runtime expression: "
                                    + expression,
                            entry.path().child(expression)));
                }
            }
        }
    }

    private static void validateParameterReferences(
            ValidationContext context,
            List<ValidationIssue> issues) {
        OperationIndex operationIndex = new OperationIndex(context);
        ReferenceValueResolver resolver = new ReferenceValueResolver(context);
        for (OperationOccurrence operation : operationIndex.all()) {
            for (Map.Entry<String, ReferenceOr<Callback>> callbackEntry :
                    operation.operation().getCallbacks().entrySet()) {
                DocumentPath usagePath = operation.path()
                        .child("callbacks")
                        .child(callbackEntry.getKey());
                Optional<ResolvedValue<Callback>> callback =
                        resolveCallback(resolver, callbackEntry.getValue(), usagePath);
                callback.ifPresent(resolved -> validateCallbackParameters(
                        context,
                        operation,
                        resolved,
                        issues));
            }
        }
    }

    private static Optional<ResolvedValue<Callback>> resolveCallback(
            ReferenceValueResolver resolver,
            ReferenceOr<Callback> referenceOr,
            DocumentPath usagePath) {
        if (referenceOr instanceof InlineObject<Callback> inlineObject) {
            return Optional.of(new ResolvedValue<>(
                    inlineObject.value(),
                    usagePath));
        }
        ReferenceObject<Callback> reference =
                (ReferenceObject<Callback>) referenceOr;
        return reference.getRef() == null
                ? Optional.empty()
                : resolver.resolveWithPath(reference.getRef(), Callback.class);
    }

    private static void validateCallbackParameters(
            ValidationContext context,
            OperationOccurrence operation,
            ResolvedValue<Callback> callback,
            List<ValidationIssue> issues) {
        List<ParameterOccurrence> parameters =
                operation.effectiveParameters(context);
        for (String callbackKey : callback.value().getExpressions().keySet()) {
            if (!RuntimeExpressionSupport.isValidCallbackKey(callbackKey)) {
                continue;
            }
            String expression = callbackKey.substring(1, callbackKey.length() - 1);
            Optional<RuntimeParameterReference> requestParameter =
                    RuntimeExpressionSupport.requestParameter(expression);
            if (requestParameter.isPresent()
                    && !contains(parameters, requestParameter.orElseThrow())) {
                issues.add(new ValidationIssue(
                        UNKNOWN_PARAMETER,
                        ValidationSeverity.ERROR,
                        "Callback expression references an undeclared request parameter: "
                                + requestParameter.orElseThrow().name(),
                        callback.path().child(callbackKey)));
            }
        }
    }

    private static boolean contains(
            List<ParameterOccurrence> parameters,
            RuntimeParameterReference reference) {
        for (ParameterOccurrence occurrence : parameters) {
            Parameter parameter = occurrence.parameter();
            if (reference.location() == parameter.getLocation()
                    && reference.name().equals(parameter.getName())) {
                return true;
            }
        }
        return false;
    }
}
