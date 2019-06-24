package de.wobkenh.pgen

import com.github.javaparser.ast.AccessSpecifier

data class ClassDescriptor(
    val packageName: String,
    val className: String,
    val type: String,
    val extendedClassName: ExtendsDescriptor,
    val implementedClassNames: List<ImplementsDescriptor>,
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

data class ExtendsDescriptor(
    var packageName: String, // var, can only be determined after all classes have been parsed
    val className: String
)

data class ImplementsDescriptor(
    var packageName: String, // var, can only be determined after all classes have been parsed
    val className: String
)
