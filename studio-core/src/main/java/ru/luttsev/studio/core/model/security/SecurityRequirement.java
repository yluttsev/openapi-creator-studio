package ru.luttsev.studio.core.model.security;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;

@Getter
public final class SecurityRequirement {

    private final Map<String, List<String>> requirements = new LinkedHashMap<>();

    public void require(String scheme, List<String> scopesOrRoles) {
        requirements.put(scheme, new ArrayList<>(scopesOrRoles));
    }
}
