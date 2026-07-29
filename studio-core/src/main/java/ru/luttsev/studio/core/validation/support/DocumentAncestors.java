package ru.luttsev.studio.core.validation.support;

import java.util.Optional;
import ru.luttsev.studio.core.navigation.DocumentPath;
import ru.luttsev.studio.core.validation.ValidationContext;

public final class DocumentAncestors {

    private DocumentAncestors() {
    }

    public static <T> Optional<ResolvedValue<T>> findNearest(
            ValidationContext context,
            DocumentPath path,
            Class<T> type) {
        Optional<DocumentPath> currentPath = path.parent();
        while (currentPath.isPresent()) {
            DocumentPath ancestorPath = currentPath.orElseThrow();
            Optional<T> value = context.navigator()
                    .find(context.document(), ancestorPath, type);
            if (value.isPresent()) {
                return Optional.of(new ResolvedValue<>(
                        value.orElseThrow(),
                        ancestorPath));
            }
            currentPath = ancestorPath.parent();
        }
        return Optional.empty();
    }
}
