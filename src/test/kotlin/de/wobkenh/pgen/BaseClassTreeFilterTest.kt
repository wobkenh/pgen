package de.wobkenh.pgen

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class BaseClassTreeFilterTest {
    @Test
    fun `test filtering for base classes`() {
        val baseClass = createTestClassDescriptor("Exception", "class", "")
        val extendClass = createTestClassDescriptor("class1", "class", "Exception")
        val extendClass2 = createTestClassDescriptor("class2", "class", "Exception")
        val unrelatedClass = createTestClassDescriptor("unrelatedClass", "class", "OtherClass")
        val unrelatedClass2 = createTestClassDescriptor("UnrelatedClass2", "class", "")
        val list: List<ClassOrEnumDescriptor> = BaseClassTreeFilter.filterForBaseClass(
            sequenceOf(baseClass, extendClass, extendClass2, unrelatedClass, unrelatedClass2),
            "Exception"
        ).toList()
        assertTrue(list.contains(baseClass))
        assertTrue(list.contains(extendClass))
        assertTrue(list.contains(extendClass2))
        assertFalse(list.contains(unrelatedClass))
        assertFalse(list.contains(unrelatedClass2))
    }

    private fun createTestClassDescriptor(name: String, type: String, extendName: String): ClassDescriptor =
        ClassDescriptor("", name, listOf(), listOf(), listOf(), listOf(), type, ExtendsDescriptor("", extendName))
}