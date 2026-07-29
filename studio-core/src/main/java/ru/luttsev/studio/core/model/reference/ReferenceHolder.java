package ru.luttsev.studio.core.model.reference;

import ru.luttsev.studio.core.model.schema.UriReference;

public interface ReferenceHolder {

    UriReference getRef();

    void setRef(UriReference reference);
}
