package ru.luttsev.studio.openapi.version.v31.encode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import ru.luttsev.studio.core.model.OpenApiVersion;
import ru.luttsev.studio.core.model.value.ObjectValue;
import ru.luttsev.studio.core.model.value.StringValue;
import ru.luttsev.studio.openapi.diagnostic.DiagnosticPhase;
import ru.luttsev.studio.openapi.diagnostic.OpenApiDiagnostic;
import ru.luttsev.studio.openapi.diagnostic.OpenApiDiagnosticCodes;

class ObjectValueEncoderTest {

    @Test
    void preservesOrderAndReportsNullValuesAtTheirKey() {
        LinkedHashMap<String, String> source = new LinkedHashMap<>();
        source.put("first", "one");
        source.put("second", null);
        source.put("third", "three");
        EncodeContext context = EncodeContext.root(OpenApiVersion.V3_1_2)
                .child("map");

        ObjectValue encoded = ObjectValueEncoder.encodeObjects(
                source,
                context,
                (value, itemContext) -> new StringValue(value));

        assertEquals(List.of("first", "third"),
                List.copyOf(encoded.values().keySet()));
        OpenApiDiagnostic diagnostic = context.diagnostics().getFirst();
        assertEquals(
                OpenApiDiagnosticCodes.MAPPING_INVALID_VALUE,
                diagnostic.code());
        assertEquals(DiagnosticPhase.MAPPING, diagnostic.phase());
        assertEquals("/map/second", diagnostic.path().toPointer());
    }

    @Test
    void encodesNullSourceAsEmptyObject() {
        EncodeContext context = EncodeContext.root(OpenApiVersion.V3_1_2);

        ObjectValue encoded = ObjectValueEncoder.encodeObjects(
                null,
                context,
                (String value, EncodeContext itemContext) ->
                        new StringValue(value));

        assertEquals(Map.of(), encoded.values());
        assertTrue(context.diagnostics().isEmpty());
    }
}
