package ru.luttsev.studio.core.validation.rule;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import ru.luttsev.studio.core.model.link.Link;
import ru.luttsev.studio.core.model.parameter.Parameter;
import ru.luttsev.studio.core.model.parameter.ParameterLocation;
import ru.luttsev.studio.core.model.path.Operation;
import ru.luttsev.studio.core.model.response.ApiResponse;
import ru.luttsev.studio.core.model.response.ResponseKey;
import ru.luttsev.studio.core.model.reference.ReferenceOr;
import ru.luttsev.studio.core.model.schema.UriReference;
import ru.luttsev.studio.core.model.value.DocumentValue;
import ru.luttsev.studio.core.model.value.StringValue;
import ru.luttsev.studio.core.navigation.DocumentEntry;
import ru.luttsev.studio.core.navigation.DocumentPath;
import ru.luttsev.studio.core.reference.ReferenceFailure;
import ru.luttsev.studio.core.reference.ReferenceResolution;
import ru.luttsev.studio.core.reference.ResolvedReference;
import ru.luttsev.studio.core.reference.UnresolvedReference;
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

public final class LinkIntegrityRule implements ValidationRule {

    private static final ValidationCode OPERATION_NOT_FOUND =
            new ValidationCode("link.operation.not-found");
    private static final ValidationCode OPERATION_AMBIGUOUS =
            new ValidationCode("link.operation.ambiguous");
    private static final ValidationCode OPERATION_REF_UNCHECKED =
            new ValidationCode("link.operation-ref.unchecked");
    private static final ValidationCode UNKNOWN_TARGET_PARAMETER =
            new ValidationCode("link.target-parameter.unknown");
    private static final ValidationCode INVALID_EXPRESSION =
            new ValidationCode("link.expression.invalid");
    private static final ValidationCode UNKNOWN_SOURCE_PARAMETER =
            new ValidationCode("link.expression.request-parameter.unknown");
    private static final ValidationCode UNKNOWN_RESPONSE_HEADER =
            new ValidationCode("link.expression.response-header.unknown");

    @Override
    public List<ValidationIssue> validate(ValidationContext context) {
        OperationIndex operations = new OperationIndex(context);
        ArrayList<ValidationIssue> issues = new ArrayList<>();
        validateLinks(context, operations, issues);
        validateSourceExpressions(context, operations, issues);
        return List.copyOf(issues);
    }

    private static void validateLinks(
            ValidationContext context,
            OperationIndex operations,
            List<ValidationIssue> issues) {
        for (DocumentEntry entry : DocumentEntries.ofType(context, Link.class)) {
            Link link = (Link) entry.value();
            Optional<OperationOccurrence> target =
                    resolveTarget(context, operations, link, entry.path(), issues);
            validateExpressions(link, entry.path(), issues);
            target.ifPresent(operation -> validateTargetParameters(
                    context,
                    link,
                    entry.path(),
                    operation,
                    issues));
        }
    }

    private static Optional<OperationOccurrence> resolveTarget(
            ValidationContext context,
            OperationIndex operations,
            Link link,
            DocumentPath linkPath,
            List<ValidationIssue> issues) {
        if (link.getOperationId() != null) {
            List<OperationOccurrence> matches =
                    operations.findByOperationId(link.getOperationId());
            if (matches.size() == 1) {
                return Optional.of(matches.getFirst());
            }
            issues.add(new ValidationIssue(
                    matches.isEmpty() ? OPERATION_NOT_FOUND : OPERATION_AMBIGUOUS,
                    ValidationSeverity.ERROR,
                    matches.isEmpty()
                            ? "Link operationId does not identify an operation: "
                                    + link.getOperationId()
                            : "Link operationId identifies more than one operation: "
                                    + link.getOperationId(),
                    linkPath.child("operationId")));
            return Optional.empty();
        }

        UriReference operationRef = link.getOperationRef();
        if (operationRef == null) {
            return Optional.empty();
        }
        ReferenceResolution<Operation> resolution =
                context.referenceResolver().resolve(
                        context.document(),
                        operationRef,
                        Operation.class);
        if (resolution instanceof ResolvedReference<Operation> resolved) {
            return operations.findByPath(resolved.targetPath());
        }

        ReferenceFailure failure =
                ((UnresolvedReference<Operation>) resolution).failure();
        ValidationSeverity severity =
                failure == ReferenceFailure.EXTERNAL_REFERENCE
                                || failure == ReferenceFailure.UNSUPPORTED_ANCHOR
                        ? ValidationSeverity.WARNING
                        : ValidationSeverity.ERROR;
        issues.add(new ValidationIssue(
                severity == ValidationSeverity.WARNING
                        ? OPERATION_REF_UNCHECKED
                        : OPERATION_NOT_FOUND,
                severity,
                severity == ValidationSeverity.WARNING
                        ? "External or anchored operationRef cannot be checked locally"
                        : "operationRef does not identify an operation",
                linkPath.child("operationRef")));
        return Optional.empty();
    }

    private static void validateTargetParameters(
            ValidationContext context,
            Link link,
            DocumentPath linkPath,
            OperationOccurrence target,
            List<ValidationIssue> issues) {
        List<ParameterOccurrence> parameters = target.effectiveParameters(context);
        for (String linkParameter : link.getParameters().keySet()) {
            if (!containsTargetParameter(parameters, linkParameter)) {
                issues.add(new ValidationIssue(
                        UNKNOWN_TARGET_PARAMETER,
                        ValidationSeverity.ERROR,
                        "Link parameter does not exist on the target operation: "
                                + linkParameter,
                        linkPath.child("parameters").child(linkParameter)));
            }
        }
    }

    private static boolean containsTargetParameter(
            List<ParameterOccurrence> parameters,
            String linkParameter) {
        int separator = linkParameter.indexOf('.');
        String possibleLocation = separator < 0
                ? null
                : linkParameter.substring(0, separator);
        boolean qualified = isParameterLocation(possibleLocation);
        String location = qualified ? possibleLocation : null;
        String name = qualified
                ? linkParameter.substring(separator + 1)
                : linkParameter;
        for (ParameterOccurrence occurrence : parameters) {
            Parameter parameter = occurrence.parameter();
            if (!name.equals(parameter.getName())) {
                continue;
            }
            if (location == null
                    || locationMatches(parameter.getLocation(), location)) {
                return true;
            }
        }
        return false;
    }

    private static boolean locationMatches(
            ParameterLocation location,
            String value) {
        return location != null
                && location.name().toLowerCase(Locale.ROOT).equals(value);
    }

    private static boolean isParameterLocation(String value) {
        return value != null
                && (value.equals("path")
                        || value.equals("query")
                        || value.equals("header")
                        || value.equals("cookie"));
    }

    private static void validateExpressions(
            Link link,
            DocumentPath linkPath,
            List<ValidationIssue> issues) {
        for (Map.Entry<String, DocumentValue> parameter :
                link.getParameters().entrySet()) {
            validateExpression(
                    parameter.getValue(),
                    linkPath.child("parameters").child(parameter.getKey()),
                    issues);
        }
        validateExpression(
                link.getRequestBody(),
                linkPath.child("requestBody"),
                issues);
    }

    private static void validateExpression(
            DocumentValue value,
            DocumentPath path,
            List<ValidationIssue> issues) {
        if (!(value instanceof StringValue stringValue)
                || !stringValue.value().startsWith("$")) {
            return;
        }
        if (!RuntimeExpressionSupport.isValid(stringValue.value())) {
            issues.add(new ValidationIssue(
                    INVALID_EXPRESSION,
                    ValidationSeverity.ERROR,
                    "Link value is not a valid runtime expression: "
                            + stringValue.value(),
                    path));
        }
    }

    private static void validateSourceExpressions(
            ValidationContext context,
            OperationIndex operations,
            List<ValidationIssue> issues) {
        ReferenceValueResolver resolver = new ReferenceValueResolver(context);
        for (OperationOccurrence source : operations.all()) {
            for (Map.Entry<ResponseKey, ReferenceOr<ApiResponse>> responseEntry :
                    source.operation().getResponses().entrySet()) {
                DocumentPath responseUsagePath = source.path()
                        .child("responses")
                        .child(responseEntry.getKey().value());
                Optional<ResolvedValue<ApiResponse>> response =
                        resolver.resolveWithPath(
                                responseEntry.getValue(),
                                ApiResponse.class,
                                responseUsagePath);
                if (response.isEmpty()) {
                    continue;
                }
                for (Map.Entry<String, ReferenceOr<Link>> linkEntry :
                        response.orElseThrow().value().getLinks().entrySet()) {
                    DocumentPath linkUsagePath = response.orElseThrow()
                            .path()
                            .child("links")
                            .child(linkEntry.getKey());
                    Optional<ResolvedValue<Link>> link =
                            resolver.resolveWithPath(
                                    linkEntry.getValue(),
                                    Link.class,
                                    linkUsagePath);
                    if (link.isPresent()) {
                        validateSourceExpressions(
                                context,
                                source,
                                response.orElseThrow().value(),
                                link.orElseThrow(),
                                issues);
                    }
                }
            }
        }
    }

    private static void validateSourceExpressions(
            ValidationContext context,
            OperationOccurrence source,
            ApiResponse response,
            ResolvedValue<Link> link,
            List<ValidationIssue> issues) {
        List<ParameterOccurrence> sourceParameters =
                source.effectiveParameters(context);
        for (Map.Entry<String, DocumentValue> parameter :
                link.value().getParameters().entrySet()) {
            validateSourceExpression(
                    parameter.getValue(),
                    link.path().child("parameters").child(parameter.getKey()),
                    sourceParameters,
                    response,
                    issues);
        }
        validateSourceExpression(
                link.value().getRequestBody(),
                link.path().child("requestBody"),
                sourceParameters,
                response,
                issues);
    }

    private static void validateSourceExpression(
            DocumentValue value,
            DocumentPath path,
            List<ParameterOccurrence> sourceParameters,
            ApiResponse response,
            List<ValidationIssue> issues) {
        if (!(value instanceof StringValue stringValue)
                || !RuntimeExpressionSupport.isValid(stringValue.value())) {
            return;
        }
        Optional<RuntimeParameterReference> requestParameter =
                RuntimeExpressionSupport.requestParameter(stringValue.value());
        if (requestParameter.isPresent()
                && !containsSourceParameter(
                        sourceParameters,
                        requestParameter.orElseThrow())) {
            issues.add(new ValidationIssue(
                    UNKNOWN_SOURCE_PARAMETER,
                    ValidationSeverity.ERROR,
                    "Link expression references an undeclared request parameter: "
                            + requestParameter.orElseThrow().name(),
                    path));
        }

        Optional<String> responseHeader =
                RuntimeExpressionSupport.responseHeader(stringValue.value());
        if (responseHeader.isPresent()
                && response.getHeaders().keySet().stream()
                        .noneMatch(header -> header.equalsIgnoreCase(
                                responseHeader.orElseThrow()))) {
            issues.add(new ValidationIssue(
                    UNKNOWN_RESPONSE_HEADER,
                    ValidationSeverity.ERROR,
                    "Link expression references an undeclared response header: "
                            + responseHeader.orElseThrow(),
                    path));
        }
    }

    private static boolean containsSourceParameter(
            List<ParameterOccurrence> parameters,
            RuntimeParameterReference reference) {
        for (ParameterOccurrence occurrence : parameters) {
            Parameter parameter = occurrence.parameter();
            if (parameter.getLocation() == reference.location()
                    && reference.name().equals(parameter.getName())) {
                return true;
            }
        }
        return false;
    }
}
