package dev.pixelib.reflectionpath.records;

import dev.pixelib.reflectionpath.errors.ReflectionException;
import dev.pixelib.reflectionpath.resolution.ResolvedPath;

import java.lang.reflect.Field;

/**
 * Implementation of ResolvedPath that represents a chain of paths ending in a field access.
 */
public record ChainedFieldPath(ResolvedPath parent, Field field) implements ResolvedPath {
    @Override
    public Object getValue(Object target) throws Exception {
        Object parentValue = parent.getValue(target);
        return field.get(parentValue);
    }

    @Override
    public Object invoke(Object target, Object... args) throws Exception {
        throw new ReflectionException("Cannot invoke a field as a method");
    }
}
