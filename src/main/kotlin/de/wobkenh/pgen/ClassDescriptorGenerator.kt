package de.wobkenh.pgen

import com.github.javaparser.StaticJavaParser
import com.github.javaparser.ast.Node
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.body.EnumDeclaration
import com.github.javaparser.ast.nodeTypes.NodeWithImplements
import com.github.javaparser.ast.nodeTypes.NodeWithMembers
import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName
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
    private val directories: List<File>,
    private val methodVisibility: Visibility,
    private val attributeVisibility: Visibility,
    private val dependencyLevel: DependencyLevel,
    private val showPackage: Boolean,
    private val showEnumArguments: Boolean
) {
    private val logger = LoggerFactory.getLogger("")

    init {
        StaticJavaParser.getConfiguration().setSymbolResolver(getSymbolResolver())
    }

    fun generateClassDescriptors(): Sequence<ClassOrEnumDescriptor> =
        directories
            .flatMap { dir -> dir.walk().filter { it.isFile && it.extension == "java" }.toList() }
            .flatMap { file -> generateClassDescriptor(file) }.asSequence()

    private fun generateClassDescriptor(file: File): List<ClassOrEnumDescriptor> {
        logger.trace("Generating descriptor for ${file.absolutePath}")
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
            val implementedClassDescriptors = getImplementedClassesClass(clazz)
            val type = if (clazz.isInterface) "interface" else if (clazz.isAbstract) "abstract class" else "class"
            val methods = getMethodsClass(clazz, clazz.isInterface)
            val attributes = getAttributesClass(clazz)
            val dependencies = if (dependencyLevel != DependencyLevel.NONE) {
                getDependencies(clazz)
                    .map { Pair(it, resolveQualifiedName(it)) }
                    .distinctBy { (_, qualifiedName) -> qualifiedName }
                    .map { (clazz, qualifiedName) -> DependencyDescriptor(getPackageName(qualifiedName), clazz.nameAsString) }
            } else listOf()
            dependencies.forEach { logger.trace("found dependency ${it.className}") }

            logger.debug("parsed ${clazz.nameAsString}")
            ClassDescriptor(
                packageName + getParentClasses(clazz),
                clazz.nameAsString,
                methods,
                attributes,
                dependencies,
                implementedClassDescriptors,
                type,
                ExtendsDescriptor(extendedPackageName, extendedClassName)
            )
        }.union(compilationUnit.findAll(EnumDeclaration::class.java).map { enum ->
            val dependencies = if (dependencyLevel != DependencyLevel.NONE) {
                getDependencies(enum)
                    .map { Pair(it, resolveQualifiedName(it)) }
                    .distinctBy { (_, qualifiedName) -> qualifiedName }
                    .map { (clazz, qualifiedName) -> DependencyDescriptor(getPackageName(qualifiedName), clazz.nameAsString) }
            } else listOf()
            requireNotNull(enum)
            val methods = getMethodsEnum(enum)
            val attributes = getAttributesEnum(enum)
            dependencies.forEach { logger.trace("found dependency ${it.className}") }
            val values = enum.entries.map {
                ValueDescriptor(it.nameAsString, if (showEnumArguments) it.arguments.joinToString(", ") else "")
            }
            val implementedClassDescriptors = getImplementedClassesEnum(enum)
            EnumDescriptor(
                packageName,
                enum.nameAsString,
                methods,
                attributes,
                dependencies,
                implementedClassDescriptors,
                values
            )
        }).toList()
    }

    private fun getParentClasses(classInterfaceOrEnum: Node): String {
        val parents = mutableListOf<String>()
        var node = classInterfaceOrEnum
        while (node.parentNode.isPresent) {
            node = node.parentNode.get()
            if (node !is NodeWithSimpleName<*>) {
                break
            }
            parents.add(node.nameAsString)
        }
        val parentsStr = parents.reversed().joinToString(".")
        return if (parentsStr.isEmpty()) {
            ""
        } else {
            ".$parentsStr"
        }
    }

    private fun getImplementedClassesClass(node: NodeWithImplements<ClassOrInterfaceDeclaration>) = getImplementedClassesBase(node)
    private fun getImplementedClassesEnum(node: NodeWithImplements<EnumDeclaration>) = getImplementedClassesBase(node)
    private fun <T : Node> getImplementedClassesBase(node: NodeWithImplements<T>): List<ImplementsDescriptor> =
        node.implementedTypes
            .map { ImplementsDescriptor(getPackageName(resolveQualifiedName(it)), it.nameAsString) }

    private fun getAttributesEnum(node: NodeWithMembers<EnumDeclaration>) = getAttributesBase(node)
    private fun getAttributesClass(node: NodeWithMembers<ClassOrInterfaceDeclaration>) = getAttributesBase(node)
    private fun <T : Node> getAttributesBase(node: NodeWithMembers<T>): List<FieldDescriptor> =
        if (attributeVisibility != Visibility.NONE) {
            node.fields
                .filter { attributeVisibility.isLowerOrEqual(it.accessSpecifier) }
                .map { field ->
                    FieldDescriptor(
                        field.variables.joinToString(", ") { it.nameAsString },
                        field.elementType.asString(), field.accessSpecifier
                    )
                }
        } else listOf()

    private fun getMethodsEnum(node: NodeWithMembers<EnumDeclaration>) = getMethodsBase(node, false)
    private fun getMethodsClass(node: NodeWithMembers<ClassOrInterfaceDeclaration>, isInterface: Boolean) =
        getMethodsBase(node, isInterface)

    private fun <T : Node> getMethodsBase(node: NodeWithMembers<T>, isInterface: Boolean): List<MethodDescriptor> {
        return if (methodVisibility != Visibility.NONE) {
            node.methods
                .filter { methodVisibility.isLowerOrEqual(it.accessSpecifier) || isInterface }
                .map { MethodDescriptor(it.signature.asString(), it.accessSpecifier, it.typeAsString) }
        } else listOf()
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
            typeSolvers.addAll(directories.map { JavaParserTypeSolver(it.parentFile) })
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