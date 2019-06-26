package de.wobkenh.pgen

import com.github.javaparser.StaticJavaParser
import com.github.javaparser.ast.Node
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.type.ClassOrInterfaceType
import com.github.javaparser.symbolsolver.JavaSymbolSolver
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver
import java.io.File

class ClassDescriptorGenerator(
    private val directory: File?,
    private val packagePath: String,
    private val methodVisibility: Visibility,
    private val attributeVisibility: Visibility
) {
    private val fileSeparator = File.separator

    fun generateClassDescriptors(): Sequence<ClassDescriptor> =
        File(directory, packagePath.replace(".", fileSeparator)).walk()
            .filter { it.isFile }
            .flatMap { file -> generateClassDescriptor(file).asSequence() }

    private fun generateClassDescriptor(file: File): List<ClassDescriptor> {
        val typeSolver = JavaSymbolSolver(CombinedTypeSolver())
        StaticJavaParser.getConfiguration().setSymbolResolver(typeSolver)
        val compilationUnit = StaticJavaParser.parse(file)
        val packageName = compilationUnit.packageDeclaration.get().nameAsString
        return compilationUnit.findAll(ClassOrInterfaceDeclaration::class.java).map { clazz ->
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
            val arguments = if (attributeVisibility != Visibility.NONE) {
                clazz.fields
                    .filter { attributeVisibility.isLowerOrEqual(it.accessSpecifier) }
                    .map { field ->
                        FieldDescriptor(
                            field.variables.joinToString(", ") { it.nameAsString },
                            field.elementType.asString(), field.accessSpecifier
                        )
                    }
            } else listOf()
            // TODO: Add Dependencies to classdescriptor
            //       Add DependencyDescriptor (Name + Package Name)
            //       Use JavaParser Configuration to resolve types
            //       Add cli option to show dependencies (NONE, INTERNAL (=provided dir), EXTERNAL (=incl. external deps), ALL (=incl. Java deps))
            val dependencies = getDependencies(clazz).distinct()

            println("parsed ${clazz.nameAsString}")
            ClassDescriptor(
                packageName,
                clazz.nameAsString,
                type,
                ExtendsDescriptor("", extendedClassName),
                implementedClassNames.map { ImplementsDescriptor("", it) },
                methods,
                arguments
            )
        }
    }

    private fun getDependencies(node: Node): List<String> {
        if (node is ClassOrInterfaceType) {
            return listOf(if (node.nameAsString == null) "" else node.nameAsString)
        }
        return node.childNodes.flatMap { getDependencies(it) }
    }

}