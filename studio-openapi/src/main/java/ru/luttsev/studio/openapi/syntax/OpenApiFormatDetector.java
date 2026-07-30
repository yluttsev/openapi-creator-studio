package ru.luttsev.studio.openapi.syntax;

import ru.luttsev.studio.openapi.format.OpenApiFormat;

final class OpenApiFormatDetector {

    private static final char BYTE_ORDER_MARK = '\uFEFF';

    private OpenApiFormatDetector() {
    }

    static OpenApiFormat detect(String content) {
        int index = content.isEmpty() || content.charAt(0) != BYTE_ORDER_MARK
                ? 0
                : 1;
        while (index < content.length()
                && Character.isWhitespace(content.charAt(index))) {
            index++;
        }

        if (index < content.length()) {
            char firstCharacter = content.charAt(index);
            if (firstCharacter == '{' || firstCharacter == '[') {
                return OpenApiFormat.JSON;
            }
        }
        return OpenApiFormat.YAML;
    }

    static String removeByteOrderMark(String content) {
        if (!content.isEmpty() && content.charAt(0) == BYTE_ORDER_MARK) {
            return content.substring(1);
        }
        return content;
    }
}
