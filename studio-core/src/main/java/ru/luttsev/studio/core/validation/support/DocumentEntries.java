package ru.luttsev.studio.core.validation.support;

import java.util.List;
import ru.luttsev.studio.core.navigation.DocumentEntry;
import ru.luttsev.studio.core.validation.ValidationContext;

public final class DocumentEntries {

    private DocumentEntries() {
    }

    public static <T> List<DocumentEntry> ofType(
            ValidationContext context,
            Class<T> type) {
        return context.navigator()
                .walk(context.document())
                .filter(entry -> type.isInstance(entry.value()))
                .toList();
    }
}
