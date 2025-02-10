package dev.pixelib.reflectionpath;

import dev.pixelib.reflectionpath.errors.ReflectionException;
import dev.pixelib.reflectionpath.models.TestPlayer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class ErrorHandlingTests {
    @Test
    void testNullTarget() {
        ReflectionPath script = new ReflectionPath("name");
        assertThrows(ReflectionException.class,
                () -> script.getAs(null, String.class));
    }

    @Test
    void testInvalidNamePath() {
        TestPlayer player = new TestPlayer("Test");
        ReflectionPath script = new ReflectionPath("invalidPath");
        assertThrows(ReflectionException.class,
                () -> script.getAs(player, String.class));
    }

    @Test
    void testInvalidTypePath() {
        TestPlayer player = new TestPlayer("Test");
        ReflectionPath script = new ReflectionPath("[InvalidType]");
        assertThrows(ReflectionException.class,
                () -> script.getAs(player, Object.class));
    }

    @Test
    void testInvalidTypeConversion() {
        TestPlayer player = new TestPlayer("Test");
        ReflectionPath script = new ReflectionPath("name");
        assertThrows(ReflectionException.class,
                () -> script.getAs(player, Integer.class));
    }
}
