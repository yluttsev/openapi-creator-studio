package ru.luttsev.studio.core.validation.rule;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import ru.luttsev.studio.core.model.path.Paths;
import ru.luttsev.studio.core.navigation.DocumentPath;
import ru.luttsev.studio.core.validation.ValidationCode;
import ru.luttsev.studio.core.validation.ValidationContext;
import ru.luttsev.studio.core.validation.ValidationIssue;
import ru.luttsev.studio.core.validation.ValidationRule;
import ru.luttsev.studio.core.validation.ValidationSeverity;

public final class AmbiguousPathRule implements ValidationRule {

    private static final Pattern TEMPLATE_EXPRESSION = Pattern.compile("\\{[^{}]+}");
    private static final ValidationCode AMBIGUOUS_PATH =
            new ValidationCode("path.template.ambiguous");

    @Override
    public List<ValidationIssue> validate(ValidationContext context) {
        Paths paths = context.document().getPaths();
        if (paths == null) {
            return List.of();
        }

        Map<String, List<String>> byHierarchy = new LinkedHashMap<>();
        for (String path : paths.getItems().keySet()) {
            String hierarchy = TEMPLATE_EXPRESSION.matcher(path).replaceAll("{}");
            byHierarchy
                    .computeIfAbsent(hierarchy, ignored -> new ArrayList<>())
                    .add(path);
        }

        ArrayList<ValidationIssue> issues = new ArrayList<>();
        for (List<String> equivalentPaths : byHierarchy.values()) {
            if (equivalentPaths.size() < 2) {
                continue;
            }
            for (String path : equivalentPaths) {
                issues.add(new ValidationIssue(
                        AMBIGUOUS_PATH,
                        ValidationSeverity.ERROR,
                        "Templated paths with the same hierarchy are not allowed: "
                                + String.join(", ", equivalentPaths),
                        DocumentPath.root().child("paths").child(path)));
            }
        }
        return List.copyOf(issues);
    }
}
