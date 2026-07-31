package ru.luttsev.studio.openapi.version.v31.decode;

import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import ru.luttsev.studio.core.model.reference.ReferenceOr;
import ru.luttsev.studio.core.model.response.ApiResponse;
import ru.luttsev.studio.core.model.response.ResponseKey;
import ru.luttsev.studio.core.model.response.Responses;
import ru.luttsev.studio.core.model.value.DocumentValue;
import ru.luttsev.studio.core.model.value.ObjectValue;

final class ResponsesDecoder {

    private static final Pattern STATUS_CODE_PATTERN =
            Pattern.compile("^[1-5](?:[0-9]{2}|XX)$");

    private final PayloadDecoder payloadDecoder;

    ResponsesDecoder(PayloadDecoder payloadDecoder) {
        this.payloadDecoder = Objects.requireNonNull(
                payloadDecoder,
                "payloadDecoder must not be null");
    }

    Responses decode(ObjectValue source, DecodeContext context) {
        Responses responses = new Responses();
        for (Map.Entry<String, DocumentValue> entry : source.values().entrySet()) {
            if (!isResponseKey(entry.getKey())) {
                responses.getAdditionalFields().put(
                        entry.getKey(),
                        entry.getValue());
                continue;
            }

            ReferenceOr<ApiResponse> response = payloadDecoder.decodeResponse(
                    entry.getValue(),
                    context.child(entry.getKey()));
            if (response != null) {
                responses.getValues().put(
                        new ResponseKey(entry.getKey()),
                        response);
            }
        }
        return responses;
    }

    private static boolean isResponseKey(String value) {
        return "default".equals(value)
                || STATUS_CODE_PATTERN.matcher(value).matches();
    }
}
