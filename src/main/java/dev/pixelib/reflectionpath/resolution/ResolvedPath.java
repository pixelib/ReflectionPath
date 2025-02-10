package dev.pixelib.reflectionpath.resolution;

/**
 * Interface representing a resolved reflection path that can be used to access
 * fields or invoke methods.
 */
public interface ResolvedPath {
    /**
     * Gets the value at this path from the target object.
     *
     * @param target The target object to get the value from
     * @return The value at this path
     * @throws Exception if the value cannot be retrieved
     */
    Object getValue(Object target) throws Exception;

    /**
     * Invokes the method at this path on the target object with the given arguments.
     *
     * @param target The target object to invoke the method on
     * @param args The arguments to pass to the method
     * @return The result of the method invocation
     * @throws Exception if the method cannot be invoked
     */
    Object invoke(Object target, Object... args) throws Exception;
}
