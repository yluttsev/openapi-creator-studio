package ru.luttsev.studio.core.model.value;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class DocumentValueTest {

    @Test
    void createsNestedDocumentValue() {
        var roles = new ArrayValue(List.of(
                new StringValue("USER"),
                new StringValue("ADMIN")
        ));
        var user = new ObjectValue(Map.of(
                "name", new StringValue("Ivan"),
                "age", new NumberValue(new BigDecimal("25")),
                "active", new BooleanValue(true),
                "roles", roles,
                "middleName", NullValue.INSTANCE
        ));

        assertEquals(new StringValue("Ivan"), user.values().get("name"));
        assertEquals(roles, user.values().get("roles"));
        assertSame(NullValue.INSTANCE, user.values().get("middleName"));
    }

    @Test
    void protectsArrayAndObjectValuesFromMutation() {
        var sourceList = new ArrayList<DocumentValue>();
        sourceList.add(new StringValue("first"));
        var arrayValue = new ArrayValue(sourceList);

        var sourceMap = new LinkedHashMap<String, DocumentValue>();
        sourceMap.put("items", arrayValue);
        var objectValue = new ObjectValue(sourceMap);

        sourceList.add(new StringValue("second"));
        sourceMap.clear();

        assertEquals(1, arrayValue.values().size());
        assertEquals(List.of("items"), objectValue.values().keySet().stream().toList());
        assertThrows(
                UnsupportedOperationException.class,
                () -> objectValue.values().put("other", NullValue.INSTANCE)
        );
    }
}
