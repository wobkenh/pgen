package de.wobkenh.pgen

data class ClassDescriptor(
    val className: String,
    val type: String,
    val extendedClassName: String,
    val implementedClassNames: List<String>
)