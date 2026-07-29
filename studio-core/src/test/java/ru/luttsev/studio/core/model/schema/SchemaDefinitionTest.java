package ru.luttsev.studio.core.model.schema;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import org.junit.jupiter.api.Test;

class SchemaDefinitionTest {

    @Test
    void initializesCollectionsAsEmpty() {
        var schema = new SchemaDefinition();

        assertTrue(schema.getTypes().isEmpty());
        assertTrue(schema.getExamples().isEmpty());
        assertTrue(schema.getEnumValues().isEmpty());
        assertTrue(schema.getProperties().isEmpty());
        assertTrue(schema.getRequired().isEmpty());
        assertTrue(schema.getAllOf().isEmpty());
        assertTrue(schema.getAnyOf().isEmpty());
        assertTrue(schema.getOneOf().isEmpty());
        assertTrue(schema.getAdditionalKeywords().isEmpty());
    }

    @Test
    void buildsNestedObjectSchema() {
        var nameSchema = new SchemaDefinition();
        nameSchema.setTypes(Set.of(JsonType.STRING));

        var userSchema = new SchemaDefinition();
        userSchema.setTypes(Set.of(JsonType.OBJECT));
        userSchema.getProperties().put("name", nameSchema);
        userSchema.getRequired().add("name");

        assertEquals(Set.of(JsonType.OBJECT), userSchema.getTypes());
        assertSame(nameSchema, userSchema.getProperties().get("name"));
        assertEquals(Set.of("name"), userSchema.getRequired());
    }

    @Test
    void representsLogicalSchema() {
        Schema schema = new LogicalSchema(false);

        assertTrue(schema instanceof LogicalSchema);
        assertFalse(((LogicalSchema) schema).isValue());
    }
}
