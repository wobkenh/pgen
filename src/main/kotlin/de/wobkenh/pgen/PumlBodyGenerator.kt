package de.wobkenh.pgen

import com.github.javaparser.ast.AccessSpecifier

class PumlBodyGenerator(private val showPackage: Boolean) {

    private val newLine = PGenConstants.newLine

    fun generatePumlBody(classDescriptors: Sequence<ClassDescriptor>): String =
        classDescriptors
            .flatMap { classDescriptor -> generatePumlFromClassDescriptor(classDescriptor).asSequence() }
            .joinToString(newLine)

    private fun generatePumlFromClassDescriptor(classDescriptor: ClassDescriptor): List<String> {
        println("Found ${classDescriptor.type} ${classDescriptor.className} extending ${classDescriptor.extendedClassName} implementing interfaces ${classDescriptor.implementedClassNames}")

        // Creating the lines
        val separator = "'------------------------"
        val packageName = getPackageName(classDescriptor.packageName)
        val className = packageName + classDescriptor.className
        val classDefinition = "${classDescriptor.type} $className {"
        val classExtension = if (classDescriptor.extendedClassName.className.isNotEmpty()) {
            "$className --|> ${getPackageName(classDescriptor.extendedClassName.packageName)}${classDescriptor.extendedClassName.className}"
        } else ""
        val classImplementations = classDescriptor.implementedClassNames
            .map { "${getPackageName(it.packageName)}${it.className}" }
            .map { "$className --|> $it" }
        val methods = classDescriptor.methods.map { "${getVisibilitySign(it.visibility)} ${it.signature}" }
        val attributes = classDescriptor.attribtues.map { "${getVisibilitySign(it.visibility)} ${it.type} ${it.name}" }

        // Creating the list using the lines
        val lines = mutableListOf(separator, "", classDefinition)
        lines.addAll(attributes)
        lines.addAll(methods)
        lines.add("}")
        if (classExtension.isNotEmpty()) {
            lines.add(classExtension)
        }
        lines.addAll(classImplementations)
        lines.add("")
        return lines
    }

    private fun getPackageName(packageName: String) = if (showPackage && packageName.isNotEmpty()) "$packageName." else ""

    private fun getVisibilitySign(accessorSpecifier: AccessSpecifier): String = when (accessorSpecifier) {
        AccessSpecifier.PRIVATE -> "-"
        AccessSpecifier.PROTECTED -> "#"
        AccessSpecifier.PACKAGE_PRIVATE -> "~"
        AccessSpecifier.PUBLIC -> "+"
    }
}