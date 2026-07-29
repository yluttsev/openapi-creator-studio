package ru.luttsev.studio.core.validation;

import java.util.List;
import java.util.Objects;

public record ValidationResult(List<ValidationIssue> issues) {

    public ValidationResult {
        Objects.requireNonNull(issues, "issues must not be null");
        issues = List.copyOf(issues);
    }

    public boolean isValid() {
        return issues.stream()
                .noneMatch(issue -> issue.severity() == ValidationSeverity.ERROR);
    }

    public List<ValidationIssue> errors() {
        return issues.stream()
                .filter(issue -> issue.severity() == ValidationSeverity.ERROR)
                .toList();
    }

    public List<ValidationIssue> warnings() {
        return issues.stream()
                .filter(issue -> issue.severity() == ValidationSeverity.WARNING)
                .toList();
    }
}
