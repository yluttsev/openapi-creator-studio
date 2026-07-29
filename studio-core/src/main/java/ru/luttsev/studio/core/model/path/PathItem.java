package ru.luttsev.studio.core.model.path;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.luttsev.studio.core.model.ExtensibleObject;
import ru.luttsev.studio.core.model.parameter.Parameter;
import ru.luttsev.studio.core.model.reference.ReferenceHolder;
import ru.luttsev.studio.core.model.reference.ReferenceOr;
import ru.luttsev.studio.core.model.schema.UriReference;
import ru.luttsev.studio.core.model.server.Server;

@Getter
@Setter
@NoArgsConstructor
public final class PathItem extends ExtensibleObject implements ReferenceHolder {

    private UriReference ref;
    private String summary;
    private String description;
    private Map<HttpMethod, Operation> operations = new LinkedHashMap<>();
    private List<Server> servers = new ArrayList<>();
    private List<ReferenceOr<Parameter>> parameters = new ArrayList<>();
}
