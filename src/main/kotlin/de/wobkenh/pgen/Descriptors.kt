package de.wobkenh.pgen

import com.github.javaparser.ast.AccessSpecifier

data class ClassDescriptor(
    val packageName: String,
    val className: String,
    val type: String,
    val extendedClass: ExtendsDescriptor,
    val implementedClasses: List<ImplementsDescriptor>,
    val methods: List<MethodDescriptor>,
    val attributes: List<FieldDescriptor>,
    val dependencies: List<DependecyDescriptor>
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

data class ExtendsDescriptor(
    val packageName: String, // var, can only be determined after all classes have been parsed
    val className: String
)

data class ImplementsDescriptor(
    val packageName: String, // var, can only be determined after all classes have been parsed
    val className: String
)

data class DependecyDescriptor(
    val packageName: String,
    val className: String
)
