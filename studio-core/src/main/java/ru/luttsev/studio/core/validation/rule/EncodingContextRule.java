package ru.luttsev.studio.core.validation.rule;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import ru.luttsev.studio.core.model.media.MediaType;
import ru.luttsev.studio.core.model.media.RequestBody;
import ru.luttsev.studio.core.model.schema.SchemaDefinition;
import ru.luttsev.studio.core.navigation.DocumentEntry;
import ru.luttsev.studio.core.validation.ValidationCode;
import ru.luttsev.studio.core.validation.ValidationContext;
import ru.luttsev.studio.core.validation.ValidationIssue;
import ru.luttsev.studio.core.validation.ValidationRule;
import ru.luttsev.studio.core.validation.ValidationSeverity;
import ru.luttsev.studio.core.validation.support.DocumentAncestors;
import ru.luttsev.studio.core.validation.support.DocumentEntries;
import ru.luttsev.studio.core.validation.support.SchemaSupport;

public final class EncodingContextRule implements ValidationRule {

    private static final ValidationCode INVALID_CONTEXT =
            new ValidationCode("encoding.context.invalid");
    private static final ValidationCode INVALID_MEDIA_TYPE =
            new ValidationCode("encoding.media-type.invalid");
    private static final ValidationCode UNKNOWN_PROPERTY =
            new ValidationCode("encoding.property.unknown");

    @Override
    public List<ValidationIssue> validate(ValidationContext context) {
        ArrayList<ValidationIssue> issues = new ArrayList<>();
        for (DocumentEntry entry : DocumentEntries.ofType(context, MediaType.class)) {
            MediaType mediaType = (MediaType) entry.value();
            if (mediaType.getEncoding().isEmpty()) {
                continue;
            }
            validateMediaType(context, entry, mediaType, issues);
        }
        return List.copyOf(issues);
    }

    private static void validateMediaType(
            ValidationContext context,
            DocumentEntry entry,
            MediaType mediaType,
            List<ValidationIssue> issues) {
        if (DocumentAncestors.findNearest(
                context,
                entry.path(),
                RequestBody.class).isEmpty()) {
            issues.add(new ValidationIssue(
                    INVALID_CONTEXT,
                    ValidationSeverity.ERROR,
                    "Encoding is only allowed for request body media types",
                    entry.path().child("encoding")));
            return;
        }

        String mediaTypeName = entry.path().segments()
                .get(entry.path().segments().size() - 1)
                .toLowerCase(Locale.ROOT);
        if (!mediaTypeName.startsWith("multipart/")
                && !mediaTypeName.equals("application/x-www-form-urlencoded")) {
            issues.add(new ValidationIssue(
                    INVALID_MEDIA_TYPE,
                    ValidationSeverity.ERROR,
                    "Encoding is only allowed for multipart/* or "
                            + "application/x-www-form-urlencoded",
                    entry.path().child("encoding")));
        }

        Optional<SchemaDefinition> schema =
                SchemaSupport.resolveDefinition(context, mediaType.getSchema());
        for (String property : mediaType.getEncoding().keySet()) {
            if (schema.isEmpty()
                    || !schema.orElseThrow().getProperties().containsKey(property)) {
                issues.add(new ValidationIssue(
                        UNKNOWN_PROPERTY,
                        ValidationSeverity.ERROR,
                        "Encoding key must match a property in the media type schema: "
                                + property,
                        entry.path().child("encoding").child(property)));
            }
        }
    }
}
