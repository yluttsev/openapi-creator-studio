package ru.luttsev.studio.application.validation;

import java.util.List;
import ru.luttsev.studio.core.validation.ValidationIssue;

public record DocumentValidation(
        long revision,
        boolean valid,
        List<ValidationIssue> issues) {

    public DocumentValidation {
        issues = List.copyOf(issues);
    }
}
