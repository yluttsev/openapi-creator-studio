package ru.luttsev.studio.openapi.version.v31.encode;

import java.util.Set;
import ru.luttsev.studio.core.model.info.Contact;
import ru.luttsev.studio.core.model.value.ObjectValue;

final class ContactEncoder {

    private static final Set<String> MAPPED_FIELDS =
            Set.of("name", "url", "email");

    ObjectValue encode(Contact source, EncodeContext context) {
        ObjectValueBuilder target = new ObjectValueBuilder()
                .putString("name", source.getName());
        if (source.getUrl() != null) {
            target.putString("url", source.getUrl().value());
        }
        target.putString("email", source.getEmail());
        AdditionalFieldsEncoder.copy(
                source,
                target,
                MAPPED_FIELDS,
                context);
        return target.build();
    }
}
