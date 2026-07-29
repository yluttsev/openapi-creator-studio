package ru.luttsev.studio.core.validation.rule;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import ru.luttsev.studio.core.model.server.Server;
import ru.luttsev.studio.core.model.server.ServerVariable;
import ru.luttsev.studio.core.navigation.DocumentEntry;
import ru.luttsev.studio.core.validation.ValidationCode;
import ru.luttsev.studio.core.validation.ValidationContext;
import ru.luttsev.studio.core.validation.ValidationIssue;
import ru.luttsev.studio.core.validation.ValidationRule;
import ru.luttsev.studio.core.validation.ValidationSeverity;
import ru.luttsev.studio.core.validation.support.DocumentEntries;

public final class ServerRule implements ValidationRule {

    private static final Pattern TEMPLATE_EXPRESSION = Pattern.compile("\\{([^{}]+)}");
    private static final ValidationCode QUERY_OR_FRAGMENT =
            new ValidationCode("server.url.query-or-fragment");
    private static final ValidationCode INVALID_TEMPLATE =
            new ValidationCode("server.url.template.invalid");
    private static final ValidationCode UNDEFINED_VARIABLE =
            new ValidationCode("server.variable.undefined");
    private static final ValidationCode DEFAULT_OUTSIDE_ENUM =
            new ValidationCode("server.variable.default-outside-enum");

    @Override
    public List<ValidationIssue> validate(ValidationContext context) {
        ArrayList<ValidationIssue> issues = new ArrayList<>();
        for (DocumentEntry entry : DocumentEntries.ofType(context, Server.class)) {
            Server server = (Server) entry.value();
            validateServer(server, entry, issues);
        }
        return List.copyOf(issues);
    }

    private static void validateServer(
            Server server,
            DocumentEntry entry,
            List<ValidationIssue> issues) {
        String url = server.getUrl();
        if (url == null) {
            return;
        }

        Set<String> variables = extractVariables(url);
        if (!isWellFormed(url)) {
            issues.add(new ValidationIssue(
                    INVALID_TEMPLATE,
                    ValidationSeverity.ERROR,
                    "Server URL contains an invalid template expression",
                    entry.path().child("url")));
        }

        for (String variable : variables) {
            if (!server.getVariables().containsKey(variable)) {
                issues.add(new ValidationIssue(
                        UNDEFINED_VARIABLE,
                        ValidationSeverity.ERROR,
                        "Server URL variable is not defined: " + variable,
                        entry.path().child("url")));
            }
        }

        for (Map.Entry<String, ServerVariable> variableEntry :
                server.getVariables().entrySet()) {
            ServerVariable variable = variableEntry.getValue();
            if (variable.getDefaultValue() != null
                    && !variable.getEnumValues().isEmpty()
                    && !variable.getEnumValues().contains(variable.getDefaultValue())) {
                issues.add(new ValidationIssue(
                        DEFAULT_OUTSIDE_ENUM,
                        ValidationSeverity.ERROR,
                        "Server variable default must be one of its enum values",
                        entry.path()
                                .child("variables")
                                .child(variableEntry.getKey())
                                .child("default")));
            }
        }

        String expandedUrl = expandDefaults(url, server.getVariables());
        if (hasQueryOrFragment(url)
                || hasQueryOrFragment(expandedUrl)) {
            issues.add(new ValidationIssue(
                    QUERY_OR_FRAGMENT,
                    ValidationSeverity.ERROR,
                    "Server URL must not contain a query or fragment",
                    entry.path().child("url")));
        }
    }

    private static Set<String> extractVariables(String url) {
        LinkedHashSet<String> variables = new LinkedHashSet<>();
        Matcher matcher = TEMPLATE_EXPRESSION.matcher(url);
        while (matcher.find()) {
            variables.add(matcher.group(1));
        }
        return variables;
    }

    private static boolean isWellFormed(String url) {
        String withoutExpressions = TEMPLATE_EXPRESSION.matcher(url).replaceAll("");
        return !withoutExpressions.contains("{")
                && !withoutExpressions.contains("}");
    }

    private static String expandDefaults(
            String url,
            Map<String, ServerVariable> variables) {
        String expanded = url;
        for (Map.Entry<String, ServerVariable> entry : variables.entrySet()) {
            if (entry.getValue().getDefaultValue() != null) {
                expanded = expanded.replace(
                        "{" + entry.getKey() + "}",
                        entry.getValue().getDefaultValue());
            }
        }
        return expanded;
    }

    private static boolean hasQueryOrFragment(String url) {
        return url.indexOf('?') >= 0 || url.indexOf('#') >= 0;
    }
}
