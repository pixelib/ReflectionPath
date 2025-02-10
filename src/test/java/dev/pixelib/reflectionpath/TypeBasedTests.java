package dev.pixelib.reflectionpath;

import dev.pixelib.reflectionpath.models.Connection;
import dev.pixelib.reflectionpath.models.ConnectionType;
import dev.pixelib.reflectionpath.models.TestPlayer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TypeBasedTests {
    @Test
    void testSimpleTypeAccess() {
        TestPlayer player = new TestPlayer("Test");
        ReflectionPath script = new ReflectionPath("[String]");

        assertEquals("Test", script.getAs(player, String.class));
    }

    @Test
    void testNestedTypeAccess() {
        TestPlayer player = new TestPlayer("Test");
        player.setConnection(new Connection(ConnectionType.LOCAL));
        ReflectionPath script = new ReflectionPath("[Connection].[ConnectionType]");

        assertEquals(ConnectionType.LOCAL, script.getAs(player, ConnectionType.class));
    }

    @Test
    void testArrayTypeAccess() {
        TestPlayer player = new TestPlayer("Test");
        player.setInventory(new String[]{"Item1", "Item2"});
        ReflectionPath script = new ReflectionPath("[String[]]");

        assertArrayEquals(new String[]{"Item1", "Item2"},
                script.getAs(player, String[].class));
    }

    @Test
    void testMethodReturnTypeAccess() {
        TestPlayer player = new TestPlayer("Test");
        ReflectionPath script = new ReflectionPath("[String]");

        assertEquals("Test", script.getAs(player, String.class));
    }
}
