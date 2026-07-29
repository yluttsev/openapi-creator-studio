package ru.luttsev.studio.core.command.support;

import java.util.List;
import java.util.Objects;
import ru.luttsev.studio.core.navigation.DocumentPath;
import ru.luttsev.studio.core.reference.index.ReferenceIndex;
import ru.luttsev.studio.core.reference.index.ResolvedReferenceUsage;

public final class ReferenceUsageSupport {

    private ReferenceUsageSupport() {
    }

    public static List<ResolvedReferenceUsage> blockingUsages(
            ReferenceIndex index,
            DocumentPath removedPath) {
        Objects.requireNonNull(index, "index must not be null");
        Objects.requireNonNull(removedPath, "removedPath must not be null");
        return index.usagesOfSubtree(removedPath).stream()
                .filter(usage -> !usage.sourcePath().startsWith(removedPath))
                .toList();
    }

    public static List<DocumentPath> sourcePaths(
            List<ResolvedReferenceUsage> usages) {
        Objects.requireNonNull(usages, "usages must not be null");
        return usages.stream()
                .map(ResolvedReferenceUsage::sourcePath)
                .toList();
    }
}
