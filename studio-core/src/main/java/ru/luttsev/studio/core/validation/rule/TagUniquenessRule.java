package ru.luttsev.studio.core.validation.rule;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import ru.luttsev.studio.core.model.tag.Tag;
import ru.luttsev.studio.core.navigation.DocumentPath;
import ru.luttsev.studio.core.validation.ValidationCode;
import ru.luttsev.studio.core.validation.ValidationContext;
import ru.luttsev.studio.core.validation.ValidationIssue;
import ru.luttsev.studio.core.validation.ValidationRule;
import ru.luttsev.studio.core.validation.ValidationSeverity;

public final class TagUniquenessRule implements ValidationRule {

    private static final ValidationCode DUPLICATE_TAG =
            new ValidationCode("tag.name.duplicate");

    @Override
    public List<ValidationIssue> validate(ValidationContext context) {
        Map<String, List<Integer>> indexesByName = new LinkedHashMap<>();
        List<Tag> tags = context.document().getTags();
        for (int index = 0; index < tags.size(); index++) {
            String name = tags.get(index).getName();
            if (name != null) {
                indexesByName
                        .computeIfAbsent(name, ignored -> new ArrayList<>())
                        .add(index);
            }
        }

        ArrayList<ValidationIssue> issues = new ArrayList<>();
        for (Map.Entry<String, List<Integer>> group : indexesByName.entrySet()) {
            if (group.getValue().size() < 2) {
                continue;
            }
            for (Integer index : group.getValue()) {
                issues.add(new ValidationIssue(
                        DUPLICATE_TAG,
                        ValidationSeverity.ERROR,
                        "Tag name must be unique: " + group.getKey(),
                        DocumentPath.root()
                                .child("tags")
                                .child(index.toString())
                                .child("name")));
            }
        }
        return List.copyOf(issues);
    }
}
