package ru.luttsev.studio.openapi.version.v31.encode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import org.junit.jupiter.api.Test;
import ru.luttsev.studio.core.model.OpenApiVersion;
import ru.luttsev.studio.core.model.value.ArrayValue;
import ru.luttsev.studio.core.model.value.StringValue;
import ru.luttsev.studio.openapi.diagnostic.DiagnosticPhase;
import ru.luttsev.studio.openapi.diagnostic.OpenApiDiagnostic;
import ru.luttsev.studio.openapi.diagnostic.OpenApiDiagnosticCodes;

class ArrayValueEncoderTest {

    @Test
    void preservesOrderAndReportsNullElementsAtTheirIndex() {
        EncodeContext context = EncodeContext.root(OpenApiVersion.V3_1_2)
                .child("items");

        ArrayValue encoded = ArrayValueEncoder.encodeObjects(
                Arrays.asList("first", null, "third"),
                context,
                (value, itemContext) -> new StringValue(value));

        assertEquals(
                Arrays.asList(
                        new StringValue("first"),
                        new StringValue("third")),
                encoded.values());
        OpenApiDiagnostic diagnostic = context.diagnostics().getFirst();
        assertEquals(
                OpenApiDiagnosticCodes.MAPPING_INVALID_VALUE,
                diagnostic.code());
        assertEquals(DiagnosticPhase.MAPPING, diagnostic.phase());
        assertEquals("/items/1", diagnostic.path().toPointer());
    }

    @Test
    void encodesNullSourceAsEmptyArray() {
        EncodeContext context = EncodeContext.root(OpenApiVersion.V3_1_2);

        ArrayValue encoded = ArrayValueEncoder.encodeObjects(
                null,
                context,
                (String value, EncodeContext itemContext) ->
                        new StringValue(value));

        assertTrue(encoded.values().isEmpty());
        assertTrue(context.diagnostics().isEmpty());
    }
}
