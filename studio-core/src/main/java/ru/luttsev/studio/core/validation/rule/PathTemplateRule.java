package ru.luttsev.studio.core.validation.rule;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import ru.luttsev.studio.core.model.parameter.Parameter;
import ru.luttsev.studio.core.model.parameter.ParameterLocation;
import ru.luttsev.studio.core.model.path.HttpMethod;
import ru.luttsev.studio.core.model.path.Operation;
import ru.luttsev.studio.core.model.path.PathItem;
import ru.luttsev.studio.core.model.path.Paths;
import ru.luttsev.studio.core.navigation.DocumentEntry;
import ru.luttsev.studio.core.navigation.DocumentPath;
import ru.luttsev.studio.core.validation.ValidationCode;
import ru.luttsev.studio.core.validation.ValidationContext;
import ru.luttsev.studio.core.validation.ValidationIssue;
import ru.luttsev.studio.core.validation.ValidationRule;
import ru.luttsev.studio.core.validation.ValidationSeverity;
import ru.luttsev.studio.core.validation.support.DocumentEntries;
import ru.luttsev.studio.core.validation.support.ParameterOccurrence;
import ru.luttsev.studio.core.validation.support.ParameterSupport;

public final class PathTemplateRule implements ValidationRule {

    private static final Pattern TEMPLATE_EXPRESSION = Pattern.compile("\\{([^{}]+)}");
    private static final ValidationCode INVALID_TEMPLATE =
            new ValidationCode("path.template.invalid");
    private static final ValidationCode MISSING_PARAMETER =
            new ValidationCode("path.template.parameter.missing");
    private static final ValidationCode EXTRA_PARAMETER =
            new ValidationCode("path.parameter.template.missing");
    private static final ValidationCode REQUIRED_PARAMETER =
            new ValidationCode("path.parameter.required");

    @Override
    public List<ValidationIssue> validate(ValidationContext context) {
        ArrayList<ValidationIssue> issues = new ArrayList<>();
        validateRequiredPathParameters(context, issues);

        Paths paths = context.document().getPaths();
        if (paths == null) {
            return List.copyOf(issues);
        }
        for (Map.Entry<String, PathItem> pathEntry : paths.getItems().entrySet()) {
            validatePath(context, pathEntry.getKey(), pathEntry.getValue(), issues);
        }
        return List.copyOf(issues);
    }

    private static void validateRequiredPathParameters(
            ValidationContext context,
            List<ValidationIssue> issues) {
        for (DocumentEntry entry : DocumentEntries.ofType(context, Parameter.class)) {
            Parameter parameter = (Parameter) entry.value();
            if (parameter.getLocation() == ParameterLocation.PATH
                    && !Boolean.TRUE.equals(parameter.getRequired())) {
                issues.add(new ValidationIssue(
                        REQUIRED_PARAMETER,
                        ValidationSeverity.ERROR,
                        "Path parameters must declare required: true",
                        entry.path().child("required")));
            }
        }
    }

    private static void validatePath(
            ValidationContext context,
            String path,
            PathItem pathItem,
            List<ValidationIssue> issues) {
        DocumentPath pathItemPath =
                DocumentPath.root().child("paths").child(path);
        Set<String> templateVariables = extractVariables(path);
        if (!isWellFormed(path)) {
            issues.add(new ValidationIssue(
                    INVALID_TEMPLATE,
                    ValidationSeverity.ERROR,
                    "Path contains an invalid template expression: " + path,
                    pathItemPath));
        }

        if (pathItem.getOperations().isEmpty()) {
            validateParameters(
                    templateVariables,
                    ParameterSupport.resolve(
                            context,
                            pathItem.getParameters(),
                            pathItemPath.child("parameters")),
                    pathItemPath.child("parameters"),
                    issues);
            return;
        }

        for (Map.Entry<HttpMethod, Operation> operationEntry :
                pathItem.getOperations().entrySet()) {
            DocumentPath operationPath =
                    pathItemPath.child(operationEntry.getKey().value().toLowerCase());
            List<ParameterOccurrence> effectiveParameters = ParameterSupport.effective(
                    context,
                    pathItem,
                    pathItemPath,
                    operationEntry.getValue(),
                    operationPath);
            validateParameters(
                    templateVariables,
                    effectiveParameters,
                    operationPath.child("parameters"),
                    issues);
        }
    }

    private static void validateParameters(
            Set<String> templateVariables,
            List<ParameterOccurrence> parameters,
            DocumentPath missingParameterPath,
            List<ValidationIssue> issues) {
        Set<String> parameterNames = new LinkedHashSet<>();
        for (ParameterOccurrence occurrence : parameters) {
            Parameter parameter = occurrence.parameter();
            if (parameter.getLocation() != ParameterLocation.PATH
                    || parameter.getName() == null) {
                continue;
            }
            parameterNames.add(parameter.getName());
            if (!templateVariables.contains(parameter.getName())) {
                issues.add(new ValidationIssue(
                        EXTRA_PARAMETER,
                        ValidationSeverity.ERROR,
                        "Path parameter has no matching template expression: "
                                + parameter.getName(),
                        occurrence.usagePath()));
            }
        }

        for (String variable : templateVariables) {
            if (!parameterNames.contains(variable)) {
                issues.add(new ValidationIssue(
                        MISSING_PARAMETER,
                        ValidationSeverity.ERROR,
                        "Path template expression has no matching path parameter: "
                                + variable,
                        missingParameterPath));
            }
        }
    }

    private static Set<String> extractVariables(String path) {
        LinkedHashSet<String> variables = new LinkedHashSet<>();
        Matcher matcher = TEMPLATE_EXPRESSION.matcher(path);
        while (matcher.find()) {
            variables.add(matcher.group(1));
        }
        return variables;
    }

    private static boolean isWellFormed(String path) {
        String withoutExpressions = TEMPLATE_EXPRESSION.matcher(path).replaceAll("");
        return !withoutExpressions.contains("{")
                && !withoutExpressions.contains("}");
    }
}
