package ru.luttsev.studio.openapi.version.v31.encode;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import ru.luttsev.studio.core.model.path.PathItem;
import ru.luttsev.studio.core.model.path.Paths;
import ru.luttsev.studio.core.model.value.ObjectValue;

final class PathsEncoder {

    private final PathItemEncoder pathItemEncoder = new PathItemEncoder(
            new PayloadEncoder(new SchemaEncoder()));

    ObjectValue encode(Paths source, EncodeContext context) {
        ObjectValueBuilder target = new ObjectValueBuilder();
        Set<String> mappedFields = new LinkedHashSet<>();
        for (Map.Entry<String, PathItem> entry : source.getItems().entrySet()) {
            mappedFields.add(entry.getKey());
            target.put(
                    entry.getKey(),
                    pathItemEncoder.encode(
                            entry.getValue(),
                            context.child(entry.getKey())));
        }
        AdditionalFieldsEncoder.copy(
                source,
                target,
                mappedFields,
                context);
        return target.build();
    }
}
