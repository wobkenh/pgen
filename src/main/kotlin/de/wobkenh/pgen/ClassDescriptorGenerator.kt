package de.wobkenh.pgen

import com.github.javaparser.StaticJavaParser
import com.github.javaparser.ast.Node
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.type.ClassOrInterfaceType
import com.github.javaparser.resolution.UnsolvedSymbolException
import com.github.javaparser.symbolsolver.JavaSymbolSolver
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver
import org.slf4j.LoggerFactory
import java.io.File

class ClassDescriptorGenerator(
    private val directory: File?,
    private val packagePath: String,
    private val methodVisibility: Visibility,
    private val attributeVisibility: Visibility,
    private val dependencyLevel: DependencyLevel,
    private val showPackage: Boolean
) {
    private val fileSeparator = File.separator
    private val logger = LoggerFactory.getLogger("")

    fun generateClassDescriptors(): Sequence<ClassDescriptor> =
        File(directory, packagePath.replace(".", fileSeparator)).walk()
            .filter { it.isFile }
            .flatMap { file -> generateClassDescriptor(file).asSequence() }

    private fun generateClassDescriptor(file: File): List<ClassDescriptor> {
        // TODO: Always at least project dir resolving for extending/implementing classes
        StaticJavaParser.getConfiguration().setSymbolResolver(getSymbolResolver())
        val compilationUnit = StaticJavaParser.parse(file)
        val packageName = compilationUnit.packageDeclaration.get().nameAsString
        return compilationUnit.findAll(ClassOrInterfaceDeclaration::class.java).map { clazz ->
            val extendedClassName = if (clazz.extendedTypes.isNonEmpty) {
                clazz.extendedTypes[0].nameAsString
            } else ""
            val extendedClassNameQualified = if (clazz.extendedTypes.isNonEmpty) {
                resolveQualifiedName(clazz.extendedTypes[0])
            } else ""
            val extendedPackageName = getPackageName(extendedClassNameQualified)
            val implementedClassDescriptors = clazz.implementedTypes
                .map { ImplementsDescriptor(getPackageName(resolveQualifiedName(it)), it.nameAsString) }
            val type = if (clazz.isInterface) "interface" else if (clazz.isAbstract) "abstract class" else "class"
            val methods = if (methodVisibility != Visibility.NONE) {
                clazz.methods
                    .filter { methodVisibility.isLowerOrEqual(it.accessSpecifier) || clazz.isInterface }
                    .map { MethodDescriptor(it.signature.asString(), it.accessSpecifier) }
            } else listOf()
            val attributes = if (attributeVisibility != Visibility.NONE) {
                clazz.fields
                    .filter { attributeVisibility.isLowerOrEqual(it.accessSpecifier) }
                    .map { field ->
                        FieldDescriptor(
                            field.variables.joinToString(", ") { it.nameAsString },
                            field.elementType.asString(), field.accessSpecifier
                        )
                    }
            } else listOf()
            val dependencies = if (dependencyLevel != DependencyLevel.NONE) {
                getDependencies(clazz)
                    .map { Pair(it, resolveQualifiedName(it)) }
                    .distinctBy { (_, qualifiedName) -> qualifiedName }
                    .map { (clazz, qualifiedName) -> DependecyDescriptor(getPackageName(qualifiedName), clazz.nameAsString) }
            } else listOf()
            dependencies.forEach { logger.trace("found dependency ${it.className}") }

            logger.debug("parsed ${clazz.nameAsString}")
            ClassDescriptor(
                packageName,
                clazz.nameAsString,
                type,
                ExtendsDescriptor(extendedPackageName, extendedClassName),
                implementedClassDescriptors,
                methods,
                attributes,
                dependencies
            )
        }
    }

    private fun resolveQualifiedName(type: ClassOrInterfaceType): String {
        return try {
            type.resolve().qualifiedName
        } catch (e: UnsolvedSymbolException) {
            logger.trace("Could not resolve full qualified name of ${type.nameAsString}, using class name instead")
            type.nameAsString
        }
    }

    private fun getDependencies(node: Node): List<ClassOrInterfaceType> {
        if (node is ClassOrInterfaceType) {
            return listOf(node)
        }
        return node.childNodes.flatMap { getDependencies(it) }
    }

    private fun getPackageName(qualifiedName: String): String {
        val index = qualifiedName.lastIndexOf(".")
        return if (index >= 0) {
            qualifiedName.substring(0, index)
        } else {
            ""
        }
    }

    private fun getSymbolResolver(): JavaSymbolSolver {
        val typeSolvers = mutableListOf<TypeSolver>()
        if (dependencyLevel >= DependencyLevel.INTERNAL || showPackage) {
            typeSolvers.add(JavaParserTypeSolver(directory))
        }
        if (dependencyLevel >= DependencyLevel.EXTERNAL) {
            // TODO: Add external libraries
        }
        if (dependencyLevel == DependencyLevel.ALL) {
            typeSolvers.add(ReflectionTypeSolver())
        }
        return JavaSymbolSolver(CombinedTypeSolver(*typeSolvers.toTypedArray()))
    }

}