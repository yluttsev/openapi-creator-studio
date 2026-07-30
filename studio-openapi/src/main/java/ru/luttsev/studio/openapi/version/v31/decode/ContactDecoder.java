package ru.luttsev.studio.openapi.version.v31.decode;

import java.util.Set;
import ru.luttsev.studio.core.model.info.Contact;
import ru.luttsev.studio.core.model.value.ObjectValue;

final class ContactDecoder {

    private static final Set<String> MAPPED_FIELDS =
            Set.of("name", "url", "email");

    Contact decode(ObjectValue source, DecodeContext context) {
        ObjectValueReader reader = new ObjectValueReader(source, context);
        Contact contact = new Contact();
        contact.setName(reader.optionalString("name"));
        contact.setUrl(reader.optionalUriReference("url"));
        contact.setEmail(reader.optionalString("email"));
        AdditionalFieldsMapper.copy(source, contact, MAPPED_FIELDS);
        return contact;
    }
}
