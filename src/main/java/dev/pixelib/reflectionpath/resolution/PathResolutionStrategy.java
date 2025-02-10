package dev.pixelib.reflectionpath.resolution;

/**
 * Defines how to handle multiple matches when resolving type-based paths.
 */
public enum PathResolutionStrategy {
    /** Use the first matching member found */
    FIRST_MATCH,
    /** Use the last matching member found */
    LAST_MATCH,
    /** Require exactly one matching member */
    EXACT_MATCH
}
