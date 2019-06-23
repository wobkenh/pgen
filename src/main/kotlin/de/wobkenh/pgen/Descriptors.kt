package de.wobkenh.pgen

import com.github.javaparser.ast.AccessSpecifier

data class ClassDescriptor(
    val className: String,
    val type: String,
    val extendedClassName: String,
    val implementedClassNames: List<String>,
    val methods: List<MethodDescriptor>,
    val attribtues: List<FieldDescriptor>
)

data class MethodDescriptor(
    val signature: String,
    val visibility: AccessSpecifier
)

data class FieldDescriptor(
    val name: String,
    val type: String,
    val visibility: AccessSpecifier
)
