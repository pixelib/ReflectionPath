package dev.pixelib.reflectionpath;

import dev.pixelib.reflectionpath.models.Connection;
import dev.pixelib.reflectionpath.models.ConnectionType;
import dev.pixelib.reflectionpath.models.TestPlayer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NameBasedTests {
    @Test
    void testSimpleFieldAccess() {
        TestPlayer player = new TestPlayer("TestName");
        ReflectionPath script = new ReflectionPath("name");

        assertEquals("TestName", script.getAs(player, String.class));
    }

    @Test
    void testNestedFieldAccess() {
        TestPlayer player = new TestPlayer("Test");
        player.setConnection(new Connection(ConnectionType.LOCAL));
        ReflectionPath script = new ReflectionPath("connection.type");

        assertEquals(ConnectionType.LOCAL, script.getAs(player, ConnectionType.class));
    }

    @Test
    void testMethodInvocation() {
        TestPlayer player = new TestPlayer("Test");
        ReflectionPath script = new ReflectionPath("getName");

        assertEquals("Test", script.getAs(player, String.class));
    }

    @Test
    void testChainedMethodInvocation() {
        TestPlayer player = new TestPlayer("Test");
        player.setConnection(new Connection(ConnectionType.REMOTE));
        ReflectionPath script = new ReflectionPath("connection.getType");

        assertEquals(ConnectionType.REMOTE, script.getAs(player, ConnectionType.class));
    }
}
