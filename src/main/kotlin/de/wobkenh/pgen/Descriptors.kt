package de.wobkenh.pgen

import com.github.javaparser.ast.AccessSpecifier

public abstract class ClassOrEnumDescriptor(
    val packageName: String,
    val className: String,
    val methods: List<MethodDescriptor>,
    val attributes: List<FieldDescriptor>,
    val dependencies: List<DependencyDescriptor>,
    val implementedClasses: List<ImplementsDescriptor>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ClassOrEnumDescriptor

        if (packageName != other.packageName) return false
        if (className != other.className) return false
        if (methods != other.methods) return false
        if (attributes != other.attributes) return false
        if (dependencies != other.dependencies) return false
        if (implementedClasses != other.implementedClasses) return false

        return true
    }

    override fun hashCode(): Int {
        var result = packageName.hashCode()
        result = 31 * result + className.hashCode()
        result = 31 * result + methods.hashCode()
        result = 31 * result + attributes.hashCode()
        result = 31 * result + dependencies.hashCode()
        result = 31 * result + implementedClasses.hashCode()
        return result
    }

    override fun toString(): String {
        return "ClassOrEnumDescriptor(packageName='$packageName', className='$className', methods=$methods, attributes=$attributes, dependencies=$dependencies, implementedClasses=$implementedClasses)"
    }


}

public class EnumDescriptor(
    packageName: String,
    className: String,
    methods: List<MethodDescriptor>,
    attributes: List<FieldDescriptor>,
    dependencies: List<DependencyDescriptor>,
    implementedClasses: List<ImplementsDescriptor>,
    val values: List<ValueDescriptor>
) : ClassOrEnumDescriptor(packageName, className, methods, attributes, dependencies, implementedClasses) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as EnumDescriptor

        if (values != other.values) return false

        return super.equals(other)
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + values.hashCode()
        result = 31 * result + super.hashCode()
        return result
    }

    override fun toString(): String {
        return "EnumDescriptor(${super.toString()}, values=$values)"
    }


}

public class ClassDescriptor(
    packageName: String,
    className: String,
    methods: List<MethodDescriptor>,
    attributes: List<FieldDescriptor>,
    dependencies: List<DependencyDescriptor>,
    implementedClasses: List<ImplementsDescriptor>,
    val type: String,
    val extendedClass: ExtendsDescriptor

) : ClassOrEnumDescriptor(packageName, className, methods, attributes, dependencies, implementedClasses) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ClassDescriptor

        if (type != other.type) return false
        if (extendedClass != other.extendedClass) return false

        return super.equals(other)
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + extendedClass.hashCode()
        result = 31 * result + super.hashCode()
        return result
    }

    override fun toString(): String {
        return "ClassDescriptor(${super.toString()}, type='$type', extendedClass=$extendedClass)"
    }


}

data class MethodDescriptor(
    val signature: String,
    val visibility: AccessSpecifier,
    val returnType: String
)

data class FieldDescriptor(
    val name: String,
    val type: String,
    val visibility: AccessSpecifier
)

data class ExtendsDescriptor(
    val packageName: String,
    val className: String
)

data class ImplementsDescriptor(
    val packageName: String,
    val className: String
)

data class DependencyDescriptor(
    val packageName: String,
    val className: String
)

data class ValueDescriptor(
    val name: String,
    val parameters: String
)
