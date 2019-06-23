package de.wobkenh.pgen

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parameters.types.file
import com.github.javaparser.StaticJavaParser
import com.github.javaparser.ast.AccessSpecifier
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import java.io.File
import java.time.LocalDateTime
import kotlin.system.exitProcess

class PGen : CliktCommand() {

    // CLI Arguments
    private val packagePath: String by option(help = "The package to analyze (OPTIONAL)").default("")
    private val directory: File? by option(help = "Sources root directory (REQUIRED)").file()
    private val outputFile: File by option(help = "Output file for PUML Class Diagram. Default output.puml").file().default(File("output.puml"))
    private val methodVisibility: Visibility by option(help = "Which methods to show. Default NONE").choice(
        "NONE" to Visibility.NONE,
        "PRIVATE" to Visibility.PRIVATE,
        "PACKAGE" to Visibility.PACKAGE,
        "PROTECTED" to Visibility.PROTECTED,
        "PUBLIC" to Visibility.PUBLIC
    ).default(Visibility.NONE)
    private val attributeVisibility: Visibility by option(help = "Which attributes/fields to show. Default NONE").choice(
        "NONE" to Visibility.NONE,
        "PRIVATE" to Visibility.PRIVATE,
        "PACKAGE" to Visibility.PACKAGE,
        "PROTECTED" to Visibility.PROTECTED,
        "PUBLIC" to Visibility.PUBLIC
    ).default(Visibility.NONE)
    private val baseClass: String by option(help = "If given, will only generate the class diagram for the hierarchie of this base class (OPTIONAL)").default(
        ""
    )
    private val scale: String? by option(help = "Scale of the diagram, e.g. '1.5', '200*100' or 'max 1024 height' (OPTIONAL)")
    private val title: String? by option(help = "Title of the diagram (OPTIONAL)")
    private val caption: String? by option(help = "Caption of the diagram (OPTIONAL)")
    private val leftToRightDirection: Boolean by option(help = "Changes the direction of the diagram to 'left to right'. Default 'top to bottom'").flag(
        default = false
    )


    // Other stuff
    private val newLine = System.lineSeparator()
    private val fileSeparator = File.separator

    override fun run() {
        if (directory == null) {
            println("No sources root directory provided.")
            exitProcess(1)
        }

        println("Generating PUML Class Diagram for classes in package path $packagePath")
        println("Using ${outputFile.absolutePath} as output path")

        if (outputFile.exists()) {
            if (outputFile.isFile) {
                println("Output file already exists. It will be overwritten")
            } else {
                println("Output file already exists and is not a file.")
                exitProcess(1)
            }
        }

        var classDescriptors = generateClassDescriptors()
        if (baseClass.isNotEmpty()) {
            println("Filtering for base class $baseClass")
            classDescriptors = BaseClassTreeFilter.filterForBaseClass(classDescriptors, this.baseClass)
        }

        val pumlBody = generatePumlBody(classDescriptors)
        writePuml(pumlBody)
    }

    private fun generateClassDescriptors(): Sequence<ClassDescriptor> =
        File(directory, packagePath.replace(".", fileSeparator)).walk()
            .filter { it.isFile }
            .flatMap { file -> generateClassDescriptor(file).asSequence() }

    private fun generateClassDescriptor(file: File): List<ClassDescriptor> =
        StaticJavaParser.parse(file).findAll(ClassOrInterfaceDeclaration::class.java).map { clazz ->
            val extendedClassName = if (clazz.extendedTypes.isNonEmpty) {
                clazz.extendedTypes[0].nameAsString
            } else ""
            val implementedClassNames = clazz.implementedTypes.map { it.nameAsString }
            val type = if (clazz.isInterface) "interface" else if (clazz.isAbstract) "abstract class" else "class"
            val methods = if (methodVisibility != Visibility.NONE) {
                clazz.methods
                    .filter { methodVisibility.isLowerOrEqual(it.accessSpecifier) || clazz.isInterface }
                    .map { MethodDescriptor(it.signature.asString(), it.accessSpecifier) }
            } else listOf()
            val arguments = if (methodVisibility != Visibility.NONE) {
                clazz.fields
                    .filter { attributeVisibility.isLowerOrEqual(it.accessSpecifier) }
                    .map { field ->
                        FieldDescriptor(
                            field.variables.joinToString(", ") { it.nameAsString },
                            field.elementType.asString(), field.accessSpecifier
                        )
                    }
            } else listOf()
            ClassDescriptor(clazz.nameAsString, type, extendedClassName, implementedClassNames, methods, arguments)
        }

    private fun generatePumlBody(classDescriptors: Sequence<ClassDescriptor>): String =
        classDescriptors
            .flatMap { classDescriptor -> generatePumlFromClassDescriptor(classDescriptor).asSequence() }
            .joinToString(newLine)

    private fun generatePumlFromClassDescriptor(classDescriptor: ClassDescriptor): List<String> {
        println("Found ${classDescriptor.type} ${classDescriptor.className} extending ${classDescriptor.extendedClassName} implementing interfaces ${classDescriptor.implementedClassNames}")

        // Creating the lines
        val separator = "'------------------------"
        val classDefinition = "${classDescriptor.type} ${classDescriptor.className} {"
        val classExtension = if (classDescriptor.extendedClassName.isNotEmpty()) {
            "${classDescriptor.className} --|> ${classDescriptor.extendedClassName}"
        } else ""
        val classImplementations = classDescriptor.implementedClassNames.map { "${classDescriptor.className} --|> $it" }
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


    private fun writePuml(pumlBodyString: String) {
        val headerLines = mutableListOf(
            "@startuml",
            "/'",
            "    PUML Class Diagram generated by PGen",
            "    Date: ${LocalDateTime.now()}",
            "    Package: $packagePath",
            "'/"
        )
        if (attributeVisibility == Visibility.NONE && methodVisibility == Visibility.NONE) {
            headerLines.add("hide members")
        }
        if (scale != null) {
            headerLines.add("scale $scale")
        }
        if (title != null) {
            headerLines.add("title $title")
        }
        if (caption != null) {
            headerLines.add("caption $caption")
        }
        if (leftToRightDirection) {
            headerLines.add("left to right direction")
        }
        val footer = "@enduml"
        outputFile.writeText(listOf(headerLines.joinToString(newLine), pumlBodyString, footer).joinToString(newLine))
    }

    private fun getVisibilitySign(accessorSpecifier: AccessSpecifier): String = when (accessorSpecifier) {
        AccessSpecifier.PRIVATE -> "-"
        AccessSpecifier.PROTECTED -> "#"
        AccessSpecifier.PACKAGE_PRIVATE -> "~"
        AccessSpecifier.PUBLIC -> "+"
    }

}

fun main(args: Array<String>) = PGen().main(args)