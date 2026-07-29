package ru.luttsev.studio.core.model.server;

import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.luttsev.studio.core.model.ExtensibleObject;

@Getter
@Setter
@NoArgsConstructor
public final class Server extends ExtensibleObject {

    private String url;
    private String description;
    private String name;
    private Map<String, ServerVariable> variables = new LinkedHashMap<>();
}
