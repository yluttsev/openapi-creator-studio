package ru.luttsev.studio.core.reference.index;

import ru.luttsev.studio.core.model.schema.UriReference;
import ru.luttsev.studio.core.navigation.DocumentPath;

public sealed interface ReferenceUsage
        permits ResolvedReferenceUsage, UnresolvedReferenceUsage {

    DocumentPath sourcePath();

    UriReference reference();
}
