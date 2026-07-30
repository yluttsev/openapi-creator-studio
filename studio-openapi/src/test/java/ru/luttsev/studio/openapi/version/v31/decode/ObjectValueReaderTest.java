package ru.luttsev.studio.openapi.version.v31.decode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import ru.luttsev.studio.core.model.OpenApiVersion;
import ru.luttsev.studio.core.model.value.ArrayValue;
import ru.luttsev.studio.core.model.value.BooleanValue;
import ru.luttsev.studio.core.model.value.NumberValue;
import ru.luttsev.studio.core.model.value.ObjectValue;
import ru.luttsev.studio.core.model.value.StringValue;
import ru.luttsev.studio.openapi.diagnostic.OpenApiDiagnostic;
import ru.luttsev.studio.openapi.diagnostic.OpenApiDiagnosticCodes;

class ObjectValueReaderTest {

    @Test
    void readsSupportedValueTypes() {
        ObjectValue nested = new ObjectValue(Map.of());
        ArrayValue array = new ArrayValue(List.of(new StringValue("value")));
        ObjectValue source = new ObjectValue(Map.of(
                "string", new StringValue("text"),
                "boolean", new BooleanValue(true),
                "number", new NumberValue(new BigDecimal("12.5")),
                "object", nested,
                "array", array));
        DecodeContext context = DecodeContext.root(OpenApiVersion.V3_1_2);
        ObjectValueReader reader = new ObjectValueReader(source, context);

        assertEquals("text", reader.requiredString("string"));
        assertEquals(true, reader.optionalBoolean("boolean"));
        assertEquals(
                new BigDecimal("12.5"),
                reader.optionalNumber("number"));
        assertSame(nested, reader.requiredObject("object"));
        assertSame(array, reader.optionalArray("array"));
        assertSame(array, reader.optionalValue("array"));
        assertTrue(context.diagnostics().isEmpty());
    }

    @Test
    void reportsMissingAndMismatchedValuesAtExactPaths() {
        ObjectValue source = new ObjectValue(Map.of(
                "title",
                new BooleanValue(true)));
        DecodeContext context = DecodeContext.root(OpenApiVersion.V3_1_2)
                .child("info");
        ObjectValueReader reader = new ObjectValueReader(source, context);

        assertNull(reader.requiredString("version"));
        assertNull(reader.optionalString("title"));

        List<OpenApiDiagnostic> diagnostics = context.diagnostics();
        assertEquals(2, diagnostics.size());
        assertEquals(
                OpenApiDiagnosticCodes.MAPPING_MISSING_REQUIRED_FIELD,
                diagnostics.get(0).code());
        assertEquals("/info/version", diagnostics.get(0).path().toPointer());
        assertEquals(
                OpenApiDiagnosticCodes.MAPPING_TYPE_MISMATCH,
                diagnostics.get(1).code());
        assertEquals("/info/title", diagnostics.get(1).path().toPointer());
    }
}
