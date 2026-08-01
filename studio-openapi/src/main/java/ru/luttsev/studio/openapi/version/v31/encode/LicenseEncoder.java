package ru.luttsev.studio.openapi.version.v31.encode;

import java.util.Set;
import ru.luttsev.studio.core.model.info.License;
import ru.luttsev.studio.core.model.value.ObjectValue;

final class LicenseEncoder {

    private static final Set<String> MAPPED_FIELDS =
            Set.of("name", "identifier", "url");

    ObjectValue encode(License source, EncodeContext context) {
        ObjectValueBuilder target = new ObjectValueBuilder()
                .putString("name", source.getName())
                .putString("identifier", source.getIdentifier());
        if (source.getUrl() != null) {
            target.putString("url", source.getUrl().value());
        }
        AdditionalFieldsEncoder.copy(
                source,
                target,
                MAPPED_FIELDS,
                context);
        return target.build();
    }
}
