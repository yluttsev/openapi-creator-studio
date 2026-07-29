package ru.luttsev.studio.core.model.reference;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.luttsev.studio.core.model.ExtensibleObject;
import ru.luttsev.studio.core.model.schema.UriReference;

@Getter
@Setter
@NoArgsConstructor
public final class ReferenceObject<T> extends ExtensibleObject
        implements ReferenceOr<T>, ReferenceHolder {

    private UriReference ref;
    private String summary;
    private String description;
}
