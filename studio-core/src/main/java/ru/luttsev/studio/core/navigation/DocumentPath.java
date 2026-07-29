package ru.luttsev.studio.core.navigation;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class DocumentPath {

    private static final DocumentPath ROOT = new DocumentPath(List.of());

    private final List<String> segments;

    private DocumentPath(List<String> segments) {
        this.segments = List.copyOf(segments);
    }

    public static DocumentPath root() {
        return ROOT;
    }

    public static DocumentPath parse(String pointer) {
        Objects.requireNonNull(pointer, "pointer must not be null");
        if (pointer.isEmpty()) {
            return root();
        }
        if (pointer.charAt(0) != '/') {
            throw new IllegalArgumentException("JSON Pointer must be empty or start with '/'");
        }

        String[] encodedSegments = pointer.substring(1).split("/", -1);
        ArrayList<String> decodedSegments = new ArrayList<>(encodedSegments.length);
        for (String encodedSegment : encodedSegments) {
            decodedSegments.add(decodeSegment(encodedSegment));
        }
        return new DocumentPath(decodedSegments);
    }

    public static DocumentPath parseFragment(String fragment) {
        Objects.requireNonNull(fragment, "fragment must not be null");
        if (!fragment.startsWith("#")) {
            throw new IllegalArgumentException("JSON Pointer fragment must start with '#'");
        }

        try {
            URI uri = new URI(fragment);
            return parse(uri.getFragment());
        } catch (URISyntaxException exception) {
            throw new IllegalArgumentException("Invalid JSON Pointer fragment", exception);
        }
    }

    public List<String> segments() {
        return segments;
    }

    public boolean isRoot() {
        return segments.isEmpty();
    }

    public DocumentPath child(String segment) {
        Objects.requireNonNull(segment, "segment must not be null");
        ArrayList<String> childSegments = new ArrayList<>(segments);
        childSegments.add(segment);
        return new DocumentPath(childSegments);
    }

    public Optional<DocumentPath> parent() {
        if (isRoot()) {
            return Optional.empty();
        }
        if (segments.size() == 1) {
            return Optional.of(root());
        }
        return Optional.of(new DocumentPath(segments.subList(0, segments.size() - 1)));
    }

    public boolean startsWith(DocumentPath other) {
        Objects.requireNonNull(other, "other must not be null");
        return segments.size() >= other.segments.size()
                && segments.subList(0, other.segments.size()).equals(other.segments);
    }

    public DocumentPath replacePrefix(
            DocumentPath currentPrefix,
            DocumentPath newPrefix) {
        Objects.requireNonNull(currentPrefix, "currentPrefix must not be null");
        Objects.requireNonNull(newPrefix, "newPrefix must not be null");
        if (!startsWith(currentPrefix)) {
            throw new IllegalArgumentException(
                    "path must start with currentPrefix");
        }

        DocumentPath result = newPrefix;
        for (int index = currentPrefix.segments.size();
                index < segments.size();
                index++) {
            result = result.child(segments.get(index));
        }
        return result;
    }

    public String toPointer() {
        if (isRoot()) {
            return "";
        }

        StringBuilder pointer = new StringBuilder();
        for (String segment : segments) {
            pointer.append('/').append(encodeSegment(segment));
        }
        return pointer.toString();
    }

    public String toFragment() {
        try {
            return new URI(null, null, toPointer()).toASCIIString();
        } catch (URISyntaxException exception) {
            throw new IllegalStateException("Document path cannot be represented as a URI fragment", exception);
        }
    }

    @Override
    public boolean equals(Object other) {
        return this == other
                || other instanceof DocumentPath path
                        && segments.equals(path.segments);
    }

    @Override
    public int hashCode() {
        return segments.hashCode();
    }

    @Override
    public String toString() {
        return toPointer();
    }

    private static String encodeSegment(String segment) {
        return segment.replace("~", "~0").replace("/", "~1");
    }

    private static String decodeSegment(String segment) {
        StringBuilder decoded = new StringBuilder(segment.length());
        for (int index = 0; index < segment.length(); index++) {
            char character = segment.charAt(index);
            if (character != '~') {
                decoded.append(character);
                continue;
            }

            if (++index >= segment.length()) {
                throw new IllegalArgumentException("Invalid '~' escape in JSON Pointer segment");
            }

            char escapedCharacter = segment.charAt(index);
            if (escapedCharacter == '0') {
                decoded.append('~');
            } else if (escapedCharacter == '1') {
                decoded.append('/');
            } else {
                throw new IllegalArgumentException(
                        "Invalid '~" + escapedCharacter + "' escape in JSON Pointer segment");
            }
        }
        return decoded.toString();
    }
}
