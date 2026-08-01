package ru.luttsev.studio.core.model.info;

import ru.luttsev.studio.core.model.ExtensibleObject;
import ru.luttsev.studio.core.model.schema.UriReference;

public final class Info extends ExtensibleObject {

    private String title;
    private String summary;
    private String description;
    private UriReference termsOfService;
    private Contact contact;
    private License license;
    private String version;

    public String getTitle() {
        return this.title;
    }

    public String getSummary() {
        return this.summary;
    }

    public String getDescription() {
        return this.description;
    }

    public UriReference getTermsOfService() {
        return this.termsOfService;
    }

    public Contact getContact() {
        return this.contact;
    }

    public License getLicense() {
        return this.license;
    }

    public String getVersion() {
        return this.version;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setTermsOfService(UriReference termsOfService) {
        this.termsOfService = termsOfService;
    }

    public void setContact(Contact contact) {
        this.contact = contact;
    }

    public void setLicense(License license) {
        this.license = license;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Info() {
    }
}
