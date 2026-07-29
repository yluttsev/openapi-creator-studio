package ru.luttsev.studio.core.model.info;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.luttsev.studio.core.model.ExtensibleObject;
import ru.luttsev.studio.core.model.schema.UriReference;

@Getter
@Setter
@NoArgsConstructor
public final class Info extends ExtensibleObject {

    private String title;
    private String summary;
    private String description;
    private UriReference termsOfService;
    private Contact contact;
    private License license;
    private String version;
}
