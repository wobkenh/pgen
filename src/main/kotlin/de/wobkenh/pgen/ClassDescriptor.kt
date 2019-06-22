package de.wobkenh.pgen

data class ClassDescriptor(
    val className: String,
    val extendedClassName: String,
    val implementedClassNames: List<String>
)