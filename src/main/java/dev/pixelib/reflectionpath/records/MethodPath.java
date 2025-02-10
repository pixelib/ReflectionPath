package dev.pixelib.reflectionpath.records;

import dev.pixelib.reflectionpath.resolution.ResolvedPath;

import java.lang.reflect.Method;

/**
 * Implementation of ResolvedPath that represents a direct method access.
 */
public record MethodPath(Method method) implements ResolvedPath {
    @Override
    public Object getValue(Object target) throws Exception {
        return method.invoke(target);
    }

    @Override
    public Object invoke(Object target, Object... args) throws Exception {
        return method.invoke(target, args);
    }
}
