package de.wobkenh.pgen

import com.github.javaparser.ast.AccessSpecifier
import org.slf4j.LoggerFactory

class PumlBodyGenerator(
    private val showPackage: Boolean,
    private val showEnumArguments: Boolean
) {
    private val logger = LoggerFactory.getLogger("")
    private val newLine = PGenConstants.newLine

    fun generatePumlBody(classDescriptors: Sequence<ClassOrEnumDescriptor>): String =
        classDescriptors
            .flatMap { classDescriptor ->
                when (classDescriptor) {
                    is ClassDescriptor -> generatePumlFromClassDescriptor(classDescriptor).asSequence()
                    is EnumDescriptor -> generatePumlFromEnumDescriptor(classDescriptor).asSequence()
                    else -> throw RuntimeException("Could not create PUML String for $classDescriptor")
                }
            }
            .joinToString(newLine)

    private fun generatePumlFromClassDescriptor(classDescriptor: ClassDescriptor): List<String> {
        // Creating the lines
        val separator = "'------------------------"
        val packageName = getPackageName(classDescriptor.packageName)
        val className = packageName + classDescriptor.className

        val classDefinition = "${classDescriptor.type} $className {"
        val classExtension = if (classDescriptor.extendedClass.className.isNotEmpty()) {
            "$className --|> ${getPackageName(classDescriptor.extendedClass.packageName)}${classDescriptor.extendedClass.className}"
        } else ""
        val classImplementations = classDescriptor.implementedClasses
            .map { "${getPackageName(it.packageName)}${it.className}" }
            .map { "$className --|> $it" }
        val methods = getMethods(classDescriptor)
        val attributes = getAttributes(classDescriptor)
        val dependencies = getDependencies(classDescriptor, className)

        // Creating the list using the lines
        val lines = mutableListOf(separator, "", classDefinition)
        lines.addAll(attributes)
        lines.addAll(methods)
        lines.add("}")
        if (classExtension.isNotEmpty()) {
            lines.add(classExtension)
        }
        lines.addAll(classImplementations)
        lines.addAll(dependencies)
        lines.add("")
        return lines
    }

    private fun generatePumlFromEnumDescriptor(enumDescriptor: EnumDescriptor): List<String> {
        // Creating the lines
        val separator = "'------------------------"
        val packageName = getPackageName(enumDescriptor.packageName)
        val className = packageName + enumDescriptor.className
        val methods = getMethods(enumDescriptor)
        val attributes = getAttributes(enumDescriptor)
        val dependencies = getDependencies(enumDescriptor, className)
        val values = enumDescriptor.values.map { "${it.name} ${if (showEnumArguments) "(${it.parameters})" else ""}" }

        // Creating the list using the lines
        val lines = mutableListOf(separator, "", "enum $className {")
        lines.addAll(values)
        lines.addAll(attributes)
        lines.addAll(methods)
        lines.add("}")
        lines.addAll(dependencies)
        lines.add("")
        return lines
    }

    private fun getMethods(classOrEnumDescriptor: ClassOrEnumDescriptor) =
        classOrEnumDescriptor.methods.map { "${getVisibilitySign(it.visibility)} ${it.returnType} ${it.signature}" }

    private fun getAttributes(classOrEnumDescriptor: ClassOrEnumDescriptor) =
        classOrEnumDescriptor.attributes.map { "${getVisibilitySign(it.visibility)} ${it.type} ${it.name}" }

    private fun getDependencies(classOrEnumDescriptor: ClassOrEnumDescriptor, className: String) =
        classOrEnumDescriptor.dependencies
            .map { "${getPackageName(it.packageName)}${it.className}" }
            .map { "$className ..> $it" }

    private fun getPackageName(packageName: String) = if (showPackage && packageName.isNotEmpty()) "$packageName." else ""

    private fun getVisibilitySign(accessorSpecifier: AccessSpecifier): String = when (accessorSpecifier) {
        AccessSpecifier.PRIVATE -> "-"
        AccessSpecifier.PROTECTED -> "#"
        AccessSpecifier.PACKAGE_PRIVATE -> "~"
        AccessSpecifier.PUBLIC -> "+"
    }
}