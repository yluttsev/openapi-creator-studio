package ru.luttsev.studio.web.document;

public final class RevisionHeader {

    private RevisionHeader() {
    }

    public static String format(long revision) {
        return '"' + Long.toString(revision) + '"';
    }

    public static long parse(String value) {
        if (value == null
                || value.length() < 3
                || value.charAt(0) != '"'
                || value.charAt(value.length() - 1) != '"') {
            throw new IllegalArgumentException("Invalid If-Match revision");
        }
        try {
            return Long.parseLong(value.substring(1, value.length() - 1));
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "Invalid If-Match revision",
                    exception);
        }
    }
}
