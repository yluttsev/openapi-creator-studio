package ru.luttsev.studio.openapi.version.v31.decode;

import java.util.Set;
import ru.luttsev.studio.core.model.info.License;
import ru.luttsev.studio.core.model.value.ObjectValue;

final class LicenseDecoder {

    private static final Set<String> MAPPED_FIELDS =
            Set.of("name", "identifier", "url");

    License decode(ObjectValue source, DecodeContext context) {
        ObjectValueReader reader = new ObjectValueReader(source, context);
        License license = new License();
        license.setName(reader.requiredString("name"));
        license.setIdentifier(reader.optionalString("identifier"));
        license.setUrl(reader.optionalUriReference("url"));
        AdditionalFieldsMapper.copy(source, license, MAPPED_FIELDS);
        return license;
    }
}
