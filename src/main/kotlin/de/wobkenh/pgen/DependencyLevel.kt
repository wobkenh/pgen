package de.wobkenh.pgen

enum class DependencyLevel(val value: Int) {
    NONE(0), // do not show dependencies
    INTERNAL(1), // show only dependencies in current scope (provided dir/package)
    EXTERNAL(2), // show internal dependencies and dependencies from external jars (e.g. maven/gradle dependencies)
    ALL(3); // resolve every dependency including java dependencies
}