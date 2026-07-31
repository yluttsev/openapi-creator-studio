package ru.luttsev.studio.openapi.version.v31.encode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import ru.luttsev.studio.core.model.OpenApiVersion;
import ru.luttsev.studio.core.model.info.Info;
import ru.luttsev.studio.core.model.value.BooleanValue;
import ru.luttsev.studio.core.model.value.DocumentValue;
import ru.luttsev.studio.core.model.value.NumberValue;
import ru.luttsev.studio.core.model.value.ObjectValue;
import ru.luttsev.studio.core.model.value.StringValue;
import ru.luttsev.studio.openapi.diagnostic.OpenApiDiagnostic;
import ru.luttsev.studio.openapi.diagnostic.OpenApiDiagnosticCodes;

class ObjectValueBuilderTest {

    @Test
    void writesSupportedValuesInInsertionOrderAndSkipsNulls() {
        ObjectValue nested = new ObjectValue(Map.of());

        ObjectValue value = new ObjectValueBuilder()
                .putString("name", "studio")
                .putBoolean("enabled", true)
                .putNumber("weight", new BigDecimal("12.5"))
                .put("nested", nested)
                .putString("omitted", null)
                .build();

        assertEquals(
                List.of("name", "enabled", "weight", "nested"),
                new ArrayList<>(value.values().keySet()));
        assertEquals(new StringValue("studio"), value.values().get("name"));
        assertEquals(new BooleanValue(true), value.values().get("enabled"));
        assertEquals(
                new NumberValue(new BigDecimal("12.5")),
                value.values().get("weight"));
        assertSame(nested, value.values().get("nested"));
    }

    @Test
    void rejectsWritingTheSameFieldTwice() {
        ObjectValueBuilder builder = new ObjectValueBuilder()
                .putString("name", "first");

        assertThrows(
                IllegalStateException.class,
                () -> builder.putString("name", "second"));
    }

    @Test
    void rejectsNullRawDocumentValues() {
        ObjectValueBuilder builder = new ObjectValueBuilder();

        assertThrows(
                NullPointerException.class,
                () -> builder.put("invalid", null));
    }

    @Test
    void preservesAdditionalFieldsAndReportsMappedFieldConflicts() {
        DocumentValue extension = new ObjectValue(Map.of(
                "enabled",
                new BooleanValue(true)));
        Info source = new Info();
        source.getAdditionalFields().put("x-studio", extension);
        source.getAdditionalFields().put(
                "title",
                new StringValue("stale title"));
        ObjectValueBuilder builder = new ObjectValueBuilder()
                .putString("title", "Typed title");
        EncodeContext context = EncodeContext.root(OpenApiVersion.V3_1_2)
                .child("info");

        AdditionalFieldsEncoder.copy(
                source,
                builder,
                Set.of("title", "version"),
                context);

        ObjectValue value = builder.build();
        assertEquals(new StringValue("Typed title"), value.values().get("title"));
        assertSame(extension, value.values().get("x-studio"));
        assertEquals(2, value.values().size());

        List<OpenApiDiagnostic> diagnostics = context.diagnostics();
        assertEquals(1, diagnostics.size());
        assertEquals(
                OpenApiDiagnosticCodes.VERSION_FIELD_CONFLICT,
                diagnostics.getFirst().code());
        assertEquals(
                "/info/title",
                diagnostics.getFirst().path().toPointer());
        assertTrue(context.hasErrors());
    }
}
