package dev.pixelib.reflectionpath.resolution;

/**
 * Defines the type of path expression being used.
 */
public enum PathType {
    /** Traditional dot-notation path (e.g., "field1.field2") */
    NAME_BASED,
    /** Type-based path with square brackets (e.g., "[Type1].[Type2]") */
    TYPE_BASED
}
