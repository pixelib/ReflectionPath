package dev.pixelib.reflectionpath.resolution;

/**
 * Record class representing a component in a type-based path, including array type information.
 */
public record TypePathComponent(String typeName, boolean isArray) {
    /**
     * Parses a type path component string into a TypePathComponent object.
     *
     * @param component The component string to parse
     * @return A new TypePathComponent instance
     */
    public static TypePathComponent parse(String component) {
        String cleanName = component.trim();
        boolean isArray = cleanName.endsWith("[]");
        if (isArray) {
            cleanName = cleanName.substring(0, cleanName.length() - 2).trim();
        }
        return new TypePathComponent(cleanName, isArray);
    }
}
