package ru.luttsev.studio.openapi.version.v31.encode;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import ru.luttsev.studio.core.model.schema.JsonType;
import ru.luttsev.studio.core.model.schema.LogicalSchema;
import ru.luttsev.studio.core.model.schema.Schema;
import ru.luttsev.studio.core.model.schema.SchemaDefinition;
import ru.luttsev.studio.core.model.value.ArrayValue;
import ru.luttsev.studio.core.model.value.BooleanValue;
import ru.luttsev.studio.core.model.value.DocumentValue;
import ru.luttsev.studio.core.model.value.ObjectValue;
import ru.luttsev.studio.core.model.value.StringValue;
import ru.luttsev.studio.openapi.diagnostic.OpenApiDiagnosticCodes;

final class SchemaEncoder {

    private static final Set<String> MAPPED_KEYWORDS = Set.of(
            "type",
            "format",
            "$ref",
            "title",
            "description",
            "default",
            "example",
            "examples",
            "enum",
            "const",
            "deprecated",
            "readOnly",
            "writeOnly",
            "properties",
            "required",
            "additionalProperties",
            "minProperties",
            "maxProperties",
            "items",
            "minItems",
            "maxItems",
            "uniqueItems",
            "minLength",
            "maxLength",
            "pattern",
            "minimum",
            "maximum",
            "exclusiveMinimum",
            "exclusiveMaximum",
            "multipleOf",
            "allOf",
            "anyOf",
            "oneOf",
            "not",
            "discriminator");

    private final DiscriminatorEncoder discriminatorEncoder =
            new DiscriminatorEncoder();

    DocumentValue encode(
            Schema source,
            EncodeContext context) {
        return switch (source) {
            case LogicalSchema logicalSchema ->
                    new BooleanValue(logicalSchema.isValue());
            case SchemaDefinition definition ->
                    encodeDefinitionGuarded(definition, context);
        };
    }

    private ObjectValue encodeDefinitionGuarded(
            SchemaDefinition source,
            EncodeContext context) {
        if (!context.enter(source)) {
            context.mappingError(
                    OpenApiDiagnosticCodes.MAPPING_CYCLIC_INLINE_OBJECT,
                    "Cyclic inline Schema cannot be encoded");
            return new ObjectValueBuilder().build();
        }
        try {
            return encodeDefinition(source, context);
        } finally {
            context.leave(source);
        }
    }

    private ObjectValue encodeDefinition(
            SchemaDefinition source,
            EncodeContext context) {
        ObjectValueBuilder target = new ObjectValueBuilder();
        putTypes(target, source.getTypes());
        if (source.getFormat() != null) {
            target.putString("format", source.getFormat().value());
        }
        if (source.getRef() != null) {
            target.putString("$ref", source.getRef().value());
        }
        target.putString("title", source.getTitle())
                .putString("description", source.getDescription());
        putValue(target, "default", source.getDefaultValue());
        target.putArray("examples", source.getExamples())
                .putArray("enum", source.getEnumValues());
        putValue(target, "const", source.getConstValue());
        target.putBoolean("deprecated", source.getDeprecated())
                .putBoolean("readOnly", source.getReadOnly())
                .putBoolean("writeOnly", source.getWriteOnly());

        putSchemaMap(
                target,
                "properties",
                source.getProperties(),
                context.child("properties"));
        putStringSet(target, "required", source.getRequired());
        putSchema(
                target,
                "additionalProperties",
                source.getAdditionalProperties(),
                context.child("additionalProperties"));
        target.putInteger("minProperties", source.getMinProperties())
                .putInteger("maxProperties", source.getMaxProperties());

        putSchema(
                target,
                "items",
                source.getItems(),
                context.child("items"));
        target.putInteger("minItems", source.getMinItems())
                .putInteger("maxItems", source.getMaxItems())
                .putBoolean("uniqueItems", source.getUniqueItems())
                .putInteger("minLength", source.getMinLength())
                .putInteger("maxLength", source.getMaxLength())
                .putString("pattern", source.getPattern())
                .putNumber("minimum", source.getMinimum())
                .putNumber("maximum", source.getMaximum())
                .putNumber("exclusiveMinimum", source.getExclusiveMinimum())
                .putNumber("exclusiveMaximum", source.getExclusiveMaximum())
                .putNumber("multipleOf", source.getMultipleOf());

        putSchemaList(
                target,
                "allOf",
                source.getAllOf(),
                context.child("allOf"));
        putSchemaList(
                target,
                "anyOf",
                source.getAnyOf(),
                context.child("anyOf"));
        putSchemaList(
                target,
                "oneOf",
                source.getOneOf(),
                context.child("oneOf"));
        putSchema(
                target,
                "not",
                source.getNotSchema(),
                context.child("not"));
        if (source.getDiscriminator() != null) {
            target.put(
                    "discriminator",
                    discriminatorEncoder.encode(
                            source.getDiscriminator(),
                            context.child("discriminator")));
        }

        if (source.getAdditionalKeywords() != null) {
            AdditionalFieldsEncoder.copy(
                    source.getAdditionalKeywords(),
                    "Additional schema keyword",
                    target,
                    MAPPED_KEYWORDS,
                    context);
        }
        return target.build();
    }

    private static void putTypes(
            ObjectValueBuilder target,
            Set<JsonType> types) {
        if (types == null || types.isEmpty()) {
            return;
        }

        ArrayList<DocumentValue> values = new ArrayList<>(types.size());
        for (JsonType type : types) {
            values.add(new StringValue(
                    type.name().toLowerCase(Locale.ROOT)));
        }
        if (values.size() == 1) {
            target.put("type", values.getFirst());
        } else {
            target.put("type", new ArrayValue(values));
        }
    }

    private void putSchemaMap(
            ObjectValueBuilder target,
            String field,
            Map<String, Schema> schemas,
            EncodeContext context) {
        if (schemas == null || schemas.isEmpty()) {
            return;
        }

        target.put(
                field,
                ObjectValueEncoder.encodeObjects(
                        schemas,
                        context,
                        this::encode));
    }

    private void putSchemaList(
            ObjectValueBuilder target,
            String field,
            List<Schema> schemas,
            EncodeContext context) {
        if (schemas == null || schemas.isEmpty()) {
            return;
        }

        target.put(
                field,
                ArrayValueEncoder.encodeObjects(
                        schemas,
                        context,
                        this::encode));
    }

    private void putSchema(
            ObjectValueBuilder target,
            String field,
            Schema schema,
            EncodeContext context) {
        if (schema != null) {
            target.put(field, encode(schema, context));
        }
    }

    private static void putStringSet(
            ObjectValueBuilder target,
            String field,
            Set<String> values) {
        if (values == null || values.isEmpty()) {
            return;
        }

        ArrayList<DocumentValue> encodedValues =
                new ArrayList<>(values.size());
        for (String value : values) {
            encodedValues.add(new StringValue(value));
        }
        target.put(field, new ArrayValue(encodedValues));
    }

    private static void putValue(
            ObjectValueBuilder target,
            String field,
            DocumentValue value) {
        if (value != null) {
            target.put(field, value);
        }
    }
}
