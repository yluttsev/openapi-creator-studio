package ru.luttsev.studio.core.model.media;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import ru.luttsev.studio.core.model.ExtensibleObject;
import ru.luttsev.studio.core.model.parameter.Header;
import ru.luttsev.studio.core.model.parameter.ParameterStyle;
import ru.luttsev.studio.core.model.reference.ReferenceOr;

public final class Encoding extends ExtensibleObject {

    private String contentType;
    private Map<String, ReferenceOr<Header>> headers = new LinkedHashMap<>();
    private ParameterStyle style;
    private Boolean explode;
    private Boolean allowReserved;
    private Map<String, Encoding> encoding = new LinkedHashMap<>();
    private List<Encoding> prefixEncoding = new ArrayList<>();
    private Encoding itemEncoding;

    public String getContentType() {
        return this.contentType;
    }

    public Map<String, ReferenceOr<Header>> getHeaders() {
        return this.headers;
    }

    public ParameterStyle getStyle() {
        return this.style;
    }

    public Boolean getExplode() {
        return this.explode;
    }

    public Boolean getAllowReserved() {
        return this.allowReserved;
    }

    public Map<String, Encoding> getEncoding() {
        return this.encoding;
    }

    public List<Encoding> getPrefixEncoding() {
        return this.prefixEncoding;
    }

    public Encoding getItemEncoding() {
        return this.itemEncoding;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public void setHeaders(Map<String, ReferenceOr<Header>> headers) {
        this.headers = headers;
    }

    public void setStyle(ParameterStyle style) {
        this.style = style;
    }

    public void setExplode(Boolean explode) {
        this.explode = explode;
    }

    public void setAllowReserved(Boolean allowReserved) {
        this.allowReserved = allowReserved;
    }

    public void setEncoding(Map<String, Encoding> encoding) {
        this.encoding = encoding;
    }

    public void setPrefixEncoding(List<Encoding> prefixEncoding) {
        this.prefixEncoding = prefixEncoding;
    }

    public void setItemEncoding(Encoding itemEncoding) {
        this.itemEncoding = itemEncoding;
    }

    public Encoding() {
    }
}
