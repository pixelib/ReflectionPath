package dev.pixelib.reflectionpath;

import dev.pixelib.reflectionpath.errors.ReflectionException;
import dev.pixelib.reflectionpath.records.ChainedFieldPath;
import dev.pixelib.reflectionpath.records.ChainedMethodPath;
import dev.pixelib.reflectionpath.records.FieldPath;
import dev.pixelib.reflectionpath.records.MethodPath;
import dev.pixelib.reflectionpath.resolution.PathResolutionStrategy;
import dev.pixelib.reflectionpath.resolution.PathType;
import dev.pixelib.reflectionpath.resolution.ResolvedPath;
import dev.pixelib.reflectionpath.resolution.TypePathComponent;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;

/**
 * ReflectionPath is a utility that provides type-safe reflection capabilities for accessing fields
 * and methods using either dot-notation path expressions or type-based path expressions.
 *
 * <p>The class supports two types of path expressions:
 * <ul>
 *   <li>Name-based paths: "field1.field2.method1.field3" - Traditional dot notation for accessing members by name</li>
 *   <li>Type-based paths: "[Player].[ConnectionType].[String]" - Square bracket notation for accessing members by their type</li>
 * </ul>
 *
 * <p>Type-based paths can also include array type specifications:
 * <pre>{@code
 * "[Player].[Item[]]" // Looks for a field/method returning Item[]
 * }</pre>
 *
 * <p>The class provides different strategies for resolving ambiguous matches when using type-based paths:
 * <ul>
 *   <li>FIRST_MATCH: Returns the first matching member found (default)</li>
 *   <li>LAST_MATCH: Returns the last matching member found</li>
 *   <li>EXACT_MATCH: Throws an exception if multiple matches are found</li>
 * </ul>
 *
 * <p>This class is thread-safe and caches resolved reflection paths for acceptable performance.
 *
 * <p>Example usage:
 * <pre>{@code
 * // Name-based access
 * ReflectionPath script1 = new ReflectionPath("person.address.city");
 * String city = script1.getAs(person, String.class);
 *
 * // Type-based access
 * ReflectionPath script2 = new ReflectionPath("[Player].[ConnectionType]");
 * ConnectionType type = script2.getAs(player, ConnectionType.class);
 *
 * // With specific resolution strategy
 * ReflectionPath script3 = new ReflectionPath("[String]", PathResolutionStrategy.EXACT_MATCH);
 * String value = script3.getAs(object, String.class);
 * }</pre>
 *
 * @see Field
 * @see Method
 * @see ConcurrentHashMap
 */
public class ReflectionPath {
    /** Pattern for matching type-based path components */
    private static final Pattern TYPE_PATH_PATTERN = Pattern.compile("\\[(.*?(?:\\[\\])?)]");

    private final String path;
    private final ConcurrentMap<Class<?>, ResolvedPath> resolvedPaths;
    private final PathType pathType;
    private final PathResolutionStrategy resolutionStrategy;
    private boolean ignoreToString = true;

    /**
     * Constructs a new ReflectionPath with the specified path expression using the default
     * FIRST_MATCH resolution strategy.
     *
     * @param path The path expression to resolve
     * @throws IllegalArgumentException if the path is null or empty
     */
    public ReflectionPath(String path) {
        this(path, PathResolutionStrategy.FIRST_MATCH);
    }

    /**
     * Determines if the toString method should be ignored when resolving methods by type.
     * By default, toString is ignored to prevent accidental matches.
     * @param ignoreToString true to ignore toString methods, false to include them
     */
    public void setIgnoreToString(boolean ignoreToString) {
        this.ignoreToString = ignoreToString;
    }

    /**
     * Constructs a new ReflectionPath with the specified path expression and resolution strategy.
     *
     * @param path The path expression to resolve
     * @param strategy The strategy to use when multiple matches are found
     * @throws IllegalArgumentException if the path is null or empty
     */
    public ReflectionPath(String path, PathResolutionStrategy strategy) {
        if (path == null || path.isEmpty()) {
            throw new IllegalArgumentException("Path cannot be null or empty");
        }
        this.path = path;
        this.resolvedPaths = new ConcurrentHashMap<>();
        this.pathType = determinePathType(path);
        this.resolutionStrategy = strategy;
    }

    /**
     * Retrieves the value at the specified path and casts it to the requested type.
     *
     * @param <T> The type to cast the result to
     * @param target The target object to resolve the path from
     * @param type The Class object representing the desired return type
     * @return The value at the specified path cast to the requested type
     * @throws ReflectionException if the path cannot be resolved or if the value cannot be cast
     * @throws IllegalArgumentException if target or type is null
     */
    public <T> T getAs(Object target, Class<T> type) {
        try {
            ResolvedPath resolvedPath = resolve(target);
            Object result = resolvedPath.getValue(target);
            return type.cast(result);
        } catch (Exception e) {
            throw new ReflectionException("Failed to get value as " + type.getSimpleName(), e);
        }
    }


    /**
     * Invokes the method at the specified path with the provided arguments.
     *
     * @param target The target object to resolve the path from
     * @param args The arguments to pass to the method
     * @return The result of the method invocation
     * @throws ReflectionException if the path cannot be resolved or if the invocation fails
     * @throws IllegalArgumentException if target is null
     */
    public Object invokeOn(Object target, Object... args) {
        try {
            return resolve(target).invoke(target, args);
        } catch (Exception e) {
            throw new ReflectionException("Failed to invoke method", e);
        }
    }

    private PathType determinePathType(String path) {
        return TYPE_PATH_PATTERN.matcher(path).find() ? PathType.TYPE_BASED : PathType.NAME_BASED;
    }

    private ResolvedPath resolve(Object target) {
        if (target == null) {
            throw new ReflectionException("Target object cannot be null");
        }

        return resolvedPaths.computeIfAbsent(target.getClass(),
                targetClass -> pathType == PathType.TYPE_BASED ?
                        resolveTypePath(targetClass, parseTypePath(path)) :
                        resolvePath(targetClass, path.split("\\.")));
    }

    private List<TypePathComponent> parseTypePath(String typePath) {
        List<TypePathComponent> components = new ArrayList<>();
        var matcher = TYPE_PATH_PATTERN.matcher(typePath);
        while (matcher.find()) {
            components.add(TypePathComponent.parse(matcher.group(1)));
        }
        return components;
    }

    private ResolvedPath resolveTypePath(Class<?> targetClass, List<TypePathComponent> typeComponents) {
        ResolvedPath currentPath = null;
        Class<?> currentClass = targetClass;

        for (TypePathComponent component : typeComponents) {
            var resolvedMember = findByType(currentClass, component);
            if (resolvedMember.isEmpty()) {
                throw new ReflectionException(
                        String.format("No member found of type '%s' in %s",
                                component.typeName(),
                                currentClass.getSimpleName()));
            }

            var member = resolvedMember.get();
            member.setAccessible(true);

            if (member instanceof Field field) {
                currentPath = (currentPath == null)
                        ? new FieldPath(field)
                        : new ChainedFieldPath(currentPath, field);
                currentClass = field.getType();
            } else if (member instanceof Method method) {
                currentPath = (currentPath == null)
                        ? new MethodPath(method)
                        : new ChainedMethodPath(currentPath, method);
                currentClass = method.getReturnType();
            }
        }

        if (currentPath == null) {
            throw new ReflectionException("Failed to resolve type path: " + path);
        }

        return currentPath;
    }

    private ResolvedPath resolvePath(Class<?> targetClass, String[] pathParts) {
        ResolvedPath currentPath = null;
        Class<?> currentClass = targetClass;

        for (String part : pathParts) {
            try {
                Field field = findField(currentClass, part);
                field.setAccessible(true);
                currentPath = (currentPath == null)
                        ? new FieldPath(field)
                        : new ChainedFieldPath(currentPath, field);
                currentClass = field.getType();
                continue;
            } catch (NoSuchFieldException ignored) {
                // Try methods next
            }

            try {
                Method method = findMethod(currentClass, part);
                method.setAccessible(true);
                currentPath = (currentPath == null)
                        ? new MethodPath(method)
                        : new ChainedMethodPath(currentPath, method);
                currentClass = method.getReturnType();
            } catch (NoSuchMethodException e) {
                throw new ReflectionException(
                        "No field or method found for '" + part + "' in " + currentClass.getSimpleName());
            }
        }

        if (currentPath == null) {
            throw new ReflectionException("Failed to resolve path: " + path);
        }

        return currentPath;
    }

    private Optional<AccessibleObject> findByType(Class<?> clazz, TypePathComponent component) {
        List<Field> fields = findFieldsByType(clazz, component);
        List<Method> methods = findMethodsByReturnType(clazz, component);

        List<AccessibleObject> allMembers = new ArrayList<>();
        allMembers.addAll(fields);
        allMembers.addAll(methods);

        if (allMembers.isEmpty()) {
            return Optional.empty();
        }

        return switch (resolutionStrategy) {
            case FIRST_MATCH -> Optional.of(allMembers.get(0));
            case LAST_MATCH -> Optional.of(allMembers.get(allMembers.size() - 1)); // Ensure last field is returned
            case EXACT_MATCH -> allMembers.size() == 1 ? Optional.of(allMembers.get(0)) : Optional.empty();
        };
    }


    private List<Field> findFieldsByType(Class<?> clazz, TypePathComponent component) {
        List<Field> matching = new ArrayList<>();
        Class<?> current = clazz;

        while (current != null) {
            for (Field field : current.getDeclaredFields()) {
                if (matchesType(field.getType(), component)) {
                    matching.add(field);
                }
            }
            current = current.getSuperclass();
        }

        return matching;
    }

    /**
     * Finds all methods in a class hierarchy that match the specified return type.
     *
     * @param clazz The class to search in
     * @param component The type component to match against
     * @return List of matching methods
     */
    private List<Method> findMethodsByReturnType(Class<?> clazz, TypePathComponent component) {
        List<Method> matching = new ArrayList<>();
        Class<?> current = clazz;

        while (current != null) {
            for (Method method : current.getDeclaredMethods()) {
                if (method.getParameterCount() == 0
                        && matchesType(method.getReturnType(), component)) {

                    // Ignore toString() unless explicitly allowed
                    if (ignoreToString && method.getName().equals("toString")
                            && method.getParameterCount() == 0) {
                        continue;
                    }

                    matching.add(method);
                }
            }
            current = current.getSuperclass();
        }

        return matching;
    }


    /**
     * Checks if a type matches the specified component type.
     *
     * @param type The type to check
     * @param component The type component to match against
     * @return true if the type matches
     */
    private boolean matchesType(Class<?> type, TypePathComponent component) {
        if (component.isArray() != type.isArray()) {
            return false;
        }

        Class<?> typeToCheck = component.isArray() ? type.getComponentType() : type;
        String typeNameToMatch = component.typeName();

        return typeToCheck.getSimpleName().equals(typeNameToMatch) ||
                typeToCheck.getName().equals(typeNameToMatch) ||
                typeToCheck.getCanonicalName().equals(typeNameToMatch);
    }

    /**
     * Searches for a field with the given name in the class hierarchy.
     *
     * @param clazz The class to search in
     * @param fieldName The name of the field to find
     * @return The found Field object
     * @throws NoSuchFieldException if no matching field is found
     */
    private Field findField(Class<?> clazz, String fieldName) throws NoSuchFieldException {
        Class<?> current = clazz;
        while (current != null) {
            try {
                return current.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                current = current.getSuperclass();
            }
        }
        throw new NoSuchFieldException(fieldName);
    }

    /**
     * Searches for a method with the given name in the class hierarchy.
     *
     * @param clazz The class to search in
     * @param methodName The name of the method to find
     * @return The found Method object
     * @throws NoSuchMethodException if no matching method is found
     */
    private Method findMethod(Class<?> clazz, String methodName) throws NoSuchMethodException {
        Class<?> current = clazz;
        while (current != null) {
            for (Method method : current.getDeclaredMethods()) {
                if (method.getName().equals(methodName)) {
                    return method;
                }
            }
            current = current.getSuperclass();
        }
        throw new NoSuchMethodException(methodName);
    }
}

