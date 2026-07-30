package ru.luttsev.studio.openapi.version.v31.decode;

import java.util.Set;
import ru.luttsev.studio.core.model.info.Contact;
import ru.luttsev.studio.core.model.info.Info;
import ru.luttsev.studio.core.model.info.License;
import ru.luttsev.studio.core.model.value.ObjectValue;

final class InfoDecoder {

    private static final Set<String> MAPPED_FIELDS = Set.of(
            "title",
            "summary",
            "description",
            "termsOfService",
            "contact",
            "license",
            "version");

    private final ContactDecoder contactDecoder = new ContactDecoder();
    private final LicenseDecoder licenseDecoder = new LicenseDecoder();

    Info decode(ObjectValue source, DecodeContext context) {
        ObjectValueReader reader = new ObjectValueReader(source, context);
        Info info = new Info();
        info.setTitle(reader.requiredString("title"));
        info.setSummary(reader.optionalString("summary"));
        info.setDescription(reader.optionalString("description"));
        info.setTermsOfService(reader.optionalUriReference("termsOfService"));
        info.setVersion(reader.requiredString("version"));

        ObjectValue contactSource = reader.optionalObject("contact");
        if (contactSource != null) {
            Contact contact = contactDecoder.decode(
                    contactSource,
                    context.child("contact"));
            info.setContact(contact);
        }

        ObjectValue licenseSource = reader.optionalObject("license");
        if (licenseSource != null) {
            License license = licenseDecoder.decode(
                    licenseSource,
                    context.child("license"));
            info.setLicense(license);
        }

        AdditionalFieldsMapper.copy(source, info, MAPPED_FIELDS);
        return info;
    }
}
