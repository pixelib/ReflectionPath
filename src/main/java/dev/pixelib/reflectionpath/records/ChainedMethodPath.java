package dev.pixelib.reflectionpath.records;

import dev.pixelib.reflectionpath.resolution.ResolvedPath;

import java.lang.reflect.Method;

/**
 * Implementation of ResolvedPath that represents a chain of paths ending in a method access.
 */
public record ChainedMethodPath(ResolvedPath parent, Method method) implements ResolvedPath {
    @Override
    public Object getValue(Object target) throws Exception {
        Object parentValue = parent.getValue(target);
        return method.invoke(parentValue);
    }

    @Override
    public Object invoke(Object target, Object... args) throws Exception {
        Object parentValue = parent.getValue(target);
        return method.invoke(parentValue, args);
    }
}
