package dev.pixelib.reflectionpath.records;

import dev.pixelib.reflectionpath.errors.ReflectionException;
import dev.pixelib.reflectionpath.resolution.ResolvedPath;

import java.lang.reflect.Field;

/**
 * Implementation of ResolvedPath that represents a direct field access.
 */
public record FieldPath(Field field) implements ResolvedPath {
    @Override
    public Object getValue(Object target) throws Exception {
        return field.get(target);
    }

    @Override
    public Object invoke(Object target, Object... args) throws Exception {
        throw new ReflectionException("Cannot invoke a field as a method");
    }
}
