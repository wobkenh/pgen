package de.wobkenh.pgen

import com.github.ajalt.clikt.output.TermUi
import kotlin.system.exitProcess

object BaseClassTreeFilter {
    fun filterForBaseClass(classDescriptorsSequence: Sequence<ClassDescriptor>, baseClass: String): Sequence<ClassDescriptor> {
        val classDescriptors = classDescriptorsSequence.toList()
        var baseClassDescriptor = classDescriptors.find { it.className == baseClass }
        if (baseClassDescriptor == null) {
            val proceed = TermUi.confirm("Base class $baseClass could not be found in package. Continue?")
            if (proceed != null && proceed) {
                var type = TermUi.prompt(text = "Choose type of $baseClass:", default = "class")
                while (type == null || type != "class" && type != "abstract class" && type != "interface") {
                    type = TermUi.prompt(text = "Choose type of $baseClass:", default = "class")
                }
                baseClassDescriptor =
                    ClassDescriptor("", baseClass, type, ExtendsDescriptor("", ""), listOf(), listOf(), listOf(), listOf())
            } else {
                exitProcess(1)
            }
        }
        return sequenceOf(baseClassDescriptor) + listExtendingClasses(classDescriptors, baseClass)
    }

    private fun listExtendingClasses(classDescriptors: List<ClassDescriptor>, baseClass: String): Sequence<ClassDescriptor> {
        val extendingClassDescriptors = classDescriptors.filter { isDescendand(it, baseClass) }.asSequence()
        return extendingClassDescriptors + extendingClassDescriptors.flatMap {
            listExtendingClasses(
                classDescriptors,
                it.className
            )
        }
    }

    private fun isDescendand(classDescriptor: ClassDescriptor, baseClass: String): Boolean =
        classDescriptor.extendedClass.className == baseClass ||
                classDescriptor.implementedClasses.any { implementsDescriptor -> implementsDescriptor.className == baseClass }

}