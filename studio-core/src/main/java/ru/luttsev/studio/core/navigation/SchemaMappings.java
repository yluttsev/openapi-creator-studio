package ru.luttsev.studio.core.navigation;

import ru.luttsev.studio.core.model.schema.Discriminator;
import ru.luttsev.studio.core.model.schema.SchemaDefinition;

final class SchemaMappings {

    private SchemaMappings() {
    }

    static void register(DocumentNodeRegistry registry) {
        registry.register(
                SchemaDefinition.class,
                SchemaMappings::collectSchemaDefinition);
        registry.register(
                Discriminator.class,
                SchemaMappings::collectDiscriminator);
    }

    private static void collectSchemaDefinition(
            SchemaDefinition schema,
            ChildrenCollector children) {
        children.add("type", schema.getTypes());
        children.add("format", schema.getFormat());
        children.add("$ref", schema.getRef());
        children.add("title", schema.getTitle());
        children.add("description", schema.getDescription());
        children.add("default", schema.getDefaultValue());
        children.add("examples", schema.getExamples());
        children.add("enum", schema.getEnumValues());
        children.add("const", schema.getConstValue());
        children.add("deprecated", schema.getDeprecated());
        children.add("readOnly", schema.getReadOnly());
        children.add("writeOnly", schema.getWriteOnly());
        children.add("properties", schema.getProperties());
        children.add("required", schema.getRequired());
        children.add("additionalProperties", schema.getAdditionalProperties());
        children.add("minProperties", schema.getMinProperties());
        children.add("maxProperties", schema.getMaxProperties());
        children.add("items", schema.getItems());
        children.add("minItems", schema.getMinItems());
        children.add("maxItems", schema.getMaxItems());
        children.add("uniqueItems", schema.getUniqueItems());
        children.add("minLength", schema.getMinLength());
        children.add("maxLength", schema.getMaxLength());
        children.add("pattern", schema.getPattern());
        children.add("minimum", schema.getMinimum());
        children.add("maximum", schema.getMaximum());
        children.add("exclusiveMinimum", schema.getExclusiveMinimum());
        children.add("exclusiveMaximum", schema.getExclusiveMaximum());
        children.add("multipleOf", schema.getMultipleOf());
        children.add("allOf", schema.getAllOf());
        children.add("anyOf", schema.getAnyOf());
        children.add("oneOf", schema.getOneOf());
        children.add("not", schema.getNotSchema());
        children.add("discriminator", schema.getDiscriminator());
        children.addAdditional(schema.getAdditionalKeywords());
    }

    private static void collectDiscriminator(
            Discriminator discriminator,
            ChildrenCollector children) {
        children.add("propertyName", discriminator.getPropertyName());
        children.add("mapping", discriminator.getMapping());
        children.add("defaultMapping", discriminator.getDefaultMapping());
        children.addAdditional(discriminator.getExtensions());
    }
}
