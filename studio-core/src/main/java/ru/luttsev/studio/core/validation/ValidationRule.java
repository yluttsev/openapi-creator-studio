package ru.luttsev.studio.core.validation;

import java.util.List;

public interface ValidationRule {

    List<ValidationIssue> validate(ValidationContext context);
}
