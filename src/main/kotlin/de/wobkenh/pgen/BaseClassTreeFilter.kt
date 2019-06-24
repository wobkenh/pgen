package de.wobkenh.pgen

import com.github.ajalt.clikt.output.TermUi
import kotlin.system.exitProcess

object BaseClassTreeFilter {
    fun filterForBaseClass(classDescriptors: Sequence<ClassDescriptor>, baseClass: String): Sequence<ClassDescriptor> {
        var baseClassDescriptor = classDescriptors.find { it.className == baseClass }
        if (baseClassDescriptor == null) {
            val proceed = TermUi.confirm("Base class $baseClass could not be found in package. Continue?")
            if (proceed != null && proceed) {
                var type = TermUi.prompt(text = "Choose type of $baseClass:", default = "class")
                while (type == null || type != "class" && type != "abstract class" && type != "interface") {
                    type = TermUi.prompt(text = "Choose type of $baseClass:", default = "class")
                }
                baseClassDescriptor = ClassDescriptor("", baseClass, type, ExtendsDescriptor("", ""), listOf(), listOf(), listOf())
            } else {
                exitProcess(1)
            }
        }
        return sequenceOf(baseClassDescriptor) + listExtendingClasses(classDescriptors, baseClass)
    }

    private fun listExtendingClasses(classDescriptors: Sequence<ClassDescriptor>, baseClass: String): Sequence<ClassDescriptor> {
        val extendingClassDescriptors = classDescriptors.filter { it.extendedClassName.className == baseClass }
        return extendingClassDescriptors + extendingClassDescriptors.flatMap {
            listExtendingClasses(
                classDescriptors,
                it.className
            )
        }
    }
}