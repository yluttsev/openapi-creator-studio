package ru.luttsev.studio.core.model.info;

import ru.luttsev.studio.core.model.ExtensibleObject;
import ru.luttsev.studio.core.model.schema.UriReference;

public final class Contact extends ExtensibleObject {

    private String name;
    private UriReference url;
    private String email;

    public String getName() {
        return this.name;
    }

    public UriReference getUrl() {
        return this.url;
    }

    public String getEmail() {
        return this.email;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUrl(UriReference url) {
        this.url = url;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Contact() {
    }
}
