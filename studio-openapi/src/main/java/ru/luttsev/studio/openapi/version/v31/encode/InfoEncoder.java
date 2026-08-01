package ru.luttsev.studio.openapi.version.v31.encode;

import java.util.Set;
import ru.luttsev.studio.core.model.info.Info;
import ru.luttsev.studio.core.model.value.ObjectValue;

final class InfoEncoder {

    private static final Set<String> MAPPED_FIELDS = Set.of(
            "title",
            "summary",
            "description",
            "termsOfService",
            "contact",
            "license",
            "version");

    private final ContactEncoder contactEncoder = new ContactEncoder();
    private final LicenseEncoder licenseEncoder = new LicenseEncoder();

    ObjectValue encode(Info source, EncodeContext context) {
        ObjectValueBuilder target = new ObjectValueBuilder()
                .putString("title", source.getTitle())
                .putString("summary", source.getSummary())
                .putString("description", source.getDescription());
        if (source.getTermsOfService() != null) {
            target.putString(
                    "termsOfService",
                    source.getTermsOfService().value());
        }
        if (source.getContact() != null) {
            target.put(
                    "contact",
                    contactEncoder.encode(
                            source.getContact(),
                            context.child("contact")));
        }
        if (source.getLicense() != null) {
            target.put(
                    "license",
                    licenseEncoder.encode(
                            source.getLicense(),
                            context.child("license")));
        }
        target.putString("version", source.getVersion());
        AdditionalFieldsEncoder.copy(
                source,
                target,
                MAPPED_FIELDS,
                context);
        return target.build();
    }
}
