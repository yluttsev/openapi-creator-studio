package ru.luttsev.studio.openapi.version.v31.decode;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import ru.luttsev.studio.core.model.schema.Discriminator;
import ru.luttsev.studio.core.model.schema.JsonType;
import ru.luttsev.studio.core.model.schema.LogicalSchema;
import ru.luttsev.studio.core.model.schema.Schema;
import ru.luttsev.studio.core.model.schema.SchemaDefinition;
import ru.luttsev.studio.core.model.schema.SchemaFormat;
import ru.luttsev.studio.core.model.value.ArrayValue;
import ru.luttsev.studio.core.model.value.BooleanValue;
import ru.luttsev.studio.core.model.value.DocumentValue;
import ru.luttsev.studio.core.model.value.ObjectValue;
import ru.luttsev.studio.core.model.value.StringValue;
import ru.luttsev.studio.openapi.diagnostic.OpenApiDiagnosticCodes;

final class SchemaDecoder {

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

    private final DiscriminatorDecoder discriminatorDecoder =
            new DiscriminatorDecoder();

    Schema decode(DocumentValue source, DecodeContext context) {
        if (source instanceof BooleanValue booleanValue) {
            return new LogicalSchema(booleanValue.value());
        }
        if (source instanceof ObjectValue objectValue) {
            return decodeDefinition(objectValue, context);
        }

        context.error(
                OpenApiDiagnosticCodes.MAPPING_TYPE_MISMATCH,
                "Expected boolean or object but found "
                        + ObjectValueReader.typeOf(source));
        return null;
    }

    private SchemaDefinition decodeDefinition(
            ObjectValue source,
            DecodeContext context) {
        ObjectValueReader reader = new ObjectValueReader(source, context);
        SchemaDefinition schema = new SchemaDefinition();

        schema.setTypes(decodeTypes(reader.optionalValue("type"), context.child("type")));
        String format = reader.optionalString("format");
        schema.setFormat(format == null ? null : new SchemaFormat(format));
        schema.setRef(reader.optionalUriReference("$ref"));
        schema.setTitle(reader.optionalString("title"));
        schema.setDescription(reader.optionalString("description"));
        schema.setDefaultValue(reader.optionalValue("default"));
        schema.setExamples(decodeExamples(reader));
        schema.setEnumValues(decodeValues(reader.optionalArray("enum")));
        schema.setConstValue(reader.optionalValue("const"));
        schema.setDeprecated(reader.optionalBoolean("deprecated"));
        schema.setReadOnly(reader.optionalBoolean("readOnly"));
        schema.setWriteOnly(reader.optionalBoolean("writeOnly"));

        schema.setProperties(decodeSchemaMap(
                reader.optionalObject("properties"),
                context.child("properties")));
        schema.setRequired(decodeStringSet(
                reader.optionalArray("required"),
                context.child("required")));
        schema.setAdditionalProperties(decodeOptionalSchema(
                reader.optionalValue("additionalProperties"),
                context.child("additionalProperties")));
        schema.setMinProperties(reader.optionalNonNegativeInteger("minProperties"));
        schema.setMaxProperties(reader.optionalNonNegativeInteger("maxProperties"));

        schema.setItems(decodeOptionalSchema(
                reader.optionalValue("items"),
                context.child("items")));
        schema.setMinItems(reader.optionalNonNegativeInteger("minItems"));
        schema.setMaxItems(reader.optionalNonNegativeInteger("maxItems"));
        schema.setUniqueItems(reader.optionalBoolean("uniqueItems"));

        schema.setMinLength(reader.optionalNonNegativeInteger("minLength"));
        schema.setMaxLength(reader.optionalNonNegativeInteger("maxLength"));
        schema.setPattern(reader.optionalString("pattern"));

        schema.setMinimum(reader.optionalNumber("minimum"));
        schema.setMaximum(reader.optionalNumber("maximum"));
        schema.setExclusiveMinimum(reader.optionalNumber("exclusiveMinimum"));
        schema.setExclusiveMaximum(reader.optionalNumber("exclusiveMaximum"));
        schema.setMultipleOf(reader.optionalNumber("multipleOf"));

        schema.setAllOf(decodeSchemaList(
                reader.optionalArray("allOf"),
                context.child("allOf")));
        schema.setAnyOf(decodeSchemaList(
                reader.optionalArray("anyOf"),
                context.child("anyOf")));
        schema.setOneOf(decodeSchemaList(
                reader.optionalArray("oneOf"),
                context.child("oneOf")));
        schema.setNotSchema(decodeOptionalSchema(
                reader.optionalValue("not"),
                context.child("not")));
        schema.setDiscriminator(decodeDiscriminator(reader, context));

        copyAdditionalKeywords(source, schema);
        return schema;
    }

    private Discriminator decodeDiscriminator(
            ObjectValueReader reader,
            DecodeContext context) {
        ObjectValue source = reader.optionalObject("discriminator");
        return source == null
                ? null
                : discriminatorDecoder.decode(
                        source,
                        context.child("discriminator"));
    }

    private Set<JsonType> decodeTypes(
            DocumentValue source,
            DecodeContext context) {
        LinkedHashSet<JsonType> types = new LinkedHashSet<>();
        if (source == null) {
            return types;
        }
        if (source instanceof StringValue stringValue) {
            addType(stringValue, context, types);
            return types;
        }
        if (source instanceof ArrayValue arrayValue) {
            for (int index = 0; index < arrayValue.values().size(); index++) {
                DocumentValue value = arrayValue.values().get(index);
                DecodeContext itemContext = context.child(Integer.toString(index));
                if (value instanceof StringValue stringValue) {
                    addType(stringValue, itemContext, types);
                } else {
                    itemContext.error(
                            OpenApiDiagnosticCodes.MAPPING_TYPE_MISMATCH,
                            "Expected string but found "
                                    + ObjectValueReader.typeOf(value));
                }
            }
            return types;
        }

        context.error(
                OpenApiDiagnosticCodes.MAPPING_TYPE_MISMATCH,
                "Expected string or array but found "
                        + ObjectValueReader.typeOf(source));
        return types;
    }

    private static void addType(
            StringValue source,
            DecodeContext context,
            Set<JsonType> target) {
        try {
            target.add(JsonType.valueOf(
                    source.value().toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException exception) {
            context.error(
                    OpenApiDiagnosticCodes.MAPPING_INVALID_VALUE,
                    "Unsupported JSON type '" + source.value() + "'");
        }
    }

    private Map<String, Schema> decodeSchemaMap(
            ObjectValue source,
            DecodeContext context) {
        return ObjectValueMapper.mapValues(source, context, this::decode);
    }

    private List<Schema> decodeSchemaList(
            ArrayValue source,
            DecodeContext context) {
        return ArrayValueMapper.mapValues(source, context, this::decode);
    }

    private Schema decodeOptionalSchema(
            DocumentValue source,
            DecodeContext context) {
        return source == null ? null : decode(source, context);
    }

    private static Set<String> decodeStringSet(
            ArrayValue source,
            DecodeContext context) {
        return new LinkedHashSet<>(
                ArrayValueMapper.mapStrings(source, context));
    }

    private static List<DocumentValue> decodeExamples(
            ObjectValueReader reader) {
        ArrayList<DocumentValue> examples = new ArrayList<>();
        DocumentValue example = reader.optionalValue("example");
        if (example != null) {
            examples.add(example);
        }

        ArrayValue source = reader.optionalArray("examples");
        if (source != null) {
            examples.addAll(source.values());
        }
        return examples;
    }

    private static List<DocumentValue> decodeValues(ArrayValue source) {
        return source == null ? new ArrayList<>() : new ArrayList<>(source.values());
    }

    private static void copyAdditionalKeywords(
            ObjectValue source,
            SchemaDefinition target) {
        for (Map.Entry<String, DocumentValue> entry : source.values().entrySet()) {
            if (!MAPPED_KEYWORDS.contains(entry.getKey())) {
                target.getAdditionalKeywords().put(
                        entry.getKey(),
                        entry.getValue());
            }
        }
    }
}
