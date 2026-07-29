package ru.luttsev.studio.core.command.component;

import java.util.List;
import java.util.Optional;
import ru.luttsev.studio.core.navigation.DocumentPath;

record ComponentAddress(String section, String name) {

    static Optional<ComponentAddress> from(DocumentPath path) {
        List<String> segments = path.segments();
        if (segments.size() != 3 || !"components".equals(segments.getFirst())) {
            return Optional.empty();
        }
        return Optional.of(new ComponentAddress(
                segments.get(1),
                segments.get(2)));
    }

    DocumentPath path() {
        return DocumentPath.root()
                .child("components")
                .child(section)
                .child(name);
    }

    DocumentPath withName(String newName) {
        return DocumentPath.root()
                .child("components")
                .child(section)
                .child(newName);
    }
}
