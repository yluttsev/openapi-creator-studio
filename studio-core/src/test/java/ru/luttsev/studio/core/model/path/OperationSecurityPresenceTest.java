package ru.luttsev.studio.core.model.path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;
import ru.luttsev.studio.core.model.security.SecurityRequirement;

class OperationSecurityPresenceTest {

    @Test
    void distinguishesInheritedSecurityFromExplicitEmptyOverride() {
        Operation operation = new Operation();

        assertFalse(operation.hasSecurityOverride());
        assertTrue(operation.getSecurity().isEmpty());

        operation.setSecurity(List.of());

        assertTrue(operation.hasSecurityOverride());
        assertTrue(operation.getSecurity().isEmpty());
    }

    @Test
    void mutationsDefineOverrideAndInheritanceClearsIt() {
        Operation operation = new Operation();
        operation.getSecurity().add(new SecurityRequirement());

        assertTrue(operation.hasSecurityOverride());

        operation.inheritSecurity();

        assertFalse(operation.hasSecurityOverride());
        assertTrue(operation.getSecurity().isEmpty());

        operation.getSecurity().clear();

        assertTrue(operation.hasSecurityOverride());
        assertTrue(operation.getSecurity().isEmpty());
    }
}
