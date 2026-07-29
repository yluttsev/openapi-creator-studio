package ru.luttsev.studio.core.reference.index;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import ru.luttsev.studio.core.navigation.DocumentPath;

public final class ReferenceIndex {

    private final List<ReferenceUsage> references;
    private final List<ResolvedReferenceUsage> resolvedReferences;
    private final List<UnresolvedReferenceUsage> unresolvedReferences;
    private final Map<DocumentPath, ReferenceUsage> bySourcePath;
    private final Map<DocumentPath, List<ResolvedReferenceUsage>> byTargetPath;

    ReferenceIndex(List<ReferenceUsage> references) {
        this.references = List.copyOf(references);

        ArrayList<ResolvedReferenceUsage> resolved = new ArrayList<>();
        ArrayList<UnresolvedReferenceUsage> unresolved = new ArrayList<>();
        LinkedHashMap<DocumentPath, ReferenceUsage> referencesBySource =
                new LinkedHashMap<>();
        LinkedHashMap<DocumentPath, List<ResolvedReferenceUsage>> referencesByTarget =
                new LinkedHashMap<>();

        for (ReferenceUsage reference : references) {
            referencesBySource.put(reference.sourcePath(), reference);
            if (reference instanceof ResolvedReferenceUsage resolvedReference) {
                resolved.add(resolvedReference);
                referencesByTarget
                        .computeIfAbsent(
                                resolvedReference.targetPath(),
                                ignored -> new ArrayList<>())
                        .add(resolvedReference);
            } else {
                unresolved.add((UnresolvedReferenceUsage) reference);
            }
        }

        resolvedReferences = List.copyOf(resolved);
        unresolvedReferences = List.copyOf(unresolved);
        bySourcePath = Map.copyOf(referencesBySource);
        byTargetPath = immutableLists(referencesByTarget);
    }

    public List<ReferenceUsage> references() {
        return references;
    }

    public List<ResolvedReferenceUsage> resolvedReferences() {
        return resolvedReferences;
    }

    public List<UnresolvedReferenceUsage> unresolvedReferences() {
        return unresolvedReferences;
    }

    public Optional<ReferenceUsage> findBySource(DocumentPath sourcePath) {
        Objects.requireNonNull(sourcePath, "sourcePath must not be null");
        return Optional.ofNullable(bySourcePath.get(sourcePath));
    }

    public List<ResolvedReferenceUsage> usagesOf(DocumentPath targetPath) {
        Objects.requireNonNull(targetPath, "targetPath must not be null");
        return byTargetPath.getOrDefault(targetPath, List.of());
    }

    public List<ReferenceUsage> referencesFrom(DocumentPath sourcePath) {
        Objects.requireNonNull(sourcePath, "sourcePath must not be null");
        return references.stream()
                .filter(reference -> reference.sourcePath().startsWith(sourcePath))
                .toList();
    }

    public boolean isReferenced(DocumentPath targetPath) {
        return !usagesOf(targetPath).isEmpty();
    }

    private static Map<DocumentPath, List<ResolvedReferenceUsage>> immutableLists(
            Map<DocumentPath, List<ResolvedReferenceUsage>> source) {
        LinkedHashMap<DocumentPath, List<ResolvedReferenceUsage>> result =
                new LinkedHashMap<>();
        for (Map.Entry<DocumentPath, List<ResolvedReferenceUsage>> entry :
                source.entrySet()) {
            result.put(entry.getKey(), List.copyOf(entry.getValue()));
        }
        return Map.copyOf(result);
    }
}
