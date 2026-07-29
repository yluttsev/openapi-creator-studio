package ru.luttsev.studio.core.validation.rule;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import ru.luttsev.studio.core.model.schema.Discriminator;
import ru.luttsev.studio.core.model.schema.Schema;
import ru.luttsev.studio.core.model.schema.SchemaDefinition;
import ru.luttsev.studio.core.model.schema.UriReference;
import ru.luttsev.studio.core.navigation.DocumentEntry;
import ru.luttsev.studio.core.navigation.DocumentPath;
import ru.luttsev.studio.core.validation.ValidationCode;
import ru.luttsev.studio.core.validation.ValidationContext;
import ru.luttsev.studio.core.validation.ValidationIssue;
import ru.luttsev.studio.core.validation.ValidationRule;
import ru.luttsev.studio.core.validation.ValidationSeverity;
import ru.luttsev.studio.core.validation.support.DocumentEntries;
import ru.luttsev.studio.core.validation.support.ReferenceValueResolver;
import ru.luttsev.studio.core.validation.support.SchemaSupport;

public final class DiscriminatorRule implements ValidationRule {

    private static final ValidationCode MISSING_COMPOSITION =
            new ValidationCode("discriminator.composition.missing");
    private static final ValidationCode MAPPING_NOT_FOUND =
            new ValidationCode("discriminator.mapping.target.not-found");
    private static final ValidationCode INVALID_MAPPING =
            new ValidationCode("discriminator.mapping.invalid");
    private static final ValidationCode PROPERTY_NOT_REQUIRED =
            new ValidationCode("discriminator.property.not-required");

    @Override
    public List<ValidationIssue> validate(ValidationContext context) {
        ArrayList<ValidationIssue> issues = new ArrayList<>();
        for (DocumentEntry entry : DocumentEntries.ofType(
                context,
                SchemaDefinition.class)) {
            SchemaDefinition schema = (SchemaDefinition) entry.value();
            if (schema.getDiscriminator() != null) {
                validateDiscriminator(context, entry, schema, issues);
            }
        }
        return List.copyOf(issues);
    }

    private static void validateDiscriminator(
            ValidationContext context,
            DocumentEntry entry,
            SchemaDefinition schema,
            List<ValidationIssue> issues) {
        Discriminator discriminator = schema.getDiscriminator();
        DocumentPath discriminatorPath = entry.path().child("discriminator");
        if (schema.getOneOf().isEmpty()
                && schema.getAnyOf().isEmpty()
                && schema.getAllOf().isEmpty()) {
            issues.add(new ValidationIssue(
                    MISSING_COMPOSITION,
                    ValidationSeverity.ERROR,
                    "Discriminator requires oneOf, anyOf, or allOf",
                    discriminatorPath));
        }

        String propertyName = discriminator.getPropertyName();
        if (propertyName != null
                && !SchemaSupport.isRequired(context, schema, propertyName)) {
            issues.add(new ValidationIssue(
                    PROPERTY_NOT_REQUIRED,
                    ValidationSeverity.WARNING,
                    "Discriminator property should be required: " + propertyName,
                    discriminatorPath.child("propertyName")));
        }

        ReferenceValueResolver resolver = new ReferenceValueResolver(context);
        for (Map.Entry<String, String> mapping :
                discriminator.getMapping().entrySet()) {
            if (isInvalidLocalMapping(mapping.getValue())) {
                issues.add(new ValidationIssue(
                        INVALID_MAPPING,
                        ValidationSeverity.ERROR,
                        "Discriminator mapping is not a valid URI reference: "
                                + mapping.getValue(),
                        discriminatorPath.child("mapping").child(mapping.getKey())));
                continue;
            }
            UriReference reference = toLocalReference(mapping.getValue());
            if (reference == null) {
                continue;
            }
            if (resolver.resolve(reference, Schema.class).isEmpty()) {
                issues.add(new ValidationIssue(
                        MAPPING_NOT_FOUND,
                        ValidationSeverity.ERROR,
                        "Discriminator mapping target does not exist: "
                                + mapping.getValue(),
                        discriminatorPath.child("mapping").child(mapping.getKey())));
            }
        }
    }

    private static UriReference toLocalReference(String mapping) {
        if (mapping.startsWith("#")) {
            return new UriReference(mapping);
        }
        if (!mapping.contains("/")
                && !mapping.contains(":")) {
            return new UriReference("#/components/schemas/" + mapping);
        }
        return null;
    }

    private static boolean isInvalidLocalMapping(String mapping) {
        return mapping == null
                || mapping.isBlank()
                || mapping.startsWith("#") && !isValidUri(mapping);
    }

    private static boolean isValidUri(String value) {
        try {
            new URI(value);
            return true;
        } catch (URISyntaxException exception) {
            return false;
        }
    }
}
