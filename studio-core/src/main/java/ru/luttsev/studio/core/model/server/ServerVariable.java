package ru.luttsev.studio.core.model.server;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.luttsev.studio.core.model.ExtensibleObject;

@Getter
@Setter
@NoArgsConstructor
public final class ServerVariable extends ExtensibleObject {

    private List<String> enumValues = new ArrayList<>();
    private String defaultValue;
    private String description;
}
