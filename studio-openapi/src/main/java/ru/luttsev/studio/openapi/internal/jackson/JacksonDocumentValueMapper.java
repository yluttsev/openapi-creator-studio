package ru.luttsev.studio.openapi.internal.jackson;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import ru.luttsev.studio.core.model.value.ArrayValue;
import ru.luttsev.studio.core.model.value.BooleanValue;
import ru.luttsev.studio.core.model.value.DocumentValue;
import ru.luttsev.studio.core.model.value.NullValue;
import ru.luttsev.studio.core.model.value.NumberValue;
import ru.luttsev.studio.core.model.value.ObjectValue;
import ru.luttsev.studio.core.model.value.StringValue;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;

public final class JacksonDocumentValueMapper {

    private static final JsonNodeFactory NODE_FACTORY = JsonNodeFactory.instance;

    public DocumentValue fromJsonNode(JsonNode node) {
        if (node.isObject()) {
            return fromObjectNode(node);
        }
        if (node.isArray()) {
            return fromArrayNode(node);
        }
        if (node.isString()) {
            return new StringValue(node.stringValue());
        }
        if (node.isNumber()) {
            return new NumberValue(node.decimalValue());
        }
        if (node.isBoolean()) {
            return new BooleanValue(node.booleanValue());
        }
        if (node.isNull()) {
            return NullValue.INSTANCE;
        }
        throw new UnsupportedSyntaxValueException(
                "Unsupported syntax value type: " + node.getNodeType());
    }

    public JsonNode toJsonNode(DocumentValue value) {
        return switch (value) {
            case ObjectValue objectValue -> toObjectNode(objectValue);
            case ArrayValue arrayValue -> toArrayNode(arrayValue);
            case StringValue stringValue ->
                    NODE_FACTORY.stringNode(stringValue.value());
            case NumberValue numberValue ->
                    NODE_FACTORY.numberNode(numberValue.value());
            case BooleanValue booleanValue ->
                    NODE_FACTORY.booleanNode(booleanValue.value());
            case NullValue ignored -> NODE_FACTORY.nullNode();
        };
    }

    private ObjectValue fromObjectNode(JsonNode node) {
        LinkedHashMap<String, DocumentValue> values = new LinkedHashMap<>();
        for (Map.Entry<String, JsonNode> property : node.properties()) {
            values.put(property.getKey(), fromJsonNode(property.getValue()));
        }
        return new ObjectValue(values);
    }

    private ArrayValue fromArrayNode(JsonNode node) {
        ArrayList<DocumentValue> values = new ArrayList<>(node.size());
        for (JsonNode element : node) {
            values.add(fromJsonNode(element));
        }
        return new ArrayValue(values);
    }

    private ObjectNode toObjectNode(ObjectValue value) {
        ObjectNode node = NODE_FACTORY.objectNode();
        for (Map.Entry<String, DocumentValue> property
                : value.values().entrySet()) {
            node.set(property.getKey(), toJsonNode(property.getValue()));
        }
        return node;
    }

    private ArrayNode toArrayNode(ArrayValue value) {
        List<DocumentValue> values = value.values();
        ArrayNode node = NODE_FACTORY.arrayNode(values.size());
        for (DocumentValue element : values) {
            node.add(toJsonNode(element));
        }
        return node;
    }
}
