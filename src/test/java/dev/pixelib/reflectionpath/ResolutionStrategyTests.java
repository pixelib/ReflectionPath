package dev.pixelib.reflectionpath;

import dev.pixelib.reflectionpath.errors.ReflectionException;
import dev.pixelib.reflectionpath.models.Connection;
import dev.pixelib.reflectionpath.models.MultiFieldTest;
import dev.pixelib.reflectionpath.models.TestPlayer;
import dev.pixelib.reflectionpath.resolution.PathResolutionStrategy;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ResolutionStrategyTests {
    @Test
    void testFirstMatchStrategy() {
        MultiFieldTest test = new MultiFieldTest();
        ReflectionPath script = new ReflectionPath("[String]",
                PathResolutionStrategy.FIRST_MATCH);

        assertEquals("first", script.getAs(test, String.class));
    }

    @Test
    void testLastMatchStrategy() {
        MultiFieldTest test = new MultiFieldTest();
        ReflectionPath script = new ReflectionPath("[String]",
                PathResolutionStrategy.LAST_MATCH);

        assertEquals("last", script.getAs(test, String.class));
    }

    @Test
    void testExactMatchStrategySuccess() {
        TestPlayer player = new TestPlayer("Test");
        ReflectionPath script = new ReflectionPath("[Connection]",
                PathResolutionStrategy.EXACT_MATCH);

        assertDoesNotThrow(() -> script.getAs(player, Connection.class));
    }

    @Test
    void testExactMatchStrategyFailure() {
        MultiFieldTest test = new MultiFieldTest();
        ReflectionPath script = new ReflectionPath("[String]",
                PathResolutionStrategy.EXACT_MATCH);

        assertThrows(ReflectionException.class,
                () -> script.getAs(test, String.class));
    }
}
